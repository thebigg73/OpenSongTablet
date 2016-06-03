package com.garethevans.church.opensongtablet;

import java.io.IOException;
import java.util.Arrays;
import android.app.Activity;

public class Transpose extends Activity {
	
	public static String oldchordformat;
    //  A  A#/Bb  B/Cb  C/B#  C#/Db    D    D#/Eb   E/Fb   E#/F   F#/Gb   G     G#/Ab
    //  A    B     H     C
    //  1    2     3     4      5      6      7      8      9      10     11     12
    
    // Sharp chords first
    public static String[] chordsharpsnumsa   = {"$.2.$",   "$.4.$",   "$.5.$",   "$.7.$",   "$.9.$",   "$.10.$",  "$.12.$"};
    public static String[] chordsharpsnumsb   = {"$.32.$",  "$.34.$",  "$.35.$",  "$.37.$",  "$.39.$",  "$.40.$",  "$.42.$"};
    public static String[] chordsharpsnumsc   = {"$.52.$",  "$.54.$",  "$.55.$",  "$.57.$",  "$.59.$",  "$.60.$",  "$.62.$"};
    public static String[] sharpchords1a      = {"A#",      "B#",      "C#",      "D#",      "E#",      "F#",      "G#"};
    public static String[] sharpchords1b      = {"A#m",     "B#m",     "C#m",     "D#m",     "E#m",     "F#m",     "G#m"};      // For key only
    public static String[] sharpchords2       = {"A#",      "H#",      "C#",      "D#",      "E#",      "F#",      "G#"};
    public static String[] sharpchords3a      = {"Ais",     "His",     "Cis",     "Dis",     "Eis",     "Fis",     "Gis"};
    public static String[] sharpchords3b      = {" ais",    " his",    " cis",    " dis",    " eis",    " fis",    " gis"};
    public static String[] sharpchords3c      = {".ais",    ".his",    ".cis",    ".dis",    ".eis",    ".fis",    ".gis"};
    public static String[] sharpchords4       = {"La#",     "Si#",     "Do#",     "Ré#",     "Mi#",     "Fa#",     "Sol#"};

    public static String[] properchordsharpsnumsa   = {"$.2.$",   "$.5.$",   "$.7.$",   "$.10.$",  "$.12.$"};  // For number to chord
    public static String[] properchordsharpsnumsb   = {"$.32.$",  "$.35.$",  "$.37.$",  "$.40.$",  "$.42.$"};  // For number to chord
    public static String[] properchordsharpsnumsc   = {"$.52.$",  "$.55.$",  "$.57.$",  "$.60.$",  "$.62.$"};  // For number to chord
    public static String[] propersharpchords1a      = {"A#",      "C#",      "D#",      "F#",      "G#"};      // For number to chord
    public static String[] propersharpchords1b      = {"A#m",     "C#m",     "D#m",     "F#m",     "G#m"};     // For number to chord
    public static String[] propersharpchords2       = {"A#",      "C#",      "D#",      "F#",      "G#"};      // For number to chord
    public static String[] propersharpchords3a      = {"Ais",     "Cis",     "Dis",     "Fis",     "Gis"};     // For number to chord
    public static String[] propersharpchords3b      = {" ais",    " cis",    " dis",    " fis",    " gis"};    // For number to chord
    public static String[] propersharpchords3c      = {".ais",    ".cis",    ".dis",    ".fis",    ".gis"};    // For number to chord
    public static String[] propersharpchords4       = {"La#",     "Do#",     "Ré#",     "Fa#",     "Sol#"};    // For number to chord

