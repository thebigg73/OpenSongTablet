package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

class Transpose {
	
    private int oldchordformat;
    //  A  A#/Bb  B/Cb  C/B#  C#/Db    D    D#/Eb   E/Fb   E#/F   F#/Gb   G     G#/Ab
    //  A    B     H     C
    //  1    2     3     4      5      6      7      8      9      10     11     12
    
    // Sharp chords first
    private  String[] chordsharpsnumsa   = {"$.2.$",   "$.4.$",   "$.5.$",   "$.7.$",   "$.9.$",   "$.10.$",  "$.12.$"};
    private  String[] chordsharpsnumsb   = {"$.32.$",  "$.34.$",  "$.35.$",  "$.37.$",  "$.39.$",  "$.40.$",  "$.42.$"};
    private  String[] chordsharpsnumsc   = {"$.52.$",  "$.54.$",  "$.55.$",  "$.57.$",  "$.59.$",  "$.60.$",  "$.62.$"};
    private  String[] sharpchords1a      = {"A#",      "B#",      "C#",      "D#",      "E#",      "F#",      "G#"};
    private  String[] sharpchords1b      = {"A#m",     "B#m",     "C#m",     "D#m",     "E#m",     "F#m",     "G#m"};      // For key only
    private  String[] sharpchords2       = {"A#",      "H#",      "C#",      "D#",      "E#",      "F#",      "G#"};
    private  String[] sharpchords3a      = {"Ais",     "His",     "Cis",     "Dis",     "Eis",     "Fis",     "Gis"};
    private  String[] sharpchords3b      = {" ais",    " his",    " cis",    " dis",    " eis",    " fis",    " gis"};
    private  String[] sharpchords3c      = {".ais",    ".his",    ".cis",    ".dis",    ".eis",    ".fis",    ".gis"};
    private  String[] sharpchords4       = {"La#",     "Si#",     "Do#",     "Ré#",     "Mi#",     "Fa#",     "Sol#"};
    private  String[] sharpchords5       = {"A#",      "B#",      "C#",      "D#",      "E#",      "F#",      "G#"};

    private  String[] properchordsharpsnumsa   = {"$.2.$",   "$.5.$",   "$.7.$",   "$.10.$",  "$.12.$"};  // For number to chord
    private  String[] properchordsharpsnumsb   = {"$.32.$",  "$.35.$",  "$.37.$",  "$.40.$",  "$.42.$"};  // For number to chord
    private  String[] properchordsharpsnumsc   = {"$.52.$",  "$.55.$",  "$.57.$",  "$.60.$",  "$.62.$"};  // For number to chord
    private  String[] propersharpchords1a      = {"A#",      "C#",      "D#",      "F#",      "G#"};      // For number to chord
    private  String[] propersharpchords1b      = {"A#m",     "C#m",     "D#m",     "F#m",     "G#m"};     // For number to chord
    private  String[] propersharpchords2       = {"A#",      "C#",      "D#",      "F#",      "G#"};      // For number to chord
    private  String[] propersharpchords3a      = {"Ais",     "Cis",     "Dis",     "Fis",     "Gis"};     // For number to chord
    private  String[] propersharpchords3b      = {" ais",    " cis",    " dis",    " fis",    " gis"};    // For number to chord
    private  String[] propersharpchords3c      = {".ais",    ".cis",    ".dis",    ".fis",    ".gis"};    // For number to chord
    private  String[] propersharpchords4       = {"La#",     "Do#",     "Ré#",     "Fa#",     "Sol#"};    // For number to chord
    private  String[] propersharpchords5       = {"A#",      "C#",      "D#",      "F#",      "G#"};      // For number to chord

    // Flat chords next
    private  String[] chordflatsnumsa    = {"$.12.$",  "$.2.$",   "$.3.$",   "$.5.$",   "$.7.$",   "$.8.$",   "$.10.$"};
    private  String[] chordflatsnumsb    = {"$.42.$",  "$.32.$",  "$.33.$",  "$.35.$",  "$.37.$",  "$.38.$",  "$.40.$"};
    private  String[] chordflatsnumsc    = {"$.62.$",  "$.52.$",  "$.53.$",  "$.55.$",  "$.57.$",  "$.58.$",  "$.60.$"};
    private  String[] flatchords1a       = {"Ab",      "Bb",      "Cb",      "Db",      "Eb",      "Fb",      "Gb"};
    private  String[] flatchords1b       = {"Abm",     "Bbm",     "Cbm",     "Dbm",     "Ebm",     "Fbm",     "Gbm"};      // For key only
    private  String[] flatchords2        = {"Ab",      "B",       "Cb",      "Db",      "Eb",      "Fb",      "Gb"};
    private  String[] flatchords3a       = {"As",      "B",       "Ces",     "Des",     "Es",      "Fes",     "Ges"};
    private  String[] flatchords3b       = {" as",     " b",      " ces",    " des",    " es",     " fes",    " ges"};
    private  String[] flatchords3c       = {".as",     ".b",      ".ces",    ".des",    ".es",     ".fes",    ".ges"};
    private  String[] flatchords4        = {"Lab",     "Sib",     "Dob",     "Réb",     "Mib",     "Fab",     "Solb"};
    private  String[] flatchords5        = {"Ab",      "Bb",      "Cb",      "Db",      "Eb",      "Fb",      "Gb"};