    // Flat chords next
    public static String[] chordflatsnumsa    = {"$.12.$",  "$.2.$",   "$.3.$",   "$.5.$",   "$.7.$",   "$.8.$",   "$.10.$"};
    public static String[] chordflatsnumsb    = {"$.42.$",  "$.32.$",  "$.33.$",  "$.35.$",  "$.37.$",  "$.38.$",  "$.40.$"};
    public static String[] chordflatsnumsc    = {"$.62.$",  "$.52.$",  "$.53.$",  "$.55.$",  "$.57.$",  "$.58.$",  "$.60.$"};
    public static String[] flatchords1a       = {"Ab",      "Bb",      "Cb",      "Db",      "Eb",      "Fb",      "Gb"};
    public static String[] flatchords1b       = {"Abm",     "Bbm",     "Cbm",     "Dbm",     "Ebm",     "Fbm",     "Gbm"};      // For key only
    public static String[] flatchords2        = {"Ab",      "B",       "Cb",      "Db",      "Eb",      "Fb",      "Gb"};
    public static String[] flatchords3a       = {"As",      "B",       "Ces",     "Des",     "Es",      "Fes",     "Ges"};
    public static String[] flatchords3b       = {" as",     " b",      " ces",    " des",    " es",     " fes",    " ges"};
    public static String[] flatchords3c       = {".as",     ".b",      ".ces",    ".des",    ".es",     ".fes",    ".ges"};
    public static String[] flatchords4        = {"Lab",     "Sib",     "Dob",     "Réb",     "Mib",     "Fab",     "Solb"};

    public static String[] properchordflatsnumsa    = {"$.12.$",  "$.2.$",   "$.5.$",   "$.7.$",   "$.10.$"};// For number to chord
    public static String[] properchordflatsnumsb    = {"$.42.$",  "$.32.$",  "$.35.$",  "$.37.$",  "$.40.$"};// For number to chord
    public static String[] properchordflatsnumsc    = {"$.62.$",  "$.52.$",  "$.55.$",  "$.57.$",  "$.60.$"};// For number to chord
    public static String[] properflatchords1a       = {"Ab",      "Bb",      "Db",      "Eb",      "Gb"};    // For number to chord
    public static String[] properflatchords2        = {"Ab",      "B",       "Db",      "Eb",      "Gb"};    // For number to chord
    public static String[] properflatchords3a       = {"As",      "B",       "Des",     "Es",      "Ges"};   // For number to chord
    public static String[] properflatchords3b       = {" as",     " b",      " des",    " es",     " ges"};  // For number to chord
    public static String[] properflatchords3c       = {".as",     ".b",      ".des",    ".es",     ".ges"};  // For number to chord
    public static String[] properflatchords4        = {"Lab",     "Sib",     "Réb",     "Mib",     "Solb"};  // For number to chord

    // Finally the natural chords
    public static String[] chordnaturalnumsa  = {"$.1.$",   "$.3.$",   "$.4.$",   "$.6.$",   "$.8.$",   "$.9.$",   "$.11.$"};
    public static String[] chordnaturalnumsb  = {"$.31.$",  "$.33.$",  "$.34.$",  "$.36.$",  "$.38.$",  "$.39.$",  "$.41.$"};
    public static String[] chordnaturalnumsc  = {"$.51.$",  "$.53.$",  "$.54.$",  "$.56.$",  "$.58.$",  "$.59.$",  "$.61.$"};
    public static String[] naturalchords1a    = {"A",       "B",       "C",       "D",       "E",       "F",       "G"};
    public static String[] naturalchords1b    = {"Am",      "Bm",      "Cm",      "Dm",      "Em",      "Fm",      "Gm"};       // For key only
    public static String[] naturalchords2     = {"A",       "H",       "C",       "D",       "E",       "F",       "G"};
    public static String[] naturalchords3a    = {"A",       "H",       "C",       "D",       "E",       "F",       "G"};
    public static String[] naturalchords3b    = {" a",      " h",      " c",      " d",      " e",      " f",      " g"};
    public static String[] naturalchords3c    = {".a",      ".h",      ".c",      ".d",      ".e",      ".f",      ".g"};
    public static String[] naturalchords4     = {"La",      "Si",      "Do",      "Ré",      "Mi",      "Fa",      "Sol"};

    public static String originalkey = FullscreenActivity.mKey;
    public static String newkey = FullscreenActivity.mKey;

    public static boolean usesflats;
    public static boolean capousesflats;