    private  String[] properchordflatsnumsa    = {"$.12.$",  "$.2.$",   "$.5.$",   "$.7.$",   "$.10.$"};// For number to chord
    private  String[] properchordflatsnumsb    = {"$.42.$",  "$.32.$",  "$.35.$",  "$.37.$",  "$.40.$"};// For number to chord
    private  String[] properchordflatsnumsc    = {"$.62.$",  "$.52.$",  "$.55.$",  "$.57.$",  "$.60.$"};// For number to chord
    private  String[] properflatchords1a       = {"Ab",      "Bb",      "Db",      "Eb",      "Gb"};    // For number to chord
    private  String[] properflatchords2        = {"Ab",      "B",       "Db",      "Eb",      "Gb"};    // For number to chord
    private  String[] properflatchords3a       = {"As",      "B",       "Des",     "Es",      "Ges"};   // For number to chord
    private  String[] properflatchords3b       = {" as",     " b",      " des",    " es",     " ges"};  // For number to chord
    private  String[] properflatchords3c       = {".as",     ".b",      ".des",    ".es",     ".ges"};  // For number to chord
    private  String[] properflatchords4        = {"Lab",     "Sib",     "Réb",     "Mib",     "Solb"};  // For number to chord
    private  String[] properflatchords5        = {"Ab",      "Bb",      "Db",      "Eb",      "Gb"};    // For number to chord

    // Finally the natural chords
    private  String[] chordnaturalnumsa  = {"$.1.$",   "$.3.$",   "$.4.$",   "$.6.$",   "$.8.$",   "$.9.$",   "$.11.$"};
    private  String[] chordnaturalnumsb  = {"$.31.$",  "$.33.$",  "$.34.$",  "$.36.$",  "$.38.$",  "$.39.$",  "$.41.$"};
    private  String[] chordnaturalnumsc  = {"$.51.$",  "$.53.$",  "$.54.$",  "$.56.$",  "$.58.$",  "$.59.$",  "$.61.$"};
    private  String[] naturalchords1a    = {"A",       "B",       "C",       "D",       "E",       "F",       "G"};
    private  String[] naturalchords1b    = {"Am",      "Bm",      "Cm",      "Dm",      "Em",      "Fm",      "Gm"};       // For key only
    private  String[] naturalchords2     = {"A",       "H",       "C",       "D",       "E",       "F",       "G"};
    private  String[] naturalchords3a    = {"A",       "H",       "C",       "D",       "E",       "F",       "G"};
    private  String[] naturalchords3b    = {" a",      " h",      " c",      " d",      " e",      " f",      " g"};
    private  String[] naturalchords3c    = {".a",      ".h",      ".c",      ".d",      ".e",      ".f",      ".g"};
    private  String[] naturalchords4     = {"La",      "Si",      "Do",      "Ré",      "Mi",      "Fa",      "Sol"};
    private  String[] naturalchords5     = {"A",       "B",       "C",       "D",       "E",       "F",       "G"};

    private  String originalkey = StaticVariables.mKey;

    private  boolean usesflats;
    private  boolean capousesflats;

     void doTranspose(Context c, Preferences preferences, boolean forcesharps, boolean forceflats, boolean convertchords) {

        try {
            // Go through each line and change each chord to $..$
            // This marks the bit to be changed

            StaticVariables.transposedLyrics = null;
            StaticVariables.transposedLyrics = "";
            FullscreenActivity.myTransposedLyrics = null;
            FullscreenActivity.myTransposedLyrics = StaticVariables.mLyrics.split("\n");

            oldchordformat = StaticVariables.detectedChordFormat;
            // Change the saved key to a number
            if (originalkey != null && !StaticVariables.mKey.equals("")) {
                String newkey = keyToNumber(StaticVariables.mKey);

                // Transpose the key
                newkey = transposeKey(newkey, StaticVariables.transposeDirection, StaticVariables.transposeTimes);

                // Convert the keynumber to a key
                newkey = numberToKey(c, preferences, newkey);

                // Decide if flats should be used
                usesflats = keyUsesFlats(c, preferences, newkey);

                StaticVariables.mKey = newkey;
            }

            // Now we change the chords into numbers
            for (int x = 0; x < FullscreenActivity.myTransposedLyrics.length; x++) {
                if (FullscreenActivity.myTransposedLyrics[x].indexOf(".") == 0) {
                    // Since this line has chords, do the changing!
                    // Decide on the chord format to use
                    switch (oldchordformat) {
                        default:
                            FullscreenActivity.myTransposedLyrics[x] = chordToNumber1(FullscreenActivity.myTransposedLyrics[x]);
                            break;

                        case 2:
                            FullscreenActivity.myTransposedLyrics[x] = chordToNumber2(FullscreenActivity.myTransposedLyrics[x]);
                            break;

                        case 3:
                            FullscreenActivity.myTransposedLyrics[x] = chordToNumber3(FullscreenActivity.myTransposedLyrics[x]);
                            break;

                        case 4:
                            FullscreenActivity.myTransposedLyrics[x] = chordToNumber4(FullscreenActivity.myTransposedLyrics[x]);
                            break;
                    }
                }
            }

            // Next up we do the transposing

            transposeChords();

            StringBuilder sb = new StringBuilder();
            // Now we put the numbers back into chords in the correct format and using either the key preference or the forced sharps or flats
            for (int x = 0; x < FullscreenActivity.myTransposedLyrics.length; x++) {
                if (FullscreenActivity.myTransposedLyrics[x].indexOf(".") == 0) {
                    // Since this line has chords, do the changing!
                    // Decide on the chord format to use
                    if (preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",false) ||
                            convertchords) {
                        // User has specified using their preferred chord format every time
                        oldchordformat = preferences.getMyPreferenceInt(c,"chordFormat",1);
                        // This is only true when the user clicks the option in the menu, so reset
                    }

                    switch (oldchordformat) {
                        default:
                            FullscreenActivity.myTransposedLyrics[x] = numberToChord1(FullscreenActivity.myTransposedLyrics[x],
                                    forcesharps, forceflats);
                            break;

                        case 2:
                            FullscreenActivity.myTransposedLyrics[x] = numberToChord2(FullscreenActivity.myTransposedLyrics[x],
                                    forcesharps, forceflats);
                            break;

                        case 3:
                            FullscreenActivity.myTransposedLyrics[x] = numberToChord3(FullscreenActivity.myTransposedLyrics[x],
                                    forcesharps, forceflats);
                            break;

                        case 4:
                            FullscreenActivity.myTransposedLyrics[x] = numberToChord4(FullscreenActivity.myTransposedLyrics[x],
                                    forcesharps, forceflats);
                            break;
                    }
                }

                // Add all the lines back up as a string
                sb.append(FullscreenActivity.myTransposedLyrics[x]).append("\n");
            }

            StaticVariables.transposedLyrics = sb.toString();

            // Now that the chords have been changed, replace the myTransposedLyrics
            // into the file
            FullscreenActivity.mynewXML = null;
            FullscreenActivity.mynewXML = "";

            // Write the new improved XML file
            StaticVariables.mLyrics = StaticVariables.transposedLyrics;

            PopUpEditSongFragment.prepareSongXML();
            PopUpEditSongFragment.justSaveSongXML(c, preferences);

            StaticVariables.transposedLyrics = null;
            StaticVariables.transposedLyrics = "";
            if (FullscreenActivity.myTransposedLyrics!=null) {
                Arrays.fill(FullscreenActivity.myTransposedLyrics, null);
            }
            if (FullscreenActivity.myParsedLyrics!=null) {
                Arrays.fill(FullscreenActivity.myParsedLyrics, null);
            }
            FullscreenActivity.myLyrics = null;
            FullscreenActivity.myLyrics = "";
            FullscreenActivity.mynewXML = null;
            FullscreenActivity.mynewXML = "";
            FullscreenActivity.myXML = null;
            FullscreenActivity.myXML = "";

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String transposeThisString(Context c, Preferences preferences, boolean convertchords,
                                      boolean forcesharps, boolean forceflats, String direction,
                                      int times, String texttotranspose) {
        try {
            // Go through each line and change each chord to $..$
            // This marks the bit to be changed

            Log.d("d", "direction=" + direction);
            Log.d("d", "times=" + direction);
            Log.d("d", "texttotranspose=" + texttotranspose);

            StaticVariables.transposedLyrics = null;
            StaticVariables.transposedLyrics = "";
            FullscreenActivity.myTransposedLyrics = null;
            FullscreenActivity.myTransposedLyrics = texttotranspose.split("\n");

            StaticVariables.transposeDirection = direction;
            StaticVariables.transposeTimes = times;

            oldchordformat = StaticVariables.detectedChordFormat;

            // Now we change the chords into numbers
            for (int x = 0; x < FullscreenActivity.myTransposedLyrics.length; x++) {
                if (FullscreenActivity.myTransposedLyrics[x].indexOf(".") == 0) {
                    // Since this line has chords, do the changing!
                    // Decide on the chord format to use
                    switch (oldchordformat) {
                        default:
                            FullscreenActivity.myTransposedLyrics[x] = chordToNumber1(FullscreenActivity.myTransposedLyrics[x]);
                            break;

                        case 2:
                            FullscreenActivity.myTransposedLyrics[x] = chordToNumber2(FullscreenActivity.myTransposedLyrics[x]);
                            break;

                        case 3:
                            FullscreenActivity.myTransposedLyrics[x] = chordToNumber3(FullscreenActivity.myTransposedLyrics[x]);
                            break;

                        case 4:
                            FullscreenActivity.myTransposedLyrics[x] = chordToNumber4(FullscreenActivity.myTransposedLyrics[x]);
                            break;

                        case 5:
                            FullscreenActivity.myTransposedLyrics[x] = chordToNumber5(FullscreenActivity.myTransposedLyrics[x]);
                            break;

                    }
                }
            }

            // Next up we do the transposing

            transposeChords();

            StringBuilder sb = new StringBuilder();
            // Now we put the numbers back into chords in the correct format and using either the key preference or the forced sharps or flats
            for (int x = 0; x < FullscreenActivity.myTransposedLyrics.length; x++) {
                if (FullscreenActivity.myTransposedLyrics[x].indexOf(".") == 0) {
                    // Since this line has chords, do the changing!
                    // Decide on the chord format to use
                    if (preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",false) || convertchords) {
                        // User has specified using their preferred chord format every time
                        oldchordformat = preferences.getMyPreferenceInt(c,"chordFormat",1);
                        // This is only true when the user clicks the option in the menu, so reset
                    }

                    switch (oldchordformat) {
                        default:
                            FullscreenActivity.myTransposedLyrics[x] = numberToChord1(FullscreenActivity.myTransposedLyrics[x],
                                    forcesharps, forceflats);
                            break;

                        case 2:
                            FullscreenActivity.myTransposedLyrics[x] = numberToChord2(FullscreenActivity.myTransposedLyrics[x],
                                    forcesharps, forceflats);
                            break;

                        case 3:
                            FullscreenActivity.myTransposedLyrics[x] = numberToChord3(FullscreenActivity.myTransposedLyrics[x],
                                    forcesharps, forceflats);
                            break;

                        case 4:
                            FullscreenActivity.myTransposedLyrics[x] = numberToChord4(FullscreenActivity.myTransposedLyrics[x],
                                    forcesharps, forceflats);
                            break;

                        case 5:
                            FullscreenActivity.myTransposedLyrics[x] = numberToChord5(FullscreenActivity.myTransposedLyrics[x],
                                    forcesharps, forceflats);
                    }
                }

                // Add all the lines back up as a string
                sb.append(FullscreenActivity.myTransposedLyrics[x]).append("\n");
            }

            // Now that the chords have been changed, return the lyrics
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return texttotranspose;
        }
    }

     String keyToNumber(String key) {
        // Swap the key with the correct number

        // Look for minor keys first
        for (int z=0;z<sharpchords1b.length;z++) {
            key = key.replace(sharpchords1b[z],chordsharpsnumsb[z]);
        }
        for (int z=0;z<flatchords1b.length;z++) {
            key = key.replace(flatchords1b[z],chordflatsnumsb[z]);
        }
        for (int z=0;z<naturalchords1b.length;z++) {
            key = key.replace(naturalchords1b[z],chordnaturalnumsb[z]);
        }

        // Look for major keys next
        for (int z=0;z<sharpchords1a.length;z++) {
            key = key.replace(sharpchords1a[z],chordsharpsnumsa[z]);
        }
        for (int z=0;z<flatchords1a.length;z++) {
            key = key.replace(flatchords1a[z],chordflatsnumsa[z]);
        }
        for (int z=0;z<naturalchords1a.length;z++) {
            key = key.replace(naturalchords1a[z],chordnaturalnumsa[z]);
        }

        return key;
    }

    private  String chordToNumber1(String line) {
        // Look for sharps first
        for (int z=0;z<sharpchords1a.length;z++) {
            line = line.replace(sharpchords1a[z],chordsharpsnumsa[z]);
        }

        // Now flats
        for (int z=0;z<flatchords1a.length;z++) {
            line = line.replace(flatchords1a[z],chordflatsnumsa[z]);
        }

        // Finally naturals
        for (int z=0;z<naturalchords1a.length;z++) {
            line = line.replace(naturalchords1a[z],chordnaturalnumsa[z]);
        }
        return line;
    }

    private  String chordToNumber2(String line) {
        // Look for sharps first
        for (int z=0;z<sharpchords2.length;z++) {
            line = line.replace(sharpchords2[z],chordsharpsnumsa[z]);
        }

        // Now flats
        for (int z=0;z<flatchords1a.length;z++) {
            line = line.replace(flatchords2[z],chordflatsnumsa[z]);
        }

        // Finally naturals
        for (int z=0;z<naturalchords1a.length;z++) {
            line = line.replace(naturalchords2[z],chordnaturalnumsa[z]);
        }
        return line;
    }

    private  String chordToNumber3(String line) {
        // Look for sharps first
        for (int z=0;z<sharpchords3c.length;z++) {
            line = line.replace(sharpchords3c[z],chordsharpsnumsc[z]);
        }
        for (int z=0;z<sharpchords3b.length;z++) {
            line = line.replace(sharpchords3b[z],chordsharpsnumsb[z]);
        }
        for (int z=0;z<sharpchords3a.length;z++) {
            line = line.replace(sharpchords3a[z],chordsharpsnumsa[z]);
        }

        // Now flats
        for (int z=0;z<flatchords3c.length;z++) {
            line = line.replace(flatchords3c[z],chordflatsnumsc[z]);
        }
        for (int z=0;z<flatchords3b.length;z++) {
            line = line.replace(flatchords3b[z],chordflatsnumsb[z]);
        }
        for (int z=0;z<flatchords3a.length;z++) {
            line = line.replace(flatchords3a[z],chordflatsnumsa[z]);
        }

        // Finally naturals
        for (int z=0;z<naturalchords3c.length;z++) {
            line = line.replace(naturalchords3c[z],chordnaturalnumsc[z]);
        }
        for (int z=0;z<naturalchords3b.length;z++) {
            line = line.replace(naturalchords3b[z],chordnaturalnumsb[z]);
        }
        for (int z=0;z<naturalchords3a.length;z++) {
            line = line.replace(naturalchords3a[z],chordnaturalnumsa[z]);
        }
        return line;
    }

    private  String chordToNumber4(String line) {

        // Change any Re into Ré and Ti into Si
        line = line.replace("Re","Ré");
        line = line.replace("Ti","Si");

        // Change lowercase into correct case
        line = line.replace("do","Do");
        line = line.replace("re","ré");
        line = line.replace("ré","Ré");
        line = line.replace("mi","Mi");
        line = line.replace("fa","Fa");
        line = line.replace("sol","Sol");
        line = line.replace("la","La");
        line = line.replace("si","Si");

        // Look for sharps first
        for (int z=0;z<sharpchords4.length;z++) {
            line = line.replace(sharpchords4[z],chordsharpsnumsa[z]);
        }

        // Now flats
        for (int z=0;z<flatchords4.length;z++) {
            line = line.replace(flatchords4[z],chordflatsnumsa[z]);
        }

        // Finally naturals
        for (int z=0;z<naturalchords4.length;z++) {
            line = line.replace(naturalchords4[z],chordnaturalnumsa[z]);
        }
        return line;
    }

    private String chordToNumber5(String line) {
        //  This compares the chords with the song key
        String[] bits;
        switch(oldchordformat) {
            case 1:
            default:
                bits = "A A# B C C# D D# E F F# G G# A A# B C C# D D# E F F# G".split(" ");
                break;

            case 2:
            case 3:
                bits = "A B H C C# D D# E F F# G G# A B H C C# D D# E F F# G".split(" ");
                break;

            case 4:
                bits = "La La# Si Do Do# Ré Ré# Mi Fa Fa# Sol Sol# La La# Si".split(" ");
        }

        int root = 0;
        boolean stilllooking = true;
        for (int i=0;i<bits.length;i++) {
            if (stilllooking && bits[i].equals(originalkey)) {
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

        String nash1 = bits[root];
        String nash2 = bits[num2];
        String nash3 = bits[num3];
        String nash4 = bits[num4];
        String nash5 = bits[num5];
        String nash6 = bits[num6];
        String nash7 = bits[num7];

        return "";
    }

     String transposeKey(String getkeynum, String direction, int transposetimes) {
        if (direction.equals("+1")) {
            // Put the numbers up by one.
            // Last step then fixes 13 to be 1

            // Repeat this as often as required.
            for (int repeatTranspose = 0; repeatTranspose < transposetimes; repeatTranspose++) {
                getkeynum = getkeynum.replace("$.12.$", "$.13.$");
                getkeynum = getkeynum.replace("$.11.$", "$.12.$");
                getkeynum = getkeynum.replace("$.10.$", "$.11.$");
                getkeynum = getkeynum.replace("$.9.$", "$.10.$");
                getkeynum = getkeynum.replace("$.8.$", "$.9.$");
                getkeynum = getkeynum.replace("$.7.$", "$.8.$");
                getkeynum = getkeynum.replace("$.6.$", "$.7.$");
                getkeynum = getkeynum.replace("$.5.$", "$.6.$");
                getkeynum = getkeynum.replace("$.4.$", "$.5.$");
                getkeynum = getkeynum.replace("$.3.$", "$.4.$");
                getkeynum = getkeynum.replace("$.2.$", "$.3.$");
                getkeynum = getkeynum.replace("$.1.$", "$.2.$");
                getkeynum = getkeynum.replace("$.13.$", "$.1.$");

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
                getkeynum = getkeynum.replace("$.1.$", "$.0.$");
                getkeynum = getkeynum.replace("$.2.$", "$.1.$");
                getkeynum = getkeynum.replace("$.3.$", "$.2.$");
                getkeynum = getkeynum.replace("$.4.$", "$.3.$");
                getkeynum = getkeynum.replace("$.5.$", "$.4.$");
                getkeynum = getkeynum.replace("$.6.$", "$.5.$");
                getkeynum = getkeynum.replace("$.7.$", "$.6.$");
                getkeynum = getkeynum.replace("$.8.$", "$.7.$");
                getkeynum = getkeynum.replace("$.9.$", "$.8.$");
                getkeynum = getkeynum.replace("$.10.$", "$.9.$");
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

    private  void transposeChords() {
        // Go through each line in turn
        for (int x = 0; x < FullscreenActivity.myTransposedLyrics.length; x++) {

            // Only do transposing if it is a chord line (starting with .)
            if (FullscreenActivity.myTransposedLyrics[x].startsWith(".")) {
                if (StaticVariables.transposeDirection.equals("+1")) {
                    // Put the numbers up by one.
                    // Last step then fixes 12 to be 0

                    // Repeat this as often as required.
                    for (int repeatTranspose = 0; repeatTranspose < StaticVariables.transposeTimes; repeatTranspose++) {
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.12.$", "$.13.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "$.12.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.10.$", "$.11.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "$.10.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.8.$", "$.9.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.7.$", "$.8.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "$.7.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.5.$", "$.6.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "$.5.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.3.$", "$.4.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.2.$", "$.3.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "$.2.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.13.$", "$.1.$");

                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.42.$", "$.43.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", "$.42.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.40.$", "$.41.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", "$.40.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.38.$", "$.39.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.37.$", "$.38.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", "$.37.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.35.$", "$.36.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", "$.35.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.33.$", "$.34.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.32.$", "$.33.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", "$.32.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.43.$", "$.31.$");

                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.62.$", "$.63.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", "$.62.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.60.$", "$.61.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", "$.60.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.58.$", "$.59.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.57.$", "$.58.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", "$.57.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.55.$", "$.56.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", "$.55.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.53.$", "$.54.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.52.$", "$.53.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", "$.52.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.63.$", "$.51.$");
                    }
                }

                if (StaticVariables.transposeDirection.equals("-1")) {
                    // Put the numbers down by one.
                    // Move num 0 down to -1 (if it goes to 11 it will be moved
                    // later)
                    // Last step then fixes -1 to be 11

                    // Repeat this as often as required.
                    for (int repeatTranspose = 0; repeatTranspose < StaticVariables.transposeTimes; repeatTranspose++) {
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "$.0.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.2.$", "$.1.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.3.$", "$.2.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "$.3.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.5.$", "$.4.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "$.5.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.7.$", "$.6.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.8.$", "$.7.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "$.8.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.10.$", "$.9.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "$.10.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.12.$", "$.11.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.0.$", "$.12.$");

                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", "$.30.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.32.$", "$.31.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.33.$", "$.32.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", "$.33.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.35.$", "$.34.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", "$.35.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.37.$", "$.36.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.38.$", "$.37.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", "$.38.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.40.$", "$.39.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", "$.40.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.42.$", "$.41.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.30.$", "$.42.$");

                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", "$.50.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.52.$", "$.51.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.53.$", "$.52.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", "$.53.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.55.$", "$.54.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", "$.55.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.57.$", "$.56.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.58.$", "$.57.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", "$.58.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.60.$", "$.59.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", "$.60.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.62.$", "$.61.$");
                        FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.50.$", "$.62.$");
                    }
                }
            }
        }
    }

     String numberToKey(Context c, Preferences preferences, String key) {
        // We need to decide which key the user likes the best for each one
        // Convert the key number into either a sharp or natural first
        // Then we swap sharps to flats if the user prefers these

        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            key = key.replace(properchordsharpsnumsa[z],propersharpchords1a[z]);
        }
        for (int z=0; z<properchordsharpsnumsb.length; z++) {
            key = key.replace(properchordsharpsnumsb[z],propersharpchords1b[z]);
        }
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            key = key.replace(chordnaturalnumsa[z],naturalchords1a[z]);
        }
        for (int z=0; z<chordnaturalnumsb.length; z++) {
            key = key.replace(chordnaturalnumsb[z],naturalchords1b[z]);
        }

        if (key.equals("G#") && preferences.getMyPreferenceBoolean(c,"prefKeyAb",true)) {
            key = "Ab";
        } else if (key.equals("G#m") && preferences.getMyPreferenceBoolean(c,"prefKeyAbm",false)) {
            key = "Abm";
        } else if (key.equals("A#") && preferences.getMyPreferenceBoolean(c,"prefKeyBb",true)) {
            key = "Bb";
        } else if (key.equals("A#m") && preferences.getMyPreferenceBoolean(c,"prefKeyBbm",true)) {
            key = "Bbm";
        } else if (key.equals("C#") && preferences.getMyPreferenceBoolean(c,"prefKeyDb",false)) {
            key = "Db";
        } else if (key.equals("C#m") && preferences.getMyPreferenceBoolean(c,"prefKeyDbm",true)) {
            key = "Dbm";
        } else if (key.equals("D#") && preferences.getMyPreferenceBoolean(c,"prefKeyEb",true)) {
            key = "Eb";
        } else if (key.equals("D#m") && preferences.getMyPreferenceBoolean(c,"prefKeyEbm",true)) {
            key = "Ebm";
        } else if (key.equals("F#") && preferences.getMyPreferenceBoolean(c,"prefKeyGb",false)) {
            key = "Gb";
        } else if (key.equals("F#m") && preferences.getMyPreferenceBoolean(c,"prefKeyGbm",false)) {
            key = "Gbm";
        }

        return key;
    }

    private  String numberToChord1(String line, boolean forcesharps, boolean forceflats) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (forcesharps || (!forceflats && !usesflats)) {
            line = useSharps1(line);
        }
        if (forceflats || (!forcesharps && usesflats)) {
            line = useFlats1(line);
        }

        // Replace the naturals
        line = useNaturals1(line);

        return line;
    }

    private  String capoNumberToChord1(String line, boolean forcesharps, boolean forceflats) {
        if (forcesharps || (!forceflats && !capousesflats)) {
            line = useSharps1(line);
        }
        if (forceflats || (!forcesharps && capousesflats)) {
            line = useFlats1(line);
        }

        // Replace the naturals
        line = useNaturals1(line);

        return line;
    }

    private  String numberToChord2(String line, boolean forcesharps, boolean forceflats) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (forcesharps || (!forceflats && !usesflats)) {
            line = useSharps2(line);
        }
        if (forceflats || (!forcesharps && usesflats)) {
            line = useFlats2(line);
        }

        // Replace the naturals
        line = useNaturals2(line);

        return line;
    }

    private  String capoNumberToChord2(String line, boolean forcesharps, boolean forceflats) {
        if (forcesharps || (!forceflats && !capousesflats)) {
            line = useSharps2(line);
        }
        if (forceflats || (!forcesharps && capousesflats)) {
            line = useFlats2(line);
        }

        // Replace the naturals
        line = useNaturals2(line);

        return line;
    }

    private  String numberToChord3(String line, boolean forcesharps, boolean forceflats) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (forcesharps || (!forceflats && !usesflats)) {
            line = useSharps3(line);
        }
        if (forceflats || (!forcesharps && usesflats)) {
            line = useFlats3(line);
        }

        // Replace the naturals
        line = useNaturals3(line);

        return line;
    }