    public static void doTranspose() throws IOException {
        // Go through each line and change each chord to $..$
        // This marks the bit to be changed

        FullscreenActivity.transposedLyrics = null;
        FullscreenActivity.transposedLyrics = "";
        FullscreenActivity.myTransposedLyrics = null;
        FullscreenActivity.myTransposedLyrics = FullscreenActivity.mLyrics.split("\n");

        oldchordformat = FullscreenActivity.oldchordformat;

        // Change the saved key to a number
        if (originalkey != null && !FullscreenActivity.mKey.equals("")) {
            newkey = keyToNumber(FullscreenActivity.mKey);
        }
        // Transpose the key
        newkey = transposeKey(newkey, FullscreenActivity.transposeDirection, FullscreenActivity.transposeTimes);

        // Convert the keynumber to a key
        newkey = numberToKey(newkey);

        // Decide if flats should be used
        usesflats = keyUsesFlats(newkey);

        if (!FullscreenActivity.mKey.equals("")) {
            FullscreenActivity.mKey = newkey;
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

                    case "2":
                        FullscreenActivity.myTransposedLyrics[x] = chordToNumber2(FullscreenActivity.myTransposedLyrics[x]);
                        break;

                    case "3":
                        FullscreenActivity.myTransposedLyrics[x] = chordToNumber3(FullscreenActivity.myTransposedLyrics[x]);
                        break;

                    case "4":
                        FullscreenActivity.myTransposedLyrics[x] = chordToNumber4(FullscreenActivity.myTransposedLyrics[x]);
                        break;

                }
            }
        }

        // Next up we do the transposing

        transposeChords();

        // Now we put the numbers back into chords in the correct format and using either the key preference or the forced sharps or flats
        for (int x = 0; x < FullscreenActivity.myTransposedLyrics.length; x++) {
            if (FullscreenActivity.myTransposedLyrics[x].indexOf(".") == 0) {
                // Since this line has chords, do the changing!
                // Decide on the chord format to use
                switch (oldchordformat) {
                    default:
                        FullscreenActivity.myTransposedLyrics[x] = numberToChord1(FullscreenActivity.myTransposedLyrics[x]);
                        break;

                    case "2":
                        FullscreenActivity.myTransposedLyrics[x] = numberToChord2(FullscreenActivity.myTransposedLyrics[x]);
                        break;

                    case "3":
                        FullscreenActivity.myTransposedLyrics[x] = numberToChord3(FullscreenActivity.myTransposedLyrics[x]);
                        break;

                    case "4":
                        FullscreenActivity.myTransposedLyrics[x] = numberToChord4(FullscreenActivity.myTransposedLyrics[x]);
                        break;
                }
            }

        // Add all the lines back up as a string
        FullscreenActivity.transposedLyrics += FullscreenActivity.myTransposedLyrics[x] + "\n";
        }


        // Now that the chords have been changed, replace the myTransposedLyrics
        // into the file
        FullscreenActivity.mynewXML = null;
        FullscreenActivity.mynewXML = "";

        // Write the new improved XML file
        FullscreenActivity.mLyrics = FullscreenActivity.transposedLyrics;

        PopUpEditSongFragment.prepareSongXML();
        PopUpEditSongFragment.justSaveSongXML();

 /*       String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + PopUpEditSongFragment.parseToHTMLEntities(FullscreenActivity.mTitle.toString() + "</title>\n";
        myNEWXML += "  <author>" + FullscreenActivity.mAuthor + "</author>\n";
        myNEWXML += "  <copyright>" + FullscreenActivity.mCopyright + "</copyright>\n";
        myNEWXML += "  <presentation>" + FullscreenActivity.mPresentation + "</presentation>\n";
        myNEWXML += "  <hymn_number>" + FullscreenActivity.mHymnNumber + "</hymn_number>\n";
        myNEWXML += "  <capo print=\"" + FullscreenActivity.mCapoPrint + "\">" + FullscreenActivity.mCapo + "</capo>\n";
        myNEWXML += "  <tempo>" + FullscreenActivity.mTempo + "</tempo>\n";
        myNEWXML += "  <time_sig>" + FullscreenActivity.mTimeSig + "</time_sig>\n";
        myNEWXML += "  <duration>" + FullscreenActivity.mDuration + "</duration>\n";
        myNEWXML += "  <ccli>" + FullscreenActivity.mCCLI + "</ccli>\n";
        myNEWXML += "  <theme>" + FullscreenActivity.mTheme + "</theme>\n";
        myNEWXML += "  <alttheme>" + FullscreenActivity.mAltTheme + "</alttheme>\n";
        myNEWXML += "  <user1>" + FullscreenActivity.mUser1 + "</user1>\n";
        myNEWXML += "  <user2>" + FullscreenActivity.mUser2 + "</user2>\n";
        myNEWXML += "  <user3>" + FullscreenActivity.mUser3 + "</user3>\n";
        myNEWXML += "  <key>" + FullscreenActivity.mKey + "</key>\n";
        myNEWXML += "  <aka>" + FullscreenActivity.mAka + "</aka>\n";
        myNEWXML += "  <key_line>" + FullscreenActivity.mKeyLine + "</key_line>\n";
        myNEWXML += "  <books>" + FullscreenActivity.mBooks + "</books>\n";
        myNEWXML += "  <midi>" + FullscreenActivity.mMidi + "</midi>\n";
        myNEWXML += "  <midi_index>" + FullscreenActivity.mMidiIndex + "</midi_index>\n";
        myNEWXML += "  <pitch>" + FullscreenActivity.mPitch + "</pitch>\n";
        myNEWXML += "  <restrictions>" + FullscreenActivity.mRestrictions + "</restrictions>\n";
        myNEWXML += "  <notes>" + FullscreenActivity.mNotes + "</notes>\n";
        myNEWXML += "  <linked_songs>" + FullscreenActivity.mLinkedSongs + "</linked_songs>\n";
        myNEWXML += "  <pad_file>" + FullscreenActivity.mPadFile + "</pad_file>\n";
        myNEWXML += "  <custom_chords>" + FullscreenActivity.mCustomChords + "</custom_chords>\n";
        myNEWXML += "  <lyrics>" + FullscreenActivity.transposedLyrics + "</lyrics>\n";
        if (!FullscreenActivity.mExtraStuff1.isEmpty()) {
            myNEWXML += "  " + FullscreenActivity.mExtraStuff1 + "\n";
        }
        if (!FullscreenActivity.mExtraStuff2.isEmpty()) {
            myNEWXML += "  " + FullscreenActivity.mExtraStuff2 + "\n";
        }
        myNEWXML += "</song>";

        FullscreenActivity.mynewXML = myNEWXML;

        // Makes sure all & are replaced with &amp;
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;","&");
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&","&amp;");

        // Now write the modified song
        FileOutputStream overWrite;

        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            overWrite = new FileOutputStream(
                    FullscreenActivity.dir + "/" + FullscreenActivity.songfilename,
                    false);
        } else {
            overWrite = new FileOutputStream(
                    FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename,
                    false);
        }
        overWrite.write(FullscreenActivity.mynewXML.getBytes());
        overWrite.flush();
        overWrite.close();
*/
        FullscreenActivity.transposedLyrics = null;
        FullscreenActivity.transposedLyrics = "";
        Arrays.fill(FullscreenActivity.myTransposedLyrics, null);
        Arrays.fill(FullscreenActivity.myParsedLyrics, null);
        FullscreenActivity.myLyrics = null;
        FullscreenActivity.myLyrics = "";
        FullscreenActivity.mynewXML = null;
        FullscreenActivity.mynewXML = "";
        FullscreenActivity.myXML = null;
        FullscreenActivity.myXML = "";

        Preferences.savePreferences();
    }

    public static String keyToNumber(String key) {
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

    public static String chordToNumber1(String line) {
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

    public static String chordToNumber2(String line) {
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

    public static String chordToNumber3(String line) {
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

    public static String chordToNumber4(String line) {

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

    public static String transposeKey(String getkeynum, String direction, int transposetimes) {
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
        }

        if (direction.equals("-1")) {
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

    public static void transposeChords() {
        // Go through each line in turn
        for (int x = 0; x < FullscreenActivity.myTransposedLyrics.length; x++) {

            // Only do transposing if it is a chord line (starting with .)
            if (FullscreenActivity.myTransposedLyrics[x].indexOf(".") == 0) {
                if (FullscreenActivity.transposeDirection.equals("+1")) {
                    // Put the numbers up by one.
                    // Last step then fixes 12 to be 0

                    // Repeat this as often as required.
                    for (int repeatTranspose = 0; repeatTranspose < FullscreenActivity.transposeTimes; repeatTranspose++) {
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

                if (FullscreenActivity.transposeDirection.equals("-1")) {
                    // Put the numbers down by one.
                    // Move num 0 down to -1 (if it goes to 11 it will be moved
                    // later)
                    // Last step then fixes -1 to be 11

                    // Repeat this as often as required.
                    for (int repeatTranspose = 0; repeatTranspose < FullscreenActivity.transposeTimes; repeatTranspose++) {
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

    public static String numberToKey(String key) {
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

        if (key.equals("G#") && FullscreenActivity.prefChord_Aflat_Gsharp.equals("b")) {
            key = "Ab";
        } else if (key.equals("G#m") && FullscreenActivity.prefChord_Aflatm_Gsharpm.equals("b")) {
            key = "Abm";
        } else if (key.equals("A#") && FullscreenActivity.prefChord_Bflat_Asharp.equals("b")) {
            key = "Bb";
        } else if (key.equals("A#m") && FullscreenActivity.prefChord_Bflatm_Asharpm.equals("b")) {
            key = "Bbm";
        } else if (key.equals("C#") && FullscreenActivity.prefChord_Dflat_Csharp.equals("b")) {
            key = "Db";
        } else if (key.equals("C#m") && FullscreenActivity.prefChord_Dflatm_Csharpm.equals("b")) {
            key = "Dbm";
        } else if (key.equals("D#") && FullscreenActivity.prefChord_Eflat_Dsharp.equals("b")) {
            key = "Eb";
        } else if (key.equals("D#m") && FullscreenActivity.prefChord_Bflatm_Asharpm.equals("b")) {
            key = "Ebm";
        } else if (key.equals("F#") && FullscreenActivity.prefChord_Gflat_Fsharp.equals("b")) {
            key = "Gb";
        } else if (key.equals("F#m") && FullscreenActivity.prefChord_Gflatm_Fsharpm.equals("b")) {
            key = "Gbm";
        }

        return key;
    }

    public static String numberToChord1(String line) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (FullscreenActivity.switchsharpsflats) {
            if (FullscreenActivity.transposeStyle.equals("flats")) {
                line = useFlats1(line);
            } else {
                line = useSharps1(line);
            }

        } else {
            if (usesflats) {
                line = useFlats1(line);
            } else {
                line = useSharps1(line);
            }
        }

        // Replace the naturals
        line = useNaturals1(line);

        return line;
    }

    public static String capoNumberToChord1(String line) {
        if (capousesflats) {
            line = useFlats1(line);
        } else {
            line = useSharps1(line);
        }

        // Replace the naturals
        line = useNaturals1(line);

        return line;
    }

    public static String numberToChord2(String line) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (FullscreenActivity.switchsharpsflats) {
            if (FullscreenActivity.transposeStyle.equals("flats")) {
                line = useFlats2(line);
            } else {
                line = useSharps2(line);
            }

        } else {
            if (usesflats) {
                line = useFlats2(line);
            } else {
                line = useSharps2(line);
            }
        }

        // Replace the naturals
        line = useNaturals2(line);

        return line;
    }

    public static String capoNumberToChord2(String line) {
        if (capousesflats) {
            line = useFlats2(line);
        } else {
            line = useSharps2(line);
        }

        // Replace the naturals
        line = useNaturals2(line);

        return line;
    }

    public static String numberToChord3(String line) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (FullscreenActivity.switchsharpsflats) {
            if (FullscreenActivity.transposeStyle.equals("flats")) {
                line = useFlats3(line);
            } else {
                line = useSharps3(line);
            }

        } else {
            if (usesflats) {
                line = useFlats3(line);
            } else {
                line = useSharps3(line);
            }
        }

        // Replace the naturals
        line = useNaturals3(line);

        return line;
    }

    public static String capoNumberToChord3(String line) {
        if (capousesflats) {
            line = useFlats3(line);
        } else {
            line = useSharps3(line);
        }

        // Replace the naturals
        line = useNaturals3(line);

        return line;
    }

    public static String numberToChord4(String line) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (FullscreenActivity.switchsharpsflats) {
            if (FullscreenActivity.transposeStyle.equals("flats")) {
                line = useFlats4(line);
            } else {
                line = useSharps4(line);
            }

        } else {
            if (usesflats) {
                line = useFlats4(line);
            } else {
                line = useSharps4(line);
            }
        }

        // Replace the naturals
        line = useNaturals4(line);

        return line;
    }

    public static String capoNumberToChord4(String line) {
        if (capousesflats) {
            line = useFlats4(line);
        } else {
            line = useSharps4(line);
        }

        // Replace the naturals
        line = useNaturals4(line);

        return line;
    }

    public static boolean keyUsesFlats(String testkey) {

        boolean result;
        result = (testkey.equals("Ab") && FullscreenActivity.prefChord_Aflat_Gsharp.equals("b")) ||
                (testkey.equals("Bb") && FullscreenActivity.prefChord_Bflat_Asharp.equals("b")) ||
                (testkey.equals("Db") && FullscreenActivity.prefChord_Dflat_Csharp.equals("b")) ||
                (testkey.equals("Eb") && FullscreenActivity.prefChord_Eflat_Dsharp.equals("b")) ||
                (testkey.equals("Gb") && FullscreenActivity.prefChord_Gflat_Fsharp.equals("b")) ||
                (testkey.equals("Bbm") && FullscreenActivity.prefChord_Bflatm_Asharpm.equals("b")) ||
                (testkey.equals("Dbm") && FullscreenActivity.prefChord_Dflatm_Csharpm.equals("b")) ||
                (testkey.equals("Ebm") && FullscreenActivity.prefChord_Eflatm_Dsharpm.equals("b")) ||
                (testkey.equals("Gbm") && FullscreenActivity.prefChord_Gflatm_Fsharpm.equals("b")) ||
                testkey.equals("C") ||
                testkey.equals("F") ||
                testkey.equals("Dm") ||
                testkey.equals("Gm") ||
                testkey.equals("Cm");
        return result;
    }

    public static String useFlats1(String line) {
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords1a[z]);
        }
        return line;
    }

    public static String useFlats2(String line) {
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords2[z]);
        }
        return line;
    }

    public static String useFlats3(String line) {
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

    public static String useFlats4(String line) {
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords4[z]);
        }
        return line;
    }

    public static String useSharps1(String line) {
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords1a[z]);
        }
        return line;
    }

    public static String useSharps2(String line) {
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords2[z]);
        }
        return line;
    }

    public static String useSharps3(String line) {
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

    public static String useSharps4(String line) {
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords4[z]);
        }
        return line;
    }

    public static String useNaturals1(String line) {
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords1a[z]);
        }
        return line;
    }

    public static String useNaturals2(String line) {
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords2[z]);
        }
        return line;
    }

    public static String useNaturals3(String line) {
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

    public static String useNaturals4(String line) {
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords4[z]);
        }
        return line;
    }

    public static String capoTranspose() {

        int numtimes = Integer.parseInt(FullscreenActivity.mCapo);

        // Get the capokey if it hasn't been set
        if (FullscreenActivity.capokey==null && FullscreenActivity.mKey!=null) {
            FullscreenActivity.capokey = keyToNumber(FullscreenActivity.mKey);
            FullscreenActivity.capokey = transposeKey(FullscreenActivity.capokey,"-1",numtimes);
            FullscreenActivity.capokey = numberToKey(FullscreenActivity.capokey);
            // Decide if flats should be used
            capousesflats = keyUsesFlats(FullscreenActivity.capokey);
        }

        // Now we change the chords into numbers
        switch (FullscreenActivity.oldchordformat) {
            default:
                FullscreenActivity.temptranspChords = chordToNumber1(FullscreenActivity.temptranspChords);
                break;

            case "2":
                FullscreenActivity.temptranspChords = chordToNumber2(FullscreenActivity.temptranspChords);
                break;

            case "3":
                FullscreenActivity.temptranspChords = chordToNumber3(FullscreenActivity.temptranspChords);
                break;

            case "4":
                FullscreenActivity.temptranspChords = chordToNumber4(FullscreenActivity.temptranspChords);
                break;
        }

        // Try to do a sensible capo change.
        // Do a for loop for each capo chord changing it by one each time until the desired fret change
        for (int s = 0; s < numtimes; s++) {
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.1.$", "$.0.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.2.$", "$.1.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.3.$", "$.2.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.4.$", "$.3.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.5.$", "$.4.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.6.$", "$.5.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.7.$", "$.6.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.8.$", "$.7.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.9.$", "$.8.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.10.$", "$.9.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.11.$", "$.10.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.12.$", "$.11.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.0.$", "$.12.$");

            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.31.$", "$.30.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.32.$", "$.31.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.33.$", "$.32.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.34.$", "$.33.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.35.$", "$.34.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.36.$", "$.35.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.37.$", "$.36.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.38.$", "$.37.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.39.$", "$.38.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.40.$", "$.39.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.41.$", "$.40.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.42.$", "$.41.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.30.$", "$.42.$");

            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.51.$", "$.50.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.52.$", "$.51.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.53.$", "$.52.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.54.$", "$.53.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.55.$", "$.54.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.56.$", "$.55.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.57.$", "$.56.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.58.$", "$.57.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.59.$", "$.58.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.60.$", "$.59.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.61.$", "$.60.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.62.$", "$.61.$");
            FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.50.$", "$.62.$");
        }

        // Now convert the numbers back to the appropriate chords
        switch (FullscreenActivity.oldchordformat) {
            default:
                FullscreenActivity.temptranspChords = capoNumberToChord1(FullscreenActivity.temptranspChords);
                break;

            case "2":
                FullscreenActivity.temptranspChords = capoNumberToChord2(FullscreenActivity.temptranspChords);
                break;

            case "3":
                FullscreenActivity.temptranspChords = capoNumberToChord3(FullscreenActivity.temptranspChords);
                break;

            case "4":
                FullscreenActivity.temptranspChords = capoNumberToChord4(FullscreenActivity.temptranspChords);
                break;
        }

        return FullscreenActivity.temptranspChords;
    }

	public static void checkChordFormat() {
		FullscreenActivity.transposedLyrics = null;
		FullscreenActivity.transposedLyrics = "";
		FullscreenActivity.myTransposedLyrics = null;
		FullscreenActivity.myTransposedLyrics = FullscreenActivity.mLyrics.split("\n");

		FullscreenActivity.oldchordformat = FullscreenActivity.chordFormat;

		boolean contains_es_is = false;
		boolean contains_H = false;
		boolean contains_do = false;
        boolean contains_nash = false;

		// Check if the user is using the same chord format as the song
		// Go through the chord lines and look for clues
		for (int x = 0; x < FullscreenActivity.myTransposedLyrics.length; x++) {
			if (FullscreenActivity.myTransposedLyrics[x].indexOf(".")==0) {
				// Chord line
				if (FullscreenActivity.myTransposedLyrics[x].contains("es") || FullscreenActivity.myTransposedLyrics[x].contains("is") ||
						FullscreenActivity.myTransposedLyrics[x].contains(" a") || FullscreenActivity.myTransposedLyrics[x].contains(".a") ||
						FullscreenActivity.myTransposedLyrics[x].contains(" b") || FullscreenActivity.myTransposedLyrics[x].contains(".b") ||
						FullscreenActivity.myTransposedLyrics[x].contains(" h") || FullscreenActivity.myTransposedLyrics[x].contains(".h") ||
						FullscreenActivity.myTransposedLyrics[x].contains(" c") || FullscreenActivity.myTransposedLyrics[x].contains(".c") ||
						FullscreenActivity.myTransposedLyrics[x].contains(" d") || FullscreenActivity.myTransposedLyrics[x].contains(".d") ||
						FullscreenActivity.myTransposedLyrics[x].contains(" e") || FullscreenActivity.myTransposedLyrics[x].contains(".e") ||
						FullscreenActivity.myTransposedLyrics[x].contains(" f") || FullscreenActivity.myTransposedLyrics[x].contains(".f") ||
						FullscreenActivity.myTransposedLyrics[x].contains(" g") || FullscreenActivity.myTransposedLyrics[x].contains(".g"))	{
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
                }
			}
		}

		//int detected = 0;
		// Set the chord style detected
		if (contains_do && FullscreenActivity.alwaysPreferredChordFormat.equals("N")) {
			FullscreenActivity.oldchordformat="4";
			//detected = 3;
		} else if (contains_H && !contains_es_is && FullscreenActivity.alwaysPreferredChordFormat.equals("N")) {
			FullscreenActivity.oldchordformat="2";
			//detected = 1;
		} else if (contains_H || contains_es_is && FullscreenActivity.alwaysPreferredChordFormat.equals("N")) {
			FullscreenActivity.oldchordformat="3";
			//detected = 2;
		} else if (contains_nash && FullscreenActivity.alwaysPreferredChordFormat.equals("N")) {
            FullscreenActivity.oldchordformat="5";
            //detected = 4;
        } else if (FullscreenActivity.alwaysPreferredChordFormat.equals("N")){
			FullscreenActivity.oldchordformat="1";
			//detected = 0;
		}
		// Ok so the user chord format may not quite match the song - it might though!
	}

}