    private  String capoNumberToChord3(String line, boolean forcesharps, boolean forceflats) {
        if (forcesharps || (!forceflats && !capousesflats)) {
            line = useSharps3(line);
        }
        if (forceflats || (!forcesharps && capousesflats)) {
            line = useFlats3(line);
        }

        // Replace the naturals
        line = useNaturals3(line);

        return line;
    }

    private  String numberToChord4(String line, boolean forcesharps, boolean forceflats) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (forcesharps || (!forceflats && !usesflats)) {
            line = useSharps4(line);
        }
        if (forceflats || (!forcesharps && usesflats)) {
            line = useFlats4(line);
        }

        // Replace the naturals
        line = useNaturals4(line);

        return line;
    }

    private  String capoNumberToChord4(String line, boolean forcesharps, boolean forceflats) {
        if (forcesharps || (!forceflats && !capousesflats)) {
            line = useSharps4(line);
        }
        if (forceflats || (!forcesharps && capousesflats)) {
            line = useFlats4(line);
        }

        // Replace the naturals
        line = useNaturals4(line);

        return line;
    }

    private  String numberToChord5(String line, boolean forcesharps, boolean forceflats) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (forcesharps || (!forceflats && !usesflats)) {
            line = useSharps5(line);
        }
        if (forceflats || (!forcesharps && usesflats)) {
            line = useFlats5(line);
        }

        // Replace the naturals
        line = useNaturals5(line);

        return line;
    }
    private  boolean keyUsesFlats(Context c, Preferences preferences, String testkey) {

        boolean result;
        result = (testkey.equals("Ab") && preferences.getMyPreferenceBoolean(c,"prefKeyAb",true)) ||
                (testkey.equals("Bb") && preferences.getMyPreferenceBoolean(c,"prefKeyBb",true)) ||
                (testkey.equals("Db") && preferences.getMyPreferenceBoolean(c,"prefKeyDb",false)) ||
                (testkey.equals("Eb") && preferences.getMyPreferenceBoolean(c,"prefKeyEb",true)) ||
                (testkey.equals("Gb") && preferences.getMyPreferenceBoolean(c,"prefKeyGb",false)) ||
                (testkey.equals("Bbm") && preferences.getMyPreferenceBoolean(c,"prefKeyBbm",true)) ||
                (testkey.equals("Dbm") && preferences.getMyPreferenceBoolean(c,"prefKeyDbm",false)) ||
                (testkey.equals("Ebm") && preferences.getMyPreferenceBoolean(c,"prefKeyEbm",true)) ||
                (testkey.equals("Gbm") && preferences.getMyPreferenceBoolean(c,"prefKeyGbm",false)) ||
                testkey.equals("C") ||
                testkey.equals("F") ||
                testkey.equals("Dm") ||
                testkey.equals("Gm") ||
                testkey.equals("Cm");
        return result;
    }

    private  String useFlats1(String line) {
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords1a[z]);
        }
        return line;
    }

    private  String useFlats2(String line) {
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords2[z]);
        }
        return line;
    }

    private  String useFlats3(String line) {
        for (int z=0; z<properchordflatsnumsc.length; z++) {
            line = line.replace(properchordflatsnumsc[z],properflatchords3c[z]);
        }
        for (int z=0; z<properchordflatsnumsb.length; z++) {
            line = line.replace(properchordflatsnumsb[z],properflatchords3b[z]);
        }
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords3a[z]);
        }
        return line;
    }

    private  String useFlats4(String line) {
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords4[z]);
        }
        return line;
    }

    private  String useFlats5(String line) {
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords5[z]);
        }
        return line;
    }

    private  String useSharps1(String line) {
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords1a[z]);
        }
        return line;
    }

    private  String useSharps2(String line) {
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords2[z]);
        }
        return line;
    }

    private  String useSharps3(String line) {
        for (int z=0; z<properchordsharpsnumsc.length; z++) {
            line = line.replace(properchordsharpsnumsc[z],propersharpchords3c[z]);
        }
        for (int z=0; z<properchordsharpsnumsb.length; z++) {
            line = line.replace(properchordsharpsnumsb[z],propersharpchords3b[z]);
        }
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords3a[z]);
        }
        return line;
    }

    private  String useSharps4(String line) {
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords4[z]);
        }
        return line;
    }

    private  String useSharps5(String line) {
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords5[z]);
        }
        return line;
    }

    private  String useNaturals1(String line) {
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords1a[z]);
        }
        return line;
    }

    private  String useNaturals2(String line) {
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords2[z]);
        }
        return line;
    }

    private  String useNaturals3(String line) {
        for (int z=0; z<chordnaturalnumsc.length; z++) {
            line = line.replace(chordnaturalnumsc[z],naturalchords3c[z]);
        }
        for (int z=0; z<chordnaturalnumsb.length; z++) {
            line = line.replace(chordnaturalnumsb[z],naturalchords3b[z]);
        }
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords3a[z]);
        }
        return line;
    }

    private  String useNaturals4(String line) {
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords4[z]);
        }
        return line;
    }

    private  String useNaturals5(String line) {
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords5[z]);
        }
        return line;
    }

    void capoTranspose(Context c, Preferences preferences, boolean forcesharps, boolean forceflats) {

        int numtimes = Integer.parseInt(StaticVariables.mCapo);

        // Get the capokey if it hasn't been set
        if (StaticVariables.mKey!=null) {
            FullscreenActivity.capokey = keyToNumber(StaticVariables.mKey);
            FullscreenActivity.capokey = transposeKey(FullscreenActivity.capokey,"-1",numtimes);
            FullscreenActivity.capokey = numberToKey(c, preferences, FullscreenActivity.capokey);
            // Decide if flats should be used
            capousesflats = keyUsesFlats(c, preferences, FullscreenActivity.capokey);
        }

        // Now we change the chords into numbers
        switch (StaticVariables.detectedChordFormat) {
            default:
                StaticVariables.temptranspChords = chordToNumber1(StaticVariables.temptranspChords);
                break;

            case 2:
                StaticVariables.temptranspChords = chordToNumber2(StaticVariables.temptranspChords);
                break;

            case 3:
                StaticVariables.temptranspChords = chordToNumber3(StaticVariables.temptranspChords);
                break;

            case 4:
                StaticVariables.temptranspChords = chordToNumber4(StaticVariables.temptranspChords);
                break;
        }

        // Try to do a sensible capo change.
        // Do a for loop for each capo chord changing it by one each time until the desired fret change
        for (int s = 0; s < numtimes; s++) {
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.1.$", "$.0.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.2.$", "$.1.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.3.$", "$.2.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.4.$", "$.3.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.5.$", "$.4.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.6.$", "$.5.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.7.$", "$.6.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.8.$", "$.7.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.9.$", "$.8.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.10.$", "$.9.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.11.$", "$.10.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.12.$", "$.11.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.0.$", "$.12.$");

            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.31.$", "$.30.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.32.$", "$.31.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.33.$", "$.32.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.34.$", "$.33.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.35.$", "$.34.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.36.$", "$.35.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.37.$", "$.36.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.38.$", "$.37.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.39.$", "$.38.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.40.$", "$.39.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.41.$", "$.40.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.42.$", "$.41.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.30.$", "$.42.$");

            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.51.$", "$.50.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.52.$", "$.51.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.53.$", "$.52.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.54.$", "$.53.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.55.$", "$.54.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.56.$", "$.55.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.57.$", "$.56.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.58.$", "$.57.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.59.$", "$.58.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.60.$", "$.59.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.61.$", "$.60.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.62.$", "$.61.$");
            StaticVariables.temptranspChords = StaticVariables.temptranspChords.replace("$.50.$", "$.62.$");
        }

        // Now convert the numbers back to the appropriate chords
        switch (StaticVariables.detectedChordFormat) {
            default:
                StaticVariables.temptranspChords = capoNumberToChord1(StaticVariables.temptranspChords,
                        forcesharps, forceflats);
                break;

            case 2:
                StaticVariables.temptranspChords = capoNumberToChord2(StaticVariables.temptranspChords,
                        forcesharps, forceflats);
                break;

            case 3:
                StaticVariables.temptranspChords = capoNumberToChord3(StaticVariables.temptranspChords,
                        forcesharps, forceflats);
                break;

            case 4:
                StaticVariables.temptranspChords = capoNumberToChord4(StaticVariables.temptranspChords,
                        forcesharps, forceflats);
                break;
        }
    }

     ArrayList<String> quickCapoKey(Context c, Preferences preferences, String key) {
        // This is used to give the user a list of either simple fret number or fret number with new capo key
        ArrayList<String> al = new ArrayList<>();
        // Add a blank entry
        al.add("");

        boolean keyisset = false;
        String keynum = key;
        if (key!=null && !key.equals("") && !key.isEmpty()) {
            // A key has been set, so convert this to a number
            keynum = keyToNumber(key);
            keyisset = true;
        }

        // Loop through this 12 times to get the key options for each fret
        // The capo key goes down a semitone as the capo fret goes up a semitone!
        String whattoadd;
        for (int i=1; i<=11; i++) {
            if (keyisset) {
                keynum = transposeKey(keynum, "-1", 1);
                key = numberToKey(c, preferences, keynum);
                whattoadd = i + " (" + key + ")";
            } else {
                whattoadd = i + "";
            }
            al.add(whattoadd);
        }
        return al;
    }

	 void checkChordFormat(Context c, Preferences preferences) {
		StaticVariables.transposedLyrics = null;
		StaticVariables.transposedLyrics = "";
		FullscreenActivity.myTransposedLyrics = null;
		FullscreenActivity.myTransposedLyrics = StaticVariables.mLyrics.split("\n");

		StaticVariables.detectedChordFormat = preferences.getMyPreferenceInt(c,"chordFormat",1);

		boolean contains_es_is = false;
		boolean contains_H = false;
		boolean contains_do = false;
        boolean contains_nash = false;
        boolean contains_nashnumeral = false;

		// Check if the user is using the same chord format as the song
		// Go through the chord lines and look for clues
        for (int x = 0; x < FullscreenActivity.myTransposedLyrics.length; x++) {
            if (FullscreenActivity.myTransposedLyrics[x].startsWith(".")) {
                // Chord line
                if (FullscreenActivity.myTransposedLyrics[x].contains("es") || FullscreenActivity.myTransposedLyrics[x].contains("is") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(" a") || FullscreenActivity.myTransposedLyrics[x].contains(".a") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(" b") || FullscreenActivity.myTransposedLyrics[x].contains(".b") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(" h") || FullscreenActivity.myTransposedLyrics[x].contains(".h") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(" c") || FullscreenActivity.myTransposedLyrics[x].contains(".c") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(" d") || FullscreenActivity.myTransposedLyrics[x].contains(".d") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(" e") || FullscreenActivity.myTransposedLyrics[x].contains(".e") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(" f") || FullscreenActivity.myTransposedLyrics[x].contains(".f") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(" g") || FullscreenActivity.myTransposedLyrics[x].contains(".g")) {
                    contains_es_is = true;
                } else if (FullscreenActivity.myTransposedLyrics[x].contains("H")) {
                    contains_H = true;
                } else if (FullscreenActivity.myTransposedLyrics[x].contains("Do") || FullscreenActivity.myTransposedLyrics[x].contains("Re") || FullscreenActivity.myTransposedLyrics[x].contains("Ré") ||
                        FullscreenActivity.myTransposedLyrics[x].contains("Me") || FullscreenActivity.myTransposedLyrics[x].contains("Fa") ||
                        FullscreenActivity.myTransposedLyrics[x].contains("Sol") || FullscreenActivity.myTransposedLyrics[x].contains("La") ||
                        FullscreenActivity.myTransposedLyrics[x].contains("Si")) {
                    contains_do = true;
                } else if (FullscreenActivity.myTransposedLyrics[x].contains(".2") || FullscreenActivity.myTransposedLyrics[x].contains(" 2") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(".3") || FullscreenActivity.myTransposedLyrics[x].contains(" 3") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(".4") || FullscreenActivity.myTransposedLyrics[x].contains(" 4") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(".5") || FullscreenActivity.myTransposedLyrics[x].contains(" 5") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(".6") || FullscreenActivity.myTransposedLyrics[x].contains(" 6") ||
                        FullscreenActivity.myTransposedLyrics[x].contains(".7") || FullscreenActivity.myTransposedLyrics[x].contains(" 7")) {
                    contains_nash = true;
                } else if (FullscreenActivity.myTransposedLyrics[x].contains(".I") || FullscreenActivity.myTransposedLyrics[x].contains(" I") ||
                    FullscreenActivity.myTransposedLyrics[x].contains(".V") || FullscreenActivity.myTransposedLyrics[x].contains(" V") ||
                    FullscreenActivity.myTransposedLyrics[x].contains(".IV") || FullscreenActivity.myTransposedLyrics[x].contains(" IV")) {
                    contains_nashnumeral = true;
                }
            }
        }

        //int detected = 0;
		// Set the chord style detected
		if (contains_do && !preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",false)) {
			StaticVariables.detectedChordFormat = 4;
			//detected = 3;
		} else if (contains_H && !contains_es_is && !preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",false)) {
			StaticVariables.detectedChordFormat = 2;
			//detected = 1;
		} else if (contains_H || contains_es_is && !preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",false)) {
			StaticVariables.detectedChordFormat = 3;
			//detected = 2;
		} else if (contains_nash && !preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",false)) {
            StaticVariables.detectedChordFormat = 5;
            //detected = 4;
        } else if (contains_nashnumeral && !preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",false)) {
		    StaticVariables.detectedChordFormat = 6;
		    //detected = 5;
        } else if (!preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",false)){
			StaticVariables.detectedChordFormat = 1;
			//detected = 0;
		}

        // Ok so the user chord format may not quite match the song - it might though!
	}

}