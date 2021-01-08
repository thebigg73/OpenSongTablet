package com.garethevans.church.opensongtablet.chords;

import android.content.Context;

import com.garethevans.church.opensongtablet.preferences.Preferences;

public class ChordDirectory {

    private String simplifyChords(Context c, Preferences preferences, String tempchord) {
        String chordtoworkon = tempchord;
        int myformat = preferences.getMyPreferenceInt(c,"chordFormat",1);
        if (myformat==2) {
            //European Bb = B and B = H
            chordtoworkon = chordtoworkon.replace("$Bb","$A#");
            chordtoworkon = chordtoworkon.replace("$B","$A#");
            chordtoworkon = chordtoworkon.replace("$H", "$B");
        } else if (myformat==3) {
            // Replace minors, flats and sharps
            chordtoworkon = chordtoworkon.replace("$As", "$Ab");
            chordtoworkon = chordtoworkon.replace("$Ais","$A#");
            chordtoworkon = chordtoworkon.replace("$as", "$Abm");
            chordtoworkon = chordtoworkon.replace("$ais","$A#m");
            chordtoworkon = chordtoworkon.replace("$a","$Am");

            chordtoworkon = chordtoworkon.replace("$Bes", "$A");
            chordtoworkon = chordtoworkon.replace("$Bis","$B");
            chordtoworkon = chordtoworkon.replace("$bes", "$Am");
            chordtoworkon = chordtoworkon.replace("$bis","$Bm");
            chordtoworkon = chordtoworkon.replace("$B","$Bb");
            chordtoworkon = chordtoworkon.replace("$b","$Bbm");

            chordtoworkon = chordtoworkon.replace("$Hs", "$Bb");
            chordtoworkon = chordtoworkon.replace("$His","$C");
            chordtoworkon = chordtoworkon.replace("$hs", "$Bbm");
            chordtoworkon = chordtoworkon.replace("$his","$Cm");
            chordtoworkon = chordtoworkon.replace("$H","$B");
            chordtoworkon = chordtoworkon.replace("$h","$Bm");

            chordtoworkon = chordtoworkon.replace("$Ces", "$B");
            chordtoworkon = chordtoworkon.replace("$Cis","$C#");
            chordtoworkon = chordtoworkon.replace("$ces", "$Bm");
            chordtoworkon = chordtoworkon.replace("$cis","$C#m");
            chordtoworkon = chordtoworkon.replace("$c","$Cm");

            chordtoworkon = chordtoworkon.replace("$Des", "$Db");
            chordtoworkon = chordtoworkon.replace("$Dis","$D#");
            chordtoworkon = chordtoworkon.replace("$des", "$Dbm");
            chordtoworkon = chordtoworkon.replace("$dis","$D#m");
            chordtoworkon = chordtoworkon.replace("$d","$Dm");

            chordtoworkon = chordtoworkon.replace("$Es", "$Eb");
            chordtoworkon = chordtoworkon.replace("$Eis","$F");
            chordtoworkon = chordtoworkon.replace("$es", "$Ebm");
            chordtoworkon = chordtoworkon.replace("$eis","$Fm");
            chordtoworkon = chordtoworkon.replace("$e","$Em");

            chordtoworkon = chordtoworkon.replace("$Fes", "$E");
            chordtoworkon = chordtoworkon.replace("$Fis","$F#");
            chordtoworkon = chordtoworkon.replace("$fes", "$Em");
            chordtoworkon = chordtoworkon.replace("$fis","$F#m");
            chordtoworkon = chordtoworkon.replace("$f","$Fm");

            chordtoworkon = chordtoworkon.replace("$Ges", "$Gb");
            chordtoworkon = chordtoworkon.replace("$Gis","$G#");
            chordtoworkon = chordtoworkon.replace("$ges", "$Gbm");
            chordtoworkon = chordtoworkon.replace("$gis","$G#m");
            chordtoworkon = chordtoworkon.replace("$g","$Gm");
        }

        // Now fix any silly chords that shouldn't exist
        chordtoworkon = chordtoworkon.replace("$B#", "$C");
        chordtoworkon = chordtoworkon.replace("$Cb", "$B");
        chordtoworkon = chordtoworkon.replace("$E#", "$F");
        chordtoworkon = chordtoworkon.replace("$Fb", "$E");


        // Now change any chord that ends with major or maj (not maj7)
        if (chordtoworkon.endsWith("maj")) {
            chordtoworkon = chordtoworkon.replace("maj", "");
        } else if (chordtoworkon.endsWith("major")) {
            chordtoworkon = chordtoworkon.replace("major", "");
        }

        // Replace min chords with m
        chordtoworkon = chordtoworkon.replace("min", "m");

        // Replace 7M chords with maj7
        chordtoworkon = chordtoworkon.replace("7M", "maj7");

        // Replace all flat chords with sharp chords
        chordtoworkon = chordtoworkon.replace("Ab", "G#");
        chordtoworkon = chordtoworkon.replace("Bb", "A#");
        chordtoworkon = chordtoworkon.replace("Db", "C#");
        chordtoworkon = chordtoworkon.replace("Eb", "D#");
        chordtoworkon = chordtoworkon.replace("Gb", "F#");

        // Replace all sus4 chords with sus
        chordtoworkon = chordtoworkon.replace("sus4", "sus");

        // Replace all sus2, add2 with 2
        chordtoworkon = chordtoworkon.replace("sus2", "2");
        chordtoworkon = chordtoworkon.replace("add2", "2");

        // Replace all sus9 chords with 9  add9 stay as add9
        chordtoworkon = chordtoworkon.replace("sus9", "9");

        // Replace all dim7 with dim
        chordtoworkon = chordtoworkon.replace("o7", "dim");
        chordtoworkon = chordtoworkon.replace("dim7", "dim");

        // Replace all m7/b5 with m7b5
        chordtoworkon = chordtoworkon.replace("m7/b5", "m7b5");

        // Replace all #5 and + with aug
        chordtoworkon = chordtoworkon.replace("#5", "aug");
        chordtoworkon = chordtoworkon.replace("+", "aug");

        // Since we are only using chords, we can ignore slash chords (bass notes)
        chordtoworkon = chordtoworkon.replace("/A#", "");
        chordtoworkon = chordtoworkon.replace("/C#", "");
        chordtoworkon = chordtoworkon.replace("/D#", "");
        chordtoworkon = chordtoworkon.replace("/F#", "");
        chordtoworkon = chordtoworkon.replace("/G#", "");
        chordtoworkon = chordtoworkon.replace("/A", "");
        chordtoworkon = chordtoworkon.replace("/B", "");
        chordtoworkon = chordtoworkon.replace("/C", "");
        chordtoworkon = chordtoworkon.replace("/D", "");
        chordtoworkon = chordtoworkon.replace("/E", "");
        chordtoworkon = chordtoworkon.replace("/F", "");
        chordtoworkon = chordtoworkon.replace("/G", "");

        // Now we can remove the $ sign
        chordtoworkon = chordtoworkon.replace("$", "");

        return chordtoworkon;
    }

    public String guitarChords(Context c, Preferences preferences, String chord) {

        String chordnotes;
        String chordtoworkon = simplifyChords(c,preferences,chord);

        // MAJOR CHORDS
        switch (chordtoworkon) {
            case "A":
                chordnotes = "002220";
                break;
            case "A#":
                chordnotes = "113331";
                break;
            case "B":
                chordnotes = "224442";
                break;
            case "C":
                chordnotes = "332010";
                break;
            case "C#":
                chordnotes = "113331_4_g_C#";
                break;
            case "D":
                chordnotes = "x00232";
                break;
            case "D#":
                chordnotes = "xx1343";
                break;
            case "E":
                chordnotes = "022100";
                break;
            case "F":
                chordnotes = "133211";
                break;
            case "F#":
                chordnotes = "244322";
                break;
            case "G":
                chordnotes = "320003";
                break;
            case "G#":
                chordnotes = "133211_4_g_G#";
                break;

            // MAJOR 7 CHORDS
            case "Amaj7":
                chordnotes = "002120";
                break;
            case "A#maj7":
                chordnotes = "113231";
                break;
            case "Bmaj7":
                chordnotes = "224342";
                break;
            case "Cmaj7":
                chordnotes = "332000";
                break;
            case "C#maj7":
                chordnotes = "113231_4_g_C#maj7";
                break;
            case "Dmaj7":
                chordnotes = "x00222";
                break;
            case "D#maj7":
                chordnotes = "xx1333";
                break;
            case "Emaj7":
                chordnotes = "021100";
                break;
            case "Fmaj7":
                chordnotes = "132211";
                break;
            case "F#maj7":
                chordnotes = "243322";
                break;
            case "Gmaj7":
                chordnotes = "320002";
                break;
            case "G#maj7":
                chordnotes = "132211_4_g_G#maj7";
                break;

            // DOMINANT 7 CHORDS
            case "A7":
                chordnotes = "002020";
                break;
            case "A#7":
                chordnotes = "113131";
                break;
            case "B7":
                chordnotes = "x21202";
                break;
            case "C7":
                chordnotes = "x32310";
                break;
            case "C#7":
                chordnotes = "113131_4_g_C#7";
                break;
            case "D7":
                chordnotes = "x00212";
                break;
            case "D#7":
                chordnotes = "xx1323";
                break;
            case "E7":
                chordnotes = "020100";
                break;
            case "F7":
                chordnotes = "131211";
                break;
            case "F#7":
                chordnotes = "242322";
                break;
            case "G7":
                chordnotes = "320001";
                break;
            case "G#7":
                chordnotes = "131211_4_g_G#7";
                break;

            // MAJOR 6 CHORDS
            case "A6":
                chordnotes = "002222";
                break;
            case "A#6":
                chordnotes = "x13333";
                break;
            case "B6":
                chordnotes = "x24444";
                break;
            case "C6":
                chordnotes = "x32210";
                break;
            case "C#6":
                chordnotes = "x13333_4_g_C#6";
                break;
            case "D6":
                chordnotes = "xx0202";
                break;
            case "D#6":
                chordnotes = "xx1313";
                break;
            case "E6":
                chordnotes = "022120";
                break;
            case "F6":
                chordnotes = "xx3231";
                break;
            case "F#6":
                chordnotes = "243342";
                break;
            case "G6":
                chordnotes = "320000";
                break;
            case "G#6":
                chordnotes = "xx3231_4_g_G#6";
                break;

            // MINOR CHORDS
            case "Am":
                chordnotes = "002210";
                break;
            case "A#m":
                chordnotes = "113321";
                break;
            case "Bm":
                chordnotes = "224432";
                break;
            case "Cm":
                chordnotes = "113321_3_g_Cm";
                break;
            case "C#m":
                chordnotes = "113321_4_g_C#m";
                break;
            case "Dm":
                chordnotes = "x00231";
                break;
            case "D#m":
                chordnotes = "xx1342";
                break;
            case "Em":
                chordnotes = "022000";
                break;
            case "Fm":
                chordnotes = "133111";
                break;
            case "F#m":
                chordnotes = "244222";
                break;
            case "Gm":
                chordnotes = "133111_3_g_Gm";
                break;
            case "G#m":
                chordnotes = "133111_4_g_G#m";
                break;

            // MINOR 6 CHORDS
            case "Am6":
                chordnotes = "x02212";
                break;
            case "A#m6":
                chordnotes = "x13021";
                break;
            case "Bm6":
                chordnotes = "x20102";
                break;
            case "Cm6":
                chordnotes = "x31213";
                break;
            case "C#m6":
                chordnotes = "x42124";
                break;
            case "Dm6":
                chordnotes = "xx0201";
                break;
            case "D#m6":
                chordnotes = "xx1312";
                break;
            case "Em6":
                chordnotes = "022020";
                break;
            case "Fm6":
                chordnotes = "133131";
                break;
            case "F#m6":
                chordnotes = "244242";
                break;
            case "Gm6":
                chordnotes = "310030";
                break;
            case "G#m6":
                chordnotes = "133131_4_g_G#m6";
                break;

            // MINOR 7 CHORDS
            case "Am7":
                chordnotes = "002010";
                break;
            case "A#m7":
                chordnotes = "113121";
                break;
            case "Bm7":
                chordnotes = "224232";
                break;
            case "Cm7":
                chordnotes = "335343";
                break;
            case "C#m7":
                chordnotes = "113121_4_g_C#m7";
                break;
            case "Dm7":
                chordnotes = "x00211";
                break;
            case "D#m7":
                chordnotes = "xx1322";
                break;
            case "Em7":
                chordnotes = "020000";
                break;
            case "Fm7":
                chordnotes = "131111";
                break;
            case "F#m7":
                chordnotes = "242222";
                break;
            case "Gm7":
                chordnotes = "353333";
                break;
            case "G#m7":
                chordnotes = "131111_4_g_G#m7";
                break;

            // SUS (SUS4) CHORDS
            case "Asus":
                chordnotes = "002230";
                break;
            case "A#sus":
                chordnotes = "113341";
                break;
            case "Bsus":
                chordnotes = "224452";
                break;
            case "Csus":
                chordnotes = "332011";
                break;
            case "C#sus":
                chordnotes = "113341_4_g_C#sus";
                break;
            case "Dsus":
                chordnotes = "x00233";
                break;
            case "D#sus":
                chordnotes = "xx1344";
                break;
            case "Esus":
                chordnotes = "022200";
                break;
            case "Fsus":
                chordnotes = "133311";
                break;
            case "F#sus":
                chordnotes = "244422";
                break;
            case "Gsus":
                chordnotes = "320013";
                break;
            case "G#sus":
                chordnotes = "133311_4_g_G#sus";
                break;

            // SUS2 / 2 CHORDS
            case "A2":
                chordnotes = "x02200";
                break;
            case "A#2":
                chordnotes = "x13311";
                break;
            case "B2":
                chordnotes = "x24422";
                break;
            case "C2":
                chordnotes = "x30033";
                break;
            case "C#2":
                chordnotes = "x13311_4_g_C#2";
                break;
            case "D2":
                chordnotes = "xx0230";
                break;
            case "D#2":
                chordnotes = "xx1341";
                break;
            case "E2":
                chordnotes = "xx2452";
                break;
            case "F2":
                chordnotes = "xx3011";
                break;
            case "F#2":
                chordnotes = "xx4122";
                break;
            case "G2":
                chordnotes = "300033";
                break;
            case "G#2":
                chordnotes = "411144";
                break;

            // 9 CHORDS
            case "A9":
                chordnotes = "21212x_4_g_A9";
                break;
            case "A#9":
                chordnotes = "x10111";
                break;
            case "B9":
                chordnotes = "x2122x";
                break;
            case "C9":
                chordnotes = "x3233x";
                break;
            case "C#9":
                chordnotes = "x2122x_3_g_C#9";
                break;
            case "D9":
                chordnotes = "x2122x_4_g_D9";
                break;
            case "D#9":
                chordnotes = "xx1021";
                break;
            case "E9":
                chordnotes = "020102";
                break;
            case "F9":
                chordnotes = "101011";
                break;
            case "F#9":
                chordnotes = "21212x";
                break;
            case "G9":
                chordnotes = "32323x";
                break;
            case "G#9":
                chordnotes = "21212x_3_g_G#9";
                break;

            // add9 CHORDS
            case "Aadd9":
                chordnotes = "54242x";
                break;
            case "A#add9":
                chordnotes = "x10311";
                break;
            case "Badd9":
                chordnotes = "43131x_4_g_Badd9";
                break;
            case "Cadd9":
                chordnotes = "x32030";
                break;
            case "C#add9":
                chordnotes = "x43141";
                break;
            case "Dadd9":
                chordnotes = "x54252";
                break;
            case "D#add9":
                chordnotes = "x43141_3_g_D#add9";
                break;
            case "Eadd9":
                chordnotes = "022102";
                break;
            case "Fadd9":
                chordnotes = "103011";
                break;
            case "F#add9":
                chordnotes = "xx4324";
                break;
            case "Gadd9":
                chordnotes = "320203";
                break;
            case "G#add9":
                chordnotes = "43131x";
                break;

            // DIMINISHED 7 CHORDS
            case "Adim":
                chordnotes = "x01212";
                break;
            case "A#dim":
                chordnotes = "xx2323";
                break;
            case "Bdim":
                chordnotes = "xx3434";
                break;
            case "Cdim":
                chordnotes = "xx1212";
                break;
            case "C#dim":
                chordnotes = "xx2323";
                break;
            case "Ddim":
                chordnotes = "xx3434";
                break;
            case "D#dim":
                chordnotes = "xx1212";
                break;
            case "Edim":
                chordnotes = "xx2323";
                break;
            case "Fdim":
                chordnotes = "xx3434";
                break;
            case "F#dim":
                chordnotes = "xx1212";
                break;
            case "Gdim":
                chordnotes = "xx2323";
                break;
            case "G#dim":
                chordnotes = "xx3434";
                break;

            // MINOR 7 FLAT 5 (HALF DIMINISHED)
            case "Am7b5":
                chordnotes = "x01013";
                break;
            case "A#m7b5":
                chordnotes = "112124";
                break;
            case "Bm7b5":
                chordnotes = "112124_2_g_Bm7b5";
                break;
            case "Cm7b5":
                chordnotes = "112124_3_g_Cm7b5";
                break;
            case "C#m7b5":
                chordnotes = "112124_4_g_C#m7b5";
                break;
            case "Dm7b5":
                chordnotes = "xx0111";
                break;
            case "D#m7b5":
                chordnotes = "xx1222";
                break;
            case "Em7b5":
                chordnotes = "010030";
                break;
            case "Fm7b5":
                chordnotes = "121141";
                break;
            case "F#m7b5":
                chordnotes = "121141_2_g_F#m7b5";
                break;
            case "Gm7b5":
                chordnotes = "121141_3_g_Gm7b5";
                break;
            case "G#m7b5":
                chordnotes = "121141_4_g_G#m7b5";
                break;

            // AUGMENTED (#5)
            case "Aaug":
                chordnotes = "x03221";
                break;
            case "A#aug":
                chordnotes = "x14332";
                break;
            case "Baug":
                chordnotes = "x14332_2_g_Baug";
                break;
            case "Caug":
                chordnotes = "x32110";
                break;
            case "C#aug":
                chordnotes = "x14332_4_g_C#aug";
                break;
            case "Daug":
                chordnotes = "xx0332";
                break;
            case "D#aug":
                chordnotes = "xx1443";
                break;
            case "Eaug":
                chordnotes = "0321x0";
                break;
            case "Faug":
                chordnotes = "143221";
                break;
            case "F#aug":
                chordnotes = "143221_2_g_F#aug";
                break;
            case "Gaug":
                chordnotes = "321003";
                break;
            case "G#aug":
                chordnotes = "143221_4_g_G#aug";
                break;
            default:
                chordnotes = "xxxxxx";
                break;
        }

        // Standard guitar chords all start with g_ (guitar) and end with _0 (fret to start with)
        if (!chordnotes.contains("_")) {
            chordnotes = chordnotes + "_0_g_"+chordtoworkon;
        }
       return chordnotes;
    }

    public String ukuleleChords(Context c, Preferences preferences, String chord) {

        String chordnotes;
        String chordtoworkon = simplifyChords(c,preferences,chord);

        // MAJOR CHORDS
        switch (chordtoworkon) {
            case "A":
            case "F#m7":
                chordnotes = "2100";
                break;
            case "A#":
                chordnotes = "3211";
                break;
            case "B":
                chordnotes = "4322";
                break;
            case "C":
                chordnotes = "0003";
                break;
            case "C#":
                chordnotes = "1114";
                break;
            case "D":
                chordnotes = "2220";
                break;
            case "D#":
                chordnotes = "3331";
                break;
            case "E":
                chordnotes = "1402";
                break;
            case "F":
                chordnotes = "2010";
                break;
            case "F#":
                chordnotes = "3121";
                break;
            case "G":
                chordnotes = "0232";
                break;
            case "G#":
                chordnotes = "1343";
                break;

            // MAJOR 7 CHORDS
            case "Amaj7":
                chordnotes = "1100";
                break;
            case "A#maj7":
                chordnotes = "2211";
                break;
            case "Bmaj7":
                chordnotes = "3322";
                break;
            case "Cmaj7":
                chordnotes = "0002";
                break;
            case "C#maj7":
                chordnotes = "1113";
                break;
            case "Dmaj7":
                chordnotes = "2224";
                break;
            case "D#maj7":
                chordnotes = "3335";
                break;
            case "Emaj7":
                chordnotes = "1302";
                break;
            case "Fmaj7":
                chordnotes = "2413";
                break;
            case "F#maj7":
                chordnotes = "3524";
                break;
            case "Gmaj7":
                chordnotes = "0222";
                break;
            case "G#maj7":
                chordnotes = "1333";
                break;

            // DOMINANT 7 CHORDS
            case "A7":
                chordnotes = "0100";
                break;
            case "A#7":
                chordnotes = "1211";
                break;
            case "B7":
                chordnotes = "2322";
                break;
            case "C7":
                chordnotes = "0001";
                break;
            case "C#7":
                chordnotes = "1112";
                break;
            case "D7":
                chordnotes = "2020";
                break;
            case "D#7":
                chordnotes = "3131";
                break;
            case "E7":
                chordnotes = "1202";
                break;
            case "F7":
                chordnotes = "2313";
                break;
            case "F#7":
                chordnotes = "3424";
                break;
            case "G7":
                chordnotes = "0212";
                break;
            case "G#7":
                chordnotes = "1323";
                break;

            // MAJOR 6 CHORDS
            case "A6":
                chordnotes = "2424";
                break;
            case "A#6":
                chordnotes = "1313_3_u_A#6";
                break;
            case "B6":
                chordnotes = "1313_4_u_B6";
                break;
            case "C6":
                chordnotes = "0000";
                break;
            case "C#6":
                chordnotes = "1111";
                break;
            case "D6":
                chordnotes = "2222";
                break;
            case "D#6":
                chordnotes = "1111_3_u_D#6";
                break;
            case "E6":
                chordnotes = "1102";
                break;
            case "F6":
                chordnotes = "2213";
                break;
            case "F#6":
                chordnotes = "3324";
                break;
            case "G6":
                chordnotes = "0202";
                break;
            case "G#6":
                chordnotes = "1313";
                break;

            // MINOR CHORDS
            case "Am":
                chordnotes = "2000";
                break;
            case "A#m":
                chordnotes = "3111";
                break;
            case "Bm":
                chordnotes = "4222";
                break;
            case "Cm":
                chordnotes = "0333";
                break;
            case "C#m":
                chordnotes = "1444";
                break;
            case "Dm":
                chordnotes = "2210";
                break;
            case "D#m":
                chordnotes = "3321";
                break;
            case "Em":
                chordnotes = "0402";
                break;
            case "Fm":
                chordnotes = "1513";
                break;
            case "F#m":
                chordnotes = "2120";
                break;
            case "Gm":
                chordnotes = "0231";
                break;
            case "G#m":
                chordnotes = "1342";
                break;

            // MINOR 7 CHORDS
            case "Am7":
                chordnotes = "2030";
                break;
            case "A#m7":
                chordnotes = "3141";
                break;
            case "Bm7":
                chordnotes = "4252";
                break;
            case "Cm7":
                chordnotes = "0332";
                break;
            case "C#m7":
                chordnotes = "1443";
                break;
            case "Dm7":
                chordnotes = "2010";
                break;
            case "D#m7":
                chordnotes = "3121";
                break;
            case "Em7":
                chordnotes = "0202";
                break;
            case "Fm7":
                chordnotes = "1313";
                break;
            // F#m7 merged with A
            case "Gm7":
                chordnotes = "0211";
                break;
            case "G#m7":
                chordnotes = "1322";
                break;

            // MINOR 6 CHORDS
            case "Am6":
                chordnotes = "2423";
                break;
            case "A#m6":
                chordnotes = "0111";
                break;
            case "Bm6":
                chordnotes = "1222";
                break;
            case "Cm6":
                chordnotes = "2333";
                break;
            case "C#m6":
                chordnotes = "1101";
                break;
            case "Dm6":
                chordnotes = "2212";
                break;
            case "D#m6":
                chordnotes = "3323";
                break;
            case "Em6":
                chordnotes = "0102";
                break;
            case "Fm6":
                chordnotes = "1213";
                break;
            case "F#m6":
                chordnotes = "2324";
                break;
            case "Gm6":
                chordnotes = "0201";
                break;
            case "G#m6":
                chordnotes = "1312";
                break;

            // SUS (SUS4) CHORDS
            case "Asus":
                chordnotes = "2200";
                break;
            case "A#sus":
                chordnotes = "3311";
                break;
            case "Bsus":
                chordnotes = "4422";
                break;
            case "Csus":
                chordnotes = "0013";
                break;
            case "C#sus":
                chordnotes = "1124";
                break;
            case "Dsus":
                chordnotes = "0230";
                break;
            case "D#sus":
                chordnotes = "1341";
                break;
            case "Esus":
                chordnotes = "2452";
                break;
            case "Fsus":
                chordnotes = "3011";
                break;
            case "F#sus":
                chordnotes = "4122";
                break;
            case "Gsus":
                chordnotes = "0233";
                break;
            case "G#sus":
                chordnotes = "1344";
                break;

            // SUS2 / 2 CHORDS
            case "A2":
                chordnotes = "2452";
                break;
            case "A#2":
                chordnotes = "3011";
                break;
            case "B2":
                chordnotes = "4122";
                break;
            case "C2":
                chordnotes = "0233";
                break;
            case "C#2":
                chordnotes = "1344";
                break;
            case "D2":
                chordnotes = "2200";
                break;
            case "D#2":
                chordnotes = "3311";
                break;
            case "E2":
                chordnotes = "4422";
                break;
            case "F2":
                chordnotes = "0013";
                break;
            case "F#2":
                chordnotes = "1124";
                break;
            case "G2":
                chordnotes = "0230";
                break;
            case "G#2":
                chordnotes = "1341";
                break;

            // 9 CHORDS
            case "A9":
                chordnotes = "2132";
                break;
            case "A#9":
                chordnotes = "3243";
                break;
            case "B9":
                chordnotes = "4354";
                break;
            case "C9":
                chordnotes = "3001";
                break;
            case "C#9":
                chordnotes = "1312";
                break;
            case "D9":
                chordnotes = "5424";
                break;
            case "D#9":
                chordnotes = "0111";
                break;
            case "E9":
                chordnotes = "1222";
                break;
            case "F9":
                chordnotes = "2333";
                break;
            case "F#9":
                chordnotes = "3444";
                break;
            case "G9":
                chordnotes = "0552";
                break;
            case "G#9":
                chordnotes = "1021";
                break;

            // add9 CHORDS
            case "Aadd9":
                chordnotes = "2102";
                break;
            case "A#add9":
                chordnotes = "3213";
                break;
            case "Badd9":
                chordnotes = "4324";
                break;
            case "Cadd9":
                chordnotes = "0203";
                break;
            case "C#add9":
                chordnotes = "1314";
                break;
            case "Dadd9":
                chordnotes = "2425";
                break;
            case "D#add9":
                chordnotes = "0311";
                break;
            case "Eadd9":
                chordnotes = "1422";
                break;
            case "Fadd9":
                chordnotes = "0010";
                break;
            case "F#add9":
                chordnotes = "1121";
                break;
            case "Gadd9":
                chordnotes = "0252";
                break;
            case "G#add9":
                chordnotes = "2232";
                break;

            // DIMINISHED 7 CHORDS
            case "Adim":
                chordnotes = "2323";
                break;
            case "A#dim":
                chordnotes = "0101";
                break;
            case "Bdim":
                chordnotes = "1212";
                break;
            case "Cdim":
                chordnotes = "2323";
                break;
            case "C#dim":
                chordnotes = "0101";
                break;
            case "Ddim":
                chordnotes = "1212";
                break;
            case "D#dim":
                chordnotes = "2323";
                break;
            case "Edim":
                chordnotes = "0101";
                break;
            case "Fdim":
                chordnotes = "1212";
                break;
            case "F#dim":
                chordnotes = "2323";
                break;
            case "Gdim":
                chordnotes = "0101";
                break;
            case "G#dim":
                chordnotes = "1212";
                break;

            // MINOR 7 FLAT 5 (HALF DIMINISHED)
            case "Am7b5":
                chordnotes = "2333";
                break;
            case "A#m7b5":
                chordnotes = "1101";
                break;
            case "Bm7b5":
                chordnotes = "2212";
                break;
            case "Cm7b5":
                chordnotes = "3323";
                break;
            case "C#m7b5":
                chordnotes = "0102";
                break;
            case "Dm7b5":
                chordnotes = "1213";
                break;
            case "D#m7b5":
                chordnotes = "2324";
                break;
            case "Em7b5":
                chordnotes = "0201";
                break;
            case "Fm7b5":
                chordnotes = "1312";
                break;
            case "F#m7b5":
                chordnotes = "2423";
                break;
            case "Gm7b5":
                chordnotes = "0111";
                break;
            case "G#m7b5":
                chordnotes = "1222";
                break;

            // AUGMENTED (#5)
            case "Aaug":
                chordnotes = "2114";
                break;
            case "A#aug":
                chordnotes = "3221";
                break;
            case "Baug":
                chordnotes = "4332";
                break;
            case "Caug":
                chordnotes = "1003";
                break;
            case "C#aug":
                chordnotes = "2110";
                break;
            case "Daug":
                chordnotes = "3221";
                break;
            case "D#aug":
                chordnotes = "0332";
                break;
            case "Eaug":
                chordnotes = "1003";
                break;
            case "Faug":
                chordnotes = "2110";
                break;
            case "F#aug":
                chordnotes = "3221";
                break;
            case "Gaug":
                chordnotes = "0332";
                break;
            case "G#aug":
                chordnotes = "1003";
                break;
            default:
                chordnotes = "xxxx";
                break;
        }

        // Standard ukulele chords all start with u_ (ukulele) and end with _0 (fret to start with)
        if (!chordnotes.contains("_")) {
            chordnotes = chordnotes + "_0_u_"+simplifyChords(c,preferences,chord);
        }

        return chordnotes;
    }

    public String mandolinChords(Context c, Preferences preferences, String chord) {

        String chordnotes;
        String chordtoworkon = simplifyChords(c,preferences,chord);

        // MAJOR CHORDS
        switch (chordtoworkon) {
            case "A":
                chordnotes = "2245";
                break;
            case "A#":
                chordnotes = "1134_3_m_A#";
                break;
            case "B":
                chordnotes = "1134_4_m_B";
                break;
            case "C":
                chordnotes = "0230";
                break;
            case "C#":
                chordnotes = "1341";
                break;
            case "D":
                chordnotes = "2002";
                break;
            case "D#":
                chordnotes = "0503";
                break;
            case "E":
                chordnotes = "1220";
                break;
            case "F":
                chordnotes = "2331";
                break;
            case "F#":
                chordnotes = "3442";
                break;
            case "G":
                chordnotes = "0023";
                break;
            case "G#":
                chordnotes = "1134";
                break;

            // MAJOR 7 CHORDS
            case "Amaj7":
                chordnotes = "2244";
                break;
            case "A#maj7":
                chordnotes = "3355";
                break;
            case "Bmaj7":
                chordnotes = "1133_4_m_Bmaj7";
                break;
            case "Cmaj7":
                chordnotes = "4230";
                break;
            case "C#maj7":
                chordnotes = "5341";
                break;
            case "Dmaj7":
                chordnotes = "2042";
                break;
            case "D#maj7":
                chordnotes = "3153";
                break;
            case "Emaj7":
                chordnotes = "1120";
                break;
            case "Fmaj7":
                chordnotes = "5300";
                break;
            case "F#maj7":
                chordnotes = "3342";
                break;
            case "Gmaj7":
                chordnotes = "0022";
                break;
            case "G#maj7":
                chordnotes = "1133";
                break;

            // DOMINANT 7 CHORDS
            case "A7":
                chordnotes = "2243";
                break;
            case "A#7":
                chordnotes = "3354";
                break;
            case "B7":
                chordnotes = "1132_4_m_B7";
                break;
            case "C7":
                chordnotes = "1132_5_m_C7";
                break;
            case "C#7":
                chordnotes = "1132_6_m_C#7";
                break;
            case "D7":
                chordnotes = "2032";
                break;
            case "D#7":
                chordnotes = "3143";
                break;
            case "E7":
                chordnotes = "1024";
                break;
            case "F7":
                chordnotes = "2135";
                break;
            case "F#7":
                chordnotes = "2135_2_m_F#7";
                break;
            case "G7":
                chordnotes = "0021";
                break;
            case "G#7":
                chordnotes = "1132";
                break;

            // MAJOR 6 CHORDS
            case "A6":
                chordnotes = "2202";
                break;
            case "A#6":
                chordnotes = "3313";
                break;
            case "B6":
                chordnotes = "4424";
                break;
            case "C6":
                chordnotes = "2230";
                break;
            case "C#6":
                chordnotes = "3341";
                break;
            case "D6":
                chordnotes = "2022";
                break;
            case "D#6":
                chordnotes = "3133";
                break;
            case "E6":
                chordnotes = "4244";
                break;
            case "F6":
                chordnotes = "3052";
                break;
            case "F#6":
                chordnotes = "2122_5_m_F#6";
                break;
            case "G6":
                chordnotes = "0020";
                break;
            case "G#6":
                chordnotes = "1131";
                break;

            // MINOR CHORDS
            case "Am":
                chordnotes = "2235";
                break;
            case "A#m":
                chordnotes = "1124_3_m_A#m";
                break;
            case "Bm":
                chordnotes = "1124_4_m_Bm";
                break;
            case "Cm":
                chordnotes = "3341_3_m_Cm";
                break;
            case "C#m":
                chordnotes = "3341_4_m_C#m";
                break;
            case "Dm":
                chordnotes = "2001";
                break;
            case "D#m":
                chordnotes = "3112";
                break;
            case "Em":
                chordnotes = "0220";
                break;
            case "Fm":
                chordnotes = "1331";
                break;
            case "F#m":
                chordnotes = "2442";
                break;
            case "Gm":
                chordnotes = "0013";
                break;
            case "G#m":
                chordnotes = "1124";
                break;

            // MINOR 7 CHORDS
            case "Am7":
                chordnotes = "2233";
                break;
            case "A#m7":
                chordnotes = "3344";
                break;
            case "Bm7":
                chordnotes = "4455";
                break;
            case "Cm7":
                chordnotes = "1122_5_m_Cm7";
                break;
            case "C#m7":
                chordnotes = "1122_6_m_C#m7";
                break;
            case "Dm7":
                chordnotes = "2031";
                break;
            case "D#m7":
                chordnotes = "3142";
                break;
            case "Em7":
                chordnotes = "4253";
                break;
            case "Fm7":
                chordnotes = "3142_3_m_Fm7";
                break;
            case "F#m7":
                chordnotes = "3142_4_m_F#m7";
                break;
            case "Gm7":
                chordnotes = "0011";
                break;
            case "G#m7":
                chordnotes = "1122";
                break;

            // MINOR 6 CHORDS
            case "Am6":
                chordnotes = "2232";
                break;
            case "A#m6":
                chordnotes = "3343";
                break;
            case "Bm6":
                chordnotes = "4454";
                break;
            case "Cm6":
                chordnotes = "1121_5_m_Cm6";
                break;
            case "C#m6":
                chordnotes = "1121_6_m_C#m6";
                break;
            case "Dm6":
                chordnotes = "2021";
                break;
            case "D#m6":
                chordnotes = "3132";
                break;
            case "Em6":
                chordnotes = "4243";
                break;
            case "Fm6":
                chordnotes = "3132_3_m_Fm6";
                break;
            case "F#m6":
                chordnotes = "3132_4_m_F#m6";
                break;
            case "Gm6":
                chordnotes = "0010";
                break;
            case "G#m6":
                chordnotes = "1121";
                break;

            // SUS (SUS4) CHORDS
            case "Asus":
                chordnotes = "2255";
                break;
            case "A#sus":
                chordnotes = "1144_3_m_A#sus";
                break;
            case "Bsus":
                chordnotes = "1144_4_m_Bsus";
                break;
            case "Csus":
                chordnotes = "0330";
                break;
            case "C#sus":
                chordnotes = "1441";
                break;
            case "Dsus":
                chordnotes = "2003";
                break;
            case "D#sus":
                chordnotes = "3114";
                break;
            case "Esus":
                chordnotes = "4225";
                break;
            case "Fsus":
                chordnotes = "3331";
                break;
            case "F#sus":
                chordnotes = "4442";
                break;
            case "Gsus":
                chordnotes = "0033";
                break;
            case "G#sus":
                chordnotes = "1144";
                break;

            // SUS2 / 2 CHORDS
            case "A2":
                chordnotes = "2225";
                break;
            case "A#2":
                chordnotes = "3331";
                break;
            case "B2":
                chordnotes = "4442";
                break;
            case "C2":
                chordnotes = "5033";
                break;
            case "C#2":
                chordnotes = "3331_4_m_C#2";
                break;
            case "D2":
                chordnotes = "2000";
                break;
            case "D#2":
                chordnotes = "3111";
                break;
            case "E2":
                chordnotes = "4222";
                break;
            case "F2":
                chordnotes = "5333";
                break;
            case "F#2":
                chordnotes = "3111_4_m_F#2";
                break;
            case "G2":
                chordnotes = "0003";
                break;
            case "G#2":
                chordnotes = "1114";
                break;

            // 9 CHORDS
            case "A9":
                chordnotes = "4245";
                break;
            case "A#9":
                chordnotes = "3031";
                break;
            case "B9":
                chordnotes = "4142";
                break;
            case "C9":
                chordnotes = "5030";
                break;
            case "C#9":
                chordnotes = "4142_3_m_C#9";
                break;
            case "D9":
                chordnotes = "4100_4_m_D9";
                break;
            case "D#9":
                chordnotes = "4142_5_m_D#9";
                break;
            case "E9":
                chordnotes = "4140_6_m_E9";
                break;
            case "F9":
                chordnotes = "x303";
                break;
            case "F#9":
                chordnotes = "x414";
                break;
            case "G9":
                chordnotes = "0325";
                break;
            case "G#9":
                chordnotes = "x414_3_m_G#9";
                break;

            // add9 CHORDS
            case "Aadd9":
                chordnotes = "4245";
                break;
            case "A#add9":
                chordnotes = "3031";
                break;
            case "Badd9":
                chordnotes = "4142";
                break;
            case "Cadd9":
                chordnotes = "5030";
                break;
            case "C#add9":
                chordnotes = "4142_3_m_C#9";
                break;
            case "Dadd9":
                chordnotes = "4100_4_m_D9";
                break;
            case "D#add9":
                chordnotes = "4142_5_m_D#9";
                break;
            case "Eadd9":
                chordnotes = "4140_6_m_E9";
                break;
            case "Fadd9":
                chordnotes = "x303";
                break;
            case "F#add9":
                chordnotes = "x414";
                break;
            case "Gadd9":
                chordnotes = "0325";
                break;
            case "G#add9":
                chordnotes = "x414_3_m_G#9";
                break;

            // DIMINISHED 7 CHORDS
            case "Adim":
                chordnotes = "2132";
                break;
            case "A#dim":
                chordnotes = "3243";
                break;
            case "Bdim":
                chordnotes = "1021";
                break;
            case "Cdim":
                chordnotes = "2132";
                break;
            case "C#dim":
                chordnotes = "3243";
                break;
            case "Ddim":
                chordnotes = "1021";
                break;
            case "D#dim":
                chordnotes = "2132";
                break;
            case "Edim":
                chordnotes = "3241";
                break;
            case "Fdim":
                chordnotes = "1021";
                break;
            case "F#dim":
                chordnotes = "2132";
                break;
            case "Gdim":
                chordnotes = "3243";
                break;
            case "G#dim":
                chordnotes = "1021";
                break;

            // MINOR 7 FLAT 5 (HALF DIMINISHED)
            case "Am7b5":
                chordnotes = "2133";
                break;
            case "A#m7b5":
                chordnotes = "3244";
                break;
            case "Bm7b5":
                chordnotes = "2133_3_m_Bm7b6";
                break;
            case "Cm7b5":
                chordnotes = "2133_4_m_Cm7b5";
                break;
            case "C#m7b5":
                chordnotes = "2133_5_m_C#m7b5";
                break;
            case "Dm7b5":
                chordnotes = "1031";
                break;
            case "D#m7b5":
                chordnotes = "2142";
                break;
            case "Em7b5":
                chordnotes = "3253";
                break;
            case "Fm7b5":
                chordnotes = "5112_6_m_Fm7b5";
                break;
            case "F#m7b5":
                chordnotes = "5112_7_m_F#m7b5";
                break;
            case "Gm7b5":
                chordnotes = "0311";
                break;
            case "G#m7b5":
                chordnotes = "1022";
                break;

            // AUGMENTED (#5)
            case "Aaug":
                chordnotes = "2341";
                break;
            case "A#aug":
                chordnotes = "3452";
                break;
            case "Baug":
                chordnotes = "4123";
                break;
            case "Caug":
                chordnotes = "5234";
                break;
            case "C#aug":
                chordnotes = "4123_3_m_C#aug";
                break;
            case "Daug":
                chordnotes = "3012";
                break;
            case "D#aug":
                chordnotes = "4123_5_m_D#aug";
                break;
            case "Eaug":
                chordnotes = "5234";
                break;
            case "Faug":
                chordnotes = "4123_7_m_Faug";
                break;
            case "F#aug":
                chordnotes = "4123_8_m_F#aug";
                break;
            case "Gaug":
                chordnotes = "0123";
                break;
            case "G#aug":
                chordnotes = "1230";
                break;
            default:
                chordnotes = "xxxx";
                break;
        }

        // Standard mandolin chords all start with m_ (mandolin) and end with _0 (fret to start with)
        if (!chordnotes.contains("_")) {
            chordnotes = chordnotes + "_0_m_"+chordtoworkon;
        }

        return chordnotes;
    }

    public String cavaquinhoChords(Context c, Preferences preferences, String chord) {

        String chordnotes;
        String chordtoworkon = simplifyChords(c,preferences,chord);

        // MAJOR CHORDS
        switch (chordtoworkon) {
            case "A":
                chordnotes = "2222";
                break;
            case "A#":
                chordnotes = "3333";
                break;
            case "B":
                chordnotes = "4444";
                break;
            case "C":
                chordnotes = "2012";
                break;
            case "C#":
                chordnotes = "3123";
                break;
            case "D":
                chordnotes = "0234";
                break;
            case "D#":
                chordnotes = "5345";
                break;
            case "E":
                chordnotes = "2102";
                break;
            case "F":
                chordnotes = "3213";
                break;
            case "F#":
                chordnotes = "4324";
                break;
            case "G":
                chordnotes = "0000";
                break;
            case "G#":
                chordnotes = "1111";
                break;

            // MAJOR 7 CHORDS
            case "Amaj7":
                chordnotes = "2122";
                break;
            case "A#maj7":
                chordnotes = "3233";
                break;
            case "Bmaj7":
                chordnotes = "4344";
                break;
            case "Cmaj7":
                chordnotes = "2002";
                break;
            case "C#maj7":
                chordnotes = "3113";
                break;
            case "Dmaj7":
                chordnotes = "0224";
                break;
            case "D#maj7":
                chordnotes = "5335";
                break;
            case "Emaj7":
                chordnotes = "2101";
                break;
            case "Fmaj7":
                chordnotes = "3212";
                break;
            case "F#maj7":
                chordnotes = "4323";
                break;
            case "Gmaj7":
                chordnotes = "0004";
                break;
            case "G#maj7":
                chordnotes = "1011";
                break;

            // DOMINANT 7 CHORDS
            case "A7":
                chordnotes = "2022";
                break;
            case "A#7":
                chordnotes = "3133";
                break;
            case "B7":
                chordnotes = "4244";
                break;
            case "C7":
                chordnotes = "2312";
                break;
            case "C#7":
                chordnotes = "3423";
                break;
            case "D7":
                chordnotes = "0534";
                break;
            case "D#7":
                chordnotes = "1021";
                break;
            case "E7":
                chordnotes = "0102";
                break;
            case "F7":
                chordnotes = "1213";
                break;
            case "F#7":
                chordnotes = "2324";
                break;
            case "G7":
                chordnotes = "3003";
                break;
            case "G#7":
                chordnotes = "4114";
                break;

            // MAJOR 6 CHORDS
            case "A6":
                chordnotes = "2224";
                break;
            case "A#6":
                chordnotes = "1113_3_c_A#6";
                break;
            case "B6":
                chordnotes = "1113_4_c_B6";
                break;
            case "C6":
                chordnotes = "1113_5_c_C6";
                break;
            case "C#6":
                chordnotes = "1113_6_c_C#6";
                break;
            case "D6":
                chordnotes = "4200";
                break;
            case "D#6":
                chordnotes = "5311";
                break;
            case "E6":
                chordnotes = "5311_2_c_E6";
                break;
            case "F6":
                chordnotes = "3210";
                break;
            case "F#6":
                chordnotes = "4321";
                break;
            case "G6":
                chordnotes = "0002";
                break;
            case "G#6":
                chordnotes = "1113";
                break;

            // MINOR CHORDS
            case "Am":
                chordnotes = "2212";
                break;
            case "A#m":
                chordnotes = "3323";
                break;
            case "Bm":
                chordnotes = "4434";
                break;
            case "Cm":
                chordnotes = "1011";
                break;
            case "C#m":
                chordnotes = "2122";
                break;
            case "Dm":
                chordnotes = "0233";
                break;
            case "D#m":
                chordnotes = "4344";
                break;
            case "Em":
                chordnotes = "2002";
                break;
            case "Fm":
                chordnotes = "3113";
                break;
            case "F#m":
                chordnotes = "4224";
                break;
            case "Gm":
                chordnotes = "5335";
                break;
            case "G#m":
                chordnotes = "1101";
                break;

            // MINOR 7 CHORDS
            case "Am7":
                chordnotes = "2012";
                break;
            case "A#m7":
                chordnotes = "3123";
                break;
            case "Bm7":
                chordnotes = "4234";
                break;
            case "Cm7":
                chordnotes = "1011";
                break;
            case "C#m7":
                chordnotes = "2122";
                break;
            case "Dm7":
                chordnotes = "0213";
                break;
            case "D#m7":
                chordnotes = "1324";
                break;
            case "Em7":
                chordnotes = "2000";
                break;
            case "Fm7":
                chordnotes = "3111";
                break;
            case "F#m7":
                chordnotes = "4222";
                break;
            case "Gm7":
                chordnotes = "5333";
                break;
            case "G#m7":
                chordnotes = "3111_4_c_G#m7";
                break;

            // MINOR 6 CHORDS
            case "Am6":
                chordnotes = "2214";
                break;
            case "A#m6":
                chordnotes = "3325";
                break;
            case "Bm6":
                chordnotes = "2214_3_c_Bm6";
                break;
            case "Cm6":
                chordnotes = "2214_4_c_Cm6";
                break;
            case "C#m6":
                chordnotes = "2214_5_c_C#m6";
                break;
            case "Dm6":
                chordnotes = "0203";
                break;
            case "D#m6":
                chordnotes = "1314";
                break;
            case "Em6":
                chordnotes = "2425";
                break;
            case "Fm6":
                chordnotes = "0113";
                break;
            case "F#m6":
                chordnotes = "1224";
                break;
            case "Gm6":
                chordnotes = "2335";
                break;
            case "G#m6":
                chordnotes = "1224_3_c_G#m6";
                break;

            // SUS (SUS4) CHORDS
            case "Asus":
                chordnotes = "2232";
                break;
            case "A#sus":
                chordnotes = "3343";
                break;
            case "Bsus":
                chordnotes = "1121_4_c_Bsus";
                break;
            case "Csus":
                chordnotes = "1121_5_c_Csus";
                break;
            case "C#sus":
                chordnotes = "1121_6_c_C#sus";
                break;
            case "Dsus":
                chordnotes = "1121_7_c_Dsus";
                break;
            case "D#sus":
                chordnotes = "1121_8_c_D#sus";
                break;
            case "Esus":
                chordnotes = "0202";
                break;
            case "Fsus":
                chordnotes = "1313";
                break;
            case "F#sus":
                chordnotes = "2424";
                break;
            case "Gsus":
                chordnotes = "0010";
                break;
            case "G#sus":
                chordnotes = "1121";
                break;

            // SUS2 / 2 CHORDS
            case "A2":
                chordnotes = "2202";
                break;
            case "A#2":
                chordnotes = "3313";
                break;
            case "B2":
                chordnotes = "4424";
                break;
            case "C2":
                chordnotes = "2010";
                break;
            case "C#2":
                chordnotes = "3121";
                break;
            case "D2":
                chordnotes = "4232";
                break;
            case "D#2":
                chordnotes = "3121_3_c_D#2";
                break;
            case "E2":
                chordnotes = "3121_4_c_E2";
                break;
            case "F2":
                chordnotes = "3121_5_c_F2";
                break;
            case "F#2":
                chordnotes = "3121_6_c_F#2";
                break;
            case "G2":
                chordnotes = "3121_7_c_G2";
                break;
            case "G#2":
                chordnotes = "3121_8_c_G#2";
                break;

            // 9 CHORDS
            case "A9":
                chordnotes = "2134_6_c_A9";
                break;
            case "A#9":
                chordnotes = "2134_7_c_A#9";
                break;
            case "B9":
                chordnotes = "2134_8_c_B9";
                break;
            case "C9":
                chordnotes = "2335";
                break;
            case "C#9":
                chordnotes = "1224_3_c_C#9";
                break;
            case "D9":
                chordnotes = "1224_4_c_D9";
                break;
            case "D#9":
                chordnotes = "1023";
                break;
            case "E9":
                chordnotes = "2134";
                break;
            case "F9":
                chordnotes = "3245";
                break;
            case "F#9":
                chordnotes = "2134_3_c_F#9";
                break;
            case "G9":
                chordnotes = "2134_4_c_G9";
                break;
            case "G#9":
                chordnotes = "2134_5_c_G#9";
                break;

            // add9 CHORDS
            case "Aadd9":
                chordnotes = "2134_6_c_Aadd9";
                break;
            case "A#add9":
                chordnotes = "2134_7_c_A#add9";
                break;
            case "Badd9":
                chordnotes = "2134_8_c_Badd9";
                break;
            case "Cadd9":
                chordnotes = "2335";
                break;
            case "C#add9":
                chordnotes = "1224_3_c_C#add9";
                break;
            case "Dadd9":
                chordnotes = "1224_4_c_Dadd9";
                break;
            case "D#add9":
                chordnotes = "1023";
                break;
            case "Eadd9":
                chordnotes = "2134";
                break;
            case "Fadd9":
                chordnotes = "3245";
                break;
            case "F#add9":
                chordnotes = "2134_3_c_F#add9";
                break;
            case "Gadd9":
                chordnotes = "2134_4_c_Gadd9";
                break;
            case "G#add9":
                chordnotes = "2134_5_c_G#add9";
                break;

            // DIMINISHED 7 CHORDS
            case "Adim":
                chordnotes = "1214";
                break;
            case "A#dim":
                chordnotes = "2325";
                break;
            case "Bdim":
                chordnotes = "1214_3_c_Bdim";
                break;
            case "Cdim":
                chordnotes = "1214";
                break;
            case "C#dim":
                chordnotes = "2325";
                break;
            case "Ddim":
                chordnotes = "1214_3_c_Ddim";
                break;
            case "D#dim":
                chordnotes = "1214";
                break;
            case "Edim":
                chordnotes = "2325";
                break;
            case "Fdim":
                chordnotes = "1214_3_c_Fdim";
                break;
            case "F#dim":
                chordnotes = "1214";
                break;
            case "Gdim":
                chordnotes = "2325";
                break;
            case "G#dim":
                chordnotes = "1214_3_c_G#dim";
                break;

            // MINOR 7 FLAT 5 (HALF DIMINISHED)
            case "Am7b5":
                chordnotes = "1215";
                break;
            case "A#m7b5":
                chordnotes = "1215_2_c_A#m7b5";
                break;
            case "Bm7b5":
                chordnotes = "1215_3_c_Bm7b5";
                break;
            case "Cm7b5":
                chordnotes = "1314";
                break;
            case "C#m7b5":
                chordnotes = "1314_2_c_C#m7b5";
                break;
            case "Dm7b5":
                chordnotes = "0113";
                break;
            case "D#m7b5":
                chordnotes = "1224";
                break;
            case "Em7b5":
                chordnotes = "1224_2_c_Em7b5";
                break;
            case "Fm7b5":
                chordnotes = "3101";
                break;
            case "F#m7b5":
                chordnotes = "1224_4_c_F#m7b5";
                break;
            case "Gm7b5":
                chordnotes = "1224_5_c_Gm7b5";
                break;
            case "G#m7b5":
                chordnotes = "0104";
                break;

            // AUGMENTED (#5)
            case "Aaug":
                chordnotes = "3223";
                break;
            case "A#aug":
                chordnotes = "4334";
                break;
            case "Baug":
                chordnotes = "1001";
                break;
            case "Caug":
                chordnotes = "2112";
                break;
            case "C#aug":
                chordnotes = "3223";
                break;
            case "Daug":
                chordnotes = "4334";
                break;
            case "D#aug":
                chordnotes = "1001";
                break;
            case "Eaug":
                chordnotes = "2112";
                break;
            case "Faug":
                chordnotes = "3223";
                break;
            case "F#aug":
                chordnotes = "4334";
                break;
            case "Gaug":
                chordnotes = "1001";
                break;
            case "G#aug":
                chordnotes = "2112";
                break;
            default:
                chordnotes = "xxxx";
                break;
        }

        // Standard cavaquinho chords all start with c_ (cavaquinho) and end with _0 (fret to start with)
        if (!chordnotes.contains("_")) {
            chordnotes = chordnotes + "_0_c_"+chordtoworkon;
        }
        return chordnotes;
    }

    public String banjo4stringChords(Context c, Preferences preferences, String chord) {

        String chordnotes;
        String chordtoworkon = simplifyChords(c,preferences,chord);

        // MAJOR CHORDS
        switch (chordtoworkon) {
            case "A":
                chordnotes = "2222";
                break;
            case "A#":
                chordnotes = "3333";
                break;
            case "B":
                chordnotes = "4444";
                break;
            case "C":
                chordnotes = "2012";
                break;
            case "C#":
                chordnotes = "3123";
                break;
            case "D":
                chordnotes = "0234";
                break;
            case "D#":
                chordnotes = "5345";
                break;
            case "E":
                chordnotes = "2102";
                break;
            case "F":
                chordnotes = "3213";
                break;
            case "F#":
                chordnotes = "4324";
                break;
            case "G":
                chordnotes = "0000";
                break;
            case "G#":
                chordnotes = "1111";
                break;

            // MAJOR 7 CHORDS
            case "Amaj7":
                chordnotes = "2122";
                break;
            case "A#maj7":
                chordnotes = "3233";
                break;
            case "Bmaj7":
                chordnotes = "4344";
                break;
            case "Cmaj7":
                chordnotes = "2002";
                break;
            case "C#maj7":
                chordnotes = "3113";
                break;
            case "Dmaj7":
                chordnotes = "0224";
                break;
            case "D#maj7":
                chordnotes = "5335";
                break;
            case "Emaj7":
                chordnotes = "2101";
                break;
            case "Fmaj7":
                chordnotes = "3212";
                break;
            case "F#maj7":
                chordnotes = "4323";
                break;
            case "Gmaj7":
                chordnotes = "0004";
                break;
            case "G#maj7":
                chordnotes = "1011";
                break;

            // DOMINANT 7 CHORDS
            case "A7":
                chordnotes = "2022";
                break;
            case "A#7":
                chordnotes = "3133";
                break;
            case "B7":
                chordnotes = "4244";
                break;
            case "C7":
                chordnotes = "2312";
                break;
            case "C#7":
                chordnotes = "3423";
                break;
            case "D7":
                chordnotes = "0534";
                break;
            case "D#7":
                chordnotes = "1021";
                break;
            case "E7":
                chordnotes = "0102";
                break;
            case "F7":
                chordnotes = "1213";
                break;
            case "F#7":
                chordnotes = "2324";
                break;
            case "G7":
                chordnotes = "3003";
                break;
            case "G#7":
                chordnotes = "4114";
                break;

            // MAJOR 6 CHORDS
            case "A6":
                chordnotes = "2224";
                break;
            case "A#6":
                chordnotes = "1113_3_b_A#6";
                break;
            case "B6":
                chordnotes = "1113_4_b_B6";
                break;
            case "C6":
                chordnotes = "1113_5_b_C6";
                break;
            case "C#6":
                chordnotes = "1113_6_b_C#6";
                break;
            case "D6":
                chordnotes = "4200";
                break;
            case "D#6":
                chordnotes = "5311";
                break;
            case "E6":
                chordnotes = "5311_2_b_E6";
                break;
            case "F6":
                chordnotes = "3210";
                break;
            case "F#6":
                chordnotes = "4321";
                break;
            case "G6":
                chordnotes = "0002";
                break;
            case "G#6":
                chordnotes = "1113";
                break;

            // MINOR CHORDS
            case "Am":
                chordnotes = "2212";
                break;
            case "A#m":
                chordnotes = "3323";
                break;
            case "Bm":
                chordnotes = "4434";
                break;
            case "Cm":
                chordnotes = "1011";
                break;
            case "C#m":
                chordnotes = "2122";
                break;
            case "Dm":
                chordnotes = "0233";
                break;
            case "D#m":
                chordnotes = "4344";
                break;
            case "Em":
                chordnotes = "2002";
                break;
            case "Fm":
                chordnotes = "3113";
                break;
            case "F#m":
                chordnotes = "4224";
                break;
            case "Gm":
                chordnotes = "5335";
                break;
            case "G#m":
                chordnotes = "1101";
                break;

            // MINOR 7 CHORDS
            case "Am7":
                chordnotes = "2012";
                break;
            case "A#m7":
                chordnotes = "3123";
                break;
            case "Bm7":
                chordnotes = "4234";
                break;
            case "Cm7":
                chordnotes = "1011";
                break;
            case "C#m7":
                chordnotes = "2122";
                break;
            case "Dm7":
                chordnotes = "0213";
                break;
            case "D#m7":
                chordnotes = "1324";
                break;
            case "Em7":
                chordnotes = "2000";
                break;
            case "Fm7":
                chordnotes = "3111";
                break;
            case "F#m7":
                chordnotes = "4222";
                break;
            case "Gm7":
                chordnotes = "5333";
                break;
            case "G#m7":
                chordnotes = "3111_4_b_G#m7";
                break;

            // MINOR 6 CHORDS
            case "Am6":
                chordnotes = "2214";
                break;
            case "A#m6":
                chordnotes = "3325";
                break;
            case "Bm6":
                chordnotes = "2214_3_b_Bm6";
                break;
            case "Cm6":
                chordnotes = "2214_4_b_Cm6";
                break;
            case "C#m6":
                chordnotes = "2214_5_b_C#m6";
                break;
            case "Dm6":
                chordnotes = "0203";
                break;
            case "D#m6":
                chordnotes = "1314";
                break;
            case "Em6":
                chordnotes = "2425";
                break;
            case "Fm6":
                chordnotes = "0113";
                break;
            case "F#m6":
                chordnotes = "1224";
                break;
            case "Gm6":
                chordnotes = "2335";
                break;
            case "G#m6":
                chordnotes = "1224_3_b_G#m6";
                break;

            // SUS (SUS4) CHORDS
            case "Asus":
                chordnotes = "2232";
                break;
            case "A#sus":
                chordnotes = "3343";
                break;
            case "Bsus":
                chordnotes = "1121_4_b_Bsus";
                break;
            case "Csus":
                chordnotes = "1121_5_b_Csus";
                break;
            case "C#sus":
                chordnotes = "1121_6_b_C#sus";
                break;
            case "Dsus":
                chordnotes = "1121_7_b_Dsus";
                break;
            case "D#sus":
                chordnotes = "1121_8_b_D#sus";
                break;
            case "Esus":
                chordnotes = "0202";
                break;
            case "Fsus":
                chordnotes = "1313";
                break;
            case "F#sus":
                chordnotes = "2424";
                break;
            case "Gsus":
                chordnotes = "0010";
                break;
            case "G#sus":
                chordnotes = "1121";
                break;

            // SUS2 / 2 CHORDS
            case "A2":
                chordnotes = "2202";
                break;
            case "A#2":
                chordnotes = "3313";
                break;
            case "B2":
                chordnotes = "4424";
                break;
            case "C2":
                chordnotes = "2010";
                break;
            case "C#2":
                chordnotes = "3121";
                break;
            case "D2":
                chordnotes = "4232";
                break;
            case "D#2":
                chordnotes = "3121_3_b_D#2";
                break;
            case "E2":
                chordnotes = "3121_4_b_E2";
                break;
            case "F2":
                chordnotes = "3121_5_b_F2";
                break;
            case "F#2":
                chordnotes = "3121_6_b_F#2";
                break;
            case "G2":
                chordnotes = "3121_7_b_G2";
                break;
            case "G#2":
                chordnotes = "3121_8_b_G#2";
                break;

            // 9 CHORDS
            case "A9":
                chordnotes = "2134_6_b_A9";
                break;
            case "A#9":
                chordnotes = "2134_7_b_A#9";
                break;
            case "B9":
                chordnotes = "2134_8_b_B9";
                break;
            case "C9":
                chordnotes = "2335";
                break;
            case "C#9":
                chordnotes = "1224_3_b_C#9";
                break;
            case "D9":
                chordnotes = "1224_4_b_D9";
                break;
            case "D#9":
                chordnotes = "1023";
                break;
            case "E9":
                chordnotes = "2134";
                break;
            case "F9":
                chordnotes = "3245";
                break;
            case "F#9":
                chordnotes = "2134_3_b_F#9";
                break;
            case "G9":
                chordnotes = "2134_4_b_G9";
                break;
            case "G#9":
                chordnotes = "2134_5_b_G#9";
                break;

            // add9 CHORDS
            case "Aadd9":
                chordnotes = "2134_6_b_Aadd9";
                break;
            case "A#add9":
                chordnotes = "2134_7_b_A#add9";
                break;
            case "Badd9":
                chordnotes = "2134_8_b_Badd9";
                break;
            case "Cadd9":
                chordnotes = "2335";
                break;
            case "C#add9":
                chordnotes = "1224_3_b_C#add9";
                break;
            case "Dadd9":
                chordnotes = "1224_4_b_Dadd9";
                break;
            case "D#add9":
                chordnotes = "1023";
                break;
            case "Eadd9":
                chordnotes = "2134";
                break;
            case "Fadd9":
                chordnotes = "3245";
                break;
            case "F#add9":
                chordnotes = "2134_3_b_F#add9";
                break;
            case "Gadd9":
                chordnotes = "2134_4_b_Gadd9";
                break;
            case "G#add9":
                chordnotes = "2134_5_b_G#add9";
                break;

            // DIMINISHED 7 CHORDS
            case "Adim":
                chordnotes = "1214";
                break;
            case "A#dim":
                chordnotes = "2325";
                break;
            case "Bdim":
                chordnotes = "1214_3_b_Bdim";
                break;
            case "Cdim":
                chordnotes = "1214";
                break;
            case "C#dim":
                chordnotes = "2325";
                break;
            case "Ddim":
                chordnotes = "1214_3_b_Ddim";
                break;
            case "D#dim":
                chordnotes = "1214";
                break;
            case "Edim":
                chordnotes = "2325";
                break;
            case "Fdim":
                chordnotes = "1214_3_b_Fdim";
                break;
            case "F#dim":
                chordnotes = "1214";
                break;
            case "Gdim":
                chordnotes = "2325";
                break;
            case "G#dim":
                chordnotes = "1214_3_b_G#dim";
                break;

            // MINOR 7 FLAT 5 (HALF DIMINISHED)
            case "Am7b5":
                chordnotes = "1215";
                break;
            case "A#m7b5":
                chordnotes = "1215_2_b_A#m7b5";
                break;
            case "Bm7b5":
                chordnotes = "1215_3_b_Bm7b5";
                break;
            case "Cm7b5":
                chordnotes = "1314";
                break;
            case "C#m7b5":
                chordnotes = "1314_2_b_C#m7b5";
                break;
            case "Dm7b5":
                chordnotes = "0113";
                break;
            case "D#m7b5":
                chordnotes = "1224";
                break;
            case "Em7b5":
                chordnotes = "1224_2_b_Em7b5";
                break;
            case "Fm7b5":
                chordnotes = "3101";
                break;
            case "F#m7b5":
                chordnotes = "1224_4_b_F#m7b5";
                break;
            case "Gm7b5":
                chordnotes = "1224_5_b_Gm7b5";
                break;
            case "G#m7b5":
                chordnotes = "0104";
                break;

            // AUGMENTED (#5)
            case "Aaug":
                chordnotes = "3223";
                break;
            case "A#aug":
                chordnotes = "4334";
                break;
            case "Baug":
                chordnotes = "1001";
                break;
            case "Caug":
                chordnotes = "2112";
                break;
            case "C#aug":
                chordnotes = "3223";
                break;
            case "Daug":
                chordnotes = "4334";
                break;
            case "D#aug":
                chordnotes = "1001";
                break;
            case "Eaug":
                chordnotes = "2112";
                break;
            case "Faug":
                chordnotes = "3223";
                break;
            case "F#aug":
                chordnotes = "4334";
                break;
            case "Gaug":
                chordnotes = "1001";
                break;
            case "G#aug":
                chordnotes = "2112";
                break;
            default:
                chordnotes = "xxxx";
                break;
        }

        // Standard banjo 4 string chords all start with b_ (banjo 4 string) and end with _0 (fret to start with)
        if (!chordnotes.contains("_")) {
            chordnotes = chordnotes + "_0_b_"+chordtoworkon;
        }

        return chordnotes;
    }

    public String banjo5stringChords(Context c, Preferences preferences, String chord) {

        String chordnotes;
        String chordtoworkon = simplifyChords(c,preferences,chord);

        // MAJOR CHORDS
        switch (chordtoworkon) {
            case "A":
                chordnotes = "x2222";
                break;
            case "A#":
                chordnotes = "x3333";
                break;
            case "B":
                chordnotes = "x4444";
                break;
            case "C":
                chordnotes = "02012";
                break;
            case "C#":
                chordnotes = "x3123";
                break;
            case "D":
                chordnotes = "x0234";
                break;
            case "D#":
                chordnotes = "05345";
                break;
            case "E":
                chordnotes = "x2102";
                break;
            case "F":
                chordnotes = "x3213";
                break;
            case "F#":
                chordnotes = "x4324";
                break;
            case "G":
                chordnotes = "00000";
                break;
            case "G#":
                chordnotes = "x1111";
                break;

            // MAJOR 7 CHORDS
            case "Amaj7":
                chordnotes = "x2122";
                break;
            case "A#maj7":
                chordnotes = "x3233";
                break;
            case "Bmaj7":
                chordnotes = "x4344";
                break;
            case "Cmaj7":
                chordnotes = "02002";
                break;
            case "C#maj7":
                chordnotes = "x3113";
                break;
            case "Dmaj7":
                chordnotes = "x0224";
                break;
            case "D#maj7":
                chordnotes = "05335";
                break;
            case "Emaj7":
                chordnotes = "x2101";
                break;
            case "Fmaj7":
                chordnotes = "x3212";
                break;
            case "F#maj7":
                chordnotes = "04323";
                break;
            case "Gmaj7":
                chordnotes = "00004";
                break;
            case "G#maj7":
                chordnotes = "01011";
                break;

            // DOMINANT 7 CHORDS
            case "A7":
                chordnotes = "02022";
                break;
            case "A#7":
                chordnotes = "x3133";
                break;
            case "B7":
                chordnotes = "x4244";
                break;
            case "C7":
                chordnotes = "02312";
                break;
            case "C#7":
                chordnotes = "x3423";
                break;
            case "D7":
                chordnotes = "x0534";
                break;
            case "D#7":
                chordnotes = "01021";
                break;
            case "E7":
                chordnotes = "x0102";
                break;
            case "F7":
                chordnotes = "x1213";
                break;
            case "F#7":
                chordnotes = "x2324";
                break;
            case "G7":
                chordnotes = "03003";
                break;
            case "G#7":
                chordnotes = "x4114";
                break;

            // MAJOR 6 CHORDS
            case "A6":
                chordnotes = "x2224";
                break;
            case "A#6":
                chordnotes = "01113_3_B_A#6";
                break;
            case "B6":
                chordnotes = "x1113_4_B_B6";
                break;
            case "C6":
                chordnotes = "01113_5_B_C6";
                break;
            case "C#6":
                chordnotes = "x1113_6_B_C#6";
                break;
            case "D6":
                chordnotes = "x4200";
                break;
            case "D#6":
                chordnotes = "05311";
                break;
            case "E6":
                chordnotes = "x5311_2_B_E6";
                break;
            case "F6":
                chordnotes = "x3210";
                break;
            case "F#6":
                chordnotes = "x4321";
                break;
            case "G6":
                chordnotes = "00002";
                break;
            case "G#6":
                chordnotes = "x1113";
                break;

            // MINOR CHORDS
            case "Am":
                chordnotes = "x2212";
                break;
            case "A#m":
                chordnotes = "x3323";
                break;
            case "Bm":
                chordnotes = "x4434";
                break;
            case "Cm":
                chordnotes = "01011";
                break;
            case "C#m":
                chordnotes = "x2122";
                break;
            case "Dm":
                chordnotes = "x0233";
                break;
            case "D#m":
                chordnotes = "x4344";
                break;
            case "Em":
                chordnotes = "02002";
                break;
            case "Fm":
                chordnotes = "x3113";
                break;
            case "F#m":
                chordnotes = "x4224";
                break;
            case "Gm":
                chordnotes = "05335";
                break;
            case "G#m":
                chordnotes = "x1101";
                break;

            // MINOR 7 CHORDS
            case "Am7":
                chordnotes = "02012";
                break;
            case "A#m7":
                chordnotes = "x3123";
                break;
            case "Bm7":
                chordnotes = "x4234";
                break;
            case "Cm7":
                chordnotes = "01011";
                break;
            case "C#m7":
                chordnotes = "x2122";
                break;
            case "Dm7":
                chordnotes = "x0213";
                break;
            case "D#m7":
                chordnotes = "x1324";
                break;
            case "Em7":
                chordnotes = "02000";
                break;
            case "Fm7":
                chordnotes = "x3111";
                break;
            case "F#m7":
                chordnotes = "x4222";
                break;
            case "Gm7":
                chordnotes = "05333";
                break;
            case "G#m7":
                chordnotes = "x3111_4_B_G#m7";
                break;

            // MINOR 6 CHORDS
            case "Am6":
                chordnotes = "x2214";
                break;
            case "A#m6":
                chordnotes = "03325";
                break;
            case "Bm6":
                chordnotes = "x2214_3_B_Bm6";
                break;
            case "Cm6":
                chordnotes = "02214_4_B_Cm6";
                break;
            case "C#m6":
                chordnotes = "x2214_5_B_C#m6";
                break;
            case "Dm6":
                chordnotes = "x0203";
                break;
            case "D#m6":
                chordnotes = "x1314";
                break;
            case "Em6":
                chordnotes = "02425";
                break;
            case "Fm6":
                chordnotes = "x0113";
                break;
            case "F#m6":
                chordnotes = "x1224";
                break;
            case "Gm6":
                chordnotes = "02335";
                break;
            case "G#m6":
                chordnotes = "x1224_3_B_G#m6";
                break;

            // SUS (SUS4) CHORDS
            case "Asus":
                chordnotes = "x2232";
                break;
            case "A#sus":
                chordnotes = "x3343";
                break;
            case "Bsus":
                chordnotes = "x1121_4_B_Bsus";
                break;
            case "Csus":
                chordnotes = "01121_5_B_Csus";
                break;
            case "C#sus":
                chordnotes = "x1121_6_B_C#sus";
                break;
            case "Dsus":
                chordnotes = "01121_7_B_Dsus";
                break;
            case "D#sus":
                chordnotes = "x1121_8_B_D#sus";
                break;
            case "Esus":
                chordnotes = "x0202";
                break;
            case "Fsus":
                chordnotes = "x1313";
                break;
            case "F#sus":
                chordnotes = "x2424";
                break;
            case "Gsus":
                chordnotes = "00010";
                break;
            case "G#sus":
                chordnotes = "x1121";
                break;

            // SUS2 / 2 CHORDS
            case "A2":
                chordnotes = "x2202";
                break;
            case "A#2":
                chordnotes = "x3313";
                break;
            case "B2":
                chordnotes = "x4424";
                break;
            case "C2":
                chordnotes = "02010";
                break;
            case "C#2":
                chordnotes = "x3121";
                break;
            case "D2":
                chordnotes = "x4232";
                break;
            case "D#2":
                chordnotes = "03121_3_B_D#2";
                break;
            case "E2":
                chordnotes = "x3121_4_B_E2";
                break;
            case "F2":
                chordnotes = "03121_5_B_F2";
                break;
            case "F#2":
                chordnotes = "x3121_6_B_F#2";
                break;
            case "G2":
                chordnotes = "03121_7_B_G2";
                break;
            case "G#2":
                chordnotes = "x3121_8_B_G#2";
                break;

            // 9 CHORDS
            case "A9":
                chordnotes = "x2134_6_B_A9";
                break;
            case "A#9":
                chordnotes = "x2134_7_B_A#9";
                break;
            case "B9":
                chordnotes = "x2134_8_B_B9";
                break;
            case "C9":
                chordnotes = "02335";
                break;
            case "C#9":
                chordnotes = "x1224_3_B_C#9";
                break;
            case "D9":
                chordnotes = "x1224_4_B_D9";
                break;
            case "D#9":
                chordnotes = "01023";
                break;
            case "E9":
                chordnotes = "x2134";
                break;
            case "F9":
                chordnotes = "03245";
                break;
            case "F#9":
                chordnotes = "x2134_3_B_F#9";
                break;
            case "G9":
                chordnotes = "02134_4_B_G9";
                break;
            case "G#9":
                chordnotes = "x2134_5_B_G#9";
                break;

            // add9 CHORDS
            case "Aadd9":
                chordnotes = "x2134_6_B_Aadd9";
                break;
            case "A#add9":
                chordnotes = "x2134_7_B_A#add9";
                break;
            case "Badd9":
                chordnotes = "x2134_8_B_Badd9";
                break;
            case "Cadd9":
                chordnotes = "02335";
                break;
            case "C#add9":
                chordnotes = "x1224_3_B_C#add9";
                break;
            case "Dadd9":
                chordnotes = "x1224_4_B_Dadd9";
                break;
            case "D#add9":
                chordnotes = "01023";
                break;
            case "Eadd9":
                chordnotes = "x2134";
                break;
            case "Fadd9":
                chordnotes = "03245";
                break;
            case "F#add9":
                chordnotes = "x2134_3_B_F#add9";
                break;
            case "Gadd9":
                chordnotes = "02134_4_B_Gadd9";
                break;
            case "G#add9":
                chordnotes = "x2134_5_B_G#add9";
                break;

            // DIMINISHED 7 CHORDS
            case "Adim":
                chordnotes = "x1214";
                break;
            case "A#dim":
                chordnotes = "02325";
                break;
            case "Bdim":
                chordnotes = "x1214_3_B_Bdim";
                break;
            case "Cdim":
                chordnotes = "x1214";
                break;
            case "C#dim":
                chordnotes = "02325";
                break;
            case "Ddim":
                chordnotes = "x1214_3_B_Ddim";
                break;
            case "D#dim":
                chordnotes = "x1214";
                break;
            case "Edim":
                chordnotes = "02325";
                break;
            case "Fdim":
                chordnotes = "x1214_3_B_Fdim";
                break;
            case "F#dim":
                chordnotes = "x1214";
                break;
            case "Gdim":
                chordnotes = "02325";
                break;
            case "G#dim":
                chordnotes = "x1214_3_B_G#dim";
                break;

            // MINOR 7 FLAT 5 (HALF DIMINISHED)
            case "Am7b5":
                chordnotes = "01215";
                break;
            case "A#m7b5":
                chordnotes = "x1215_2_B_A#m7b5";
                break;
            case "Bm7b5":
                chordnotes = "x1215_3_B_Bm7b5";
                break;
            case "Cm7b5":
                chordnotes = "x1314";
                break;
            case "C#m7b5":
                chordnotes = "01314_2_B_C#m7b5";
                break;
            case "Dm7b5":
                chordnotes = "x0113";
                break;
            case "D#m7b5":
                chordnotes = "x1224";
                break;
            case "Em7b5":
                chordnotes = "01224_2_B_Em7b5";
                break;
            case "Fm7b5":
                chordnotes = "x3101";
                break;
            case "F#m7b5":
                chordnotes = "x1224_4_B_F#m7b5";
                break;
            case "Gm7b5":
                chordnotes = "01224_5_B_Gm7b5";
                break;
            case "G#m7b5":
                chordnotes = "x0104";
                break;

            // AUGMENTED (#5)
            case "Aaug":
                chordnotes = "x3223";
                break;
            case "A#aug":
                chordnotes = "x4334";
                break;
            case "Baug":
                chordnotes = "01001";
                break;
            case "Caug":
                chordnotes = "x2112";
                break;
            case "C#aug":
                chordnotes = "x3223";
                break;
            case "Daug":
                chordnotes = "x4334";
                break;
            case "D#aug":
                chordnotes = "01001";
                break;
            case "Eaug":
                chordnotes = "x2112";
                break;
            case "Faug":
                chordnotes = "x3223";
                break;
            case "F#aug":
                chordnotes = "x4334";
                break;
            case "Gaug":
                chordnotes = "01001";
                break;
            case "G#aug":
                chordnotes = "x2112";
                break;
            default:
                chordnotes = "xxxxx";
                break;
        }

        // Standard banjo 5 string chords all start with B_ (banjo 5 string) and end with _0 (fret to start with)
        if (!chordnotes.contains("_")) {
            chordnotes = chordnotes + "_0_B_"+chordtoworkon;
        }

        return chordnotes;
    }

    public String pianoChords(Context c, Preferences preferences, String chord) {

        String chordnotes;
        String chordtoworkon = simplifyChords(c,preferences,chord);

        // Notes C  C# D  D# E  F  F# G  G# A  A# B  C  C# D  D# E  F  F# G  G# A  A# B
        //       1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24

        // MAJOR CHORDS
        switch (chordtoworkon) {
            case "A":
                chordnotes = "A,C#,E";
                break;
            case "A#":
                chordnotes = "A#,D,F";
                break;
            case "B":
                chordnotes = "B,D#,F#";
                break;
            case "C":
                chordnotes = "C,E,G";
                break;
            case "C#":
                chordnotes = "C#,F,G#";
                break;
            case "D":
                chordnotes = "D,F#,A";
                break;
            case "D#":
                chordnotes = "D#,G,A#";
                break;
            case "E":
                chordnotes = "E,G#,B";
                break;
            case "F":
                chordnotes = "F,A,C";
                break;
            case "F#":
                chordnotes = "F#,A#,C#";
                break;
            case "G":
                chordnotes = "G,B,D";
                break;
            case "G#":
                chordnotes = "G#,C,D#";
                break;

            // MAJOR 7 CHORDS
            case "Amaj7":
                chordnotes = "A,C#,E,G#";
                break;
            case "A#maj7":
                chordnotes = "A#,D,F,A";
                break;
            case "Bmaj7":
                chordnotes = "B,D#,F#,A#";
                break;
            case "Cmaj7":
                chordnotes = "C,E,G,B";
                break;
            case "C#maj7":
                chordnotes = "C#,F,G#,C";
                break;
            case "Dmaj7":
                chordnotes = "D,F#,A,C#";
                break;
            case "D#maj7":
                chordnotes = "D#,G,A#,D";
                break;
            case "Emaj7":
                chordnotes = "E,G#,B,D#";
                break;
            case "Fmaj7":
                chordnotes = "F,A,C,E";
                break;
            case "F#maj7":
                chordnotes = "F#,A#,C#,F";
                break;
            case "Gmaj7":
                chordnotes = "G,B,D,F#";
                break;
            case "G#maj7":
                chordnotes = "G#,C,D#,G";
                break;

            // DOMINANT 7 CHORDS
            case "A7":
                chordnotes = "A,C#,E,G";
                break;
            case "A#7":
                chordnotes = "A#,D,F,G#";
                break;
            case "B7":
                chordnotes = "B,D#,F#,A";
                break;
            case "C7":
                chordnotes = "C,E,G,A#";
                break;
            case "C#7":
                chordnotes = "C#,F,G#,B";
                break;
            case "D7":
                chordnotes = "D,F#,A,C";
                break;
            case "D#7":
                chordnotes = "D#,G,A#,C#";
                break;
            case "E7":
                chordnotes = "E,G#,B,D";
                break;
            case "F7":
                chordnotes = "F,A,C,D#";
                break;
            case "F#7":
                chordnotes = "F#,A#,C#,E";
                break;
            case "G7":
                chordnotes = "G,B,D,F";
                break;
            case "G#7":
                chordnotes = "G#,C,D#,F#";
                break;

            // MAJOR 6 CHORDS
            case "A6":
                chordnotes = "A,C#,E,F#";
                break;
            case "A#6":
                chordnotes = "A#,D,F,G";
                break;
            case "B6":
                chordnotes = "B,D#,F#,G#";
                break;
            case "C6":
                chordnotes = "C,E,G,A";
                break;
            case "C#6":
                chordnotes = "C#,F,G#,A#";
                break;
            case "D6":
                chordnotes = "D,F#,A,B";
                break;
            case "D#6":
                chordnotes = "D#,G,A#,C";
                break;
            case "E6":
                chordnotes = "E,G#,B,C#";
                break;
            case "F6":
                chordnotes = "F,A,C,D";
                break;
            case "F#6":
                chordnotes = "F#,A#,C#,D#";
                break;
            case "G6":
                chordnotes = "G,B,D,E";
                break;
            case "G#6":
                chordnotes = "G#,C,D#,F";
                break;

            // MINOR CHORDS
            case "Am":
                chordnotes = "A,C,E";
                break;
            case "A#m":
                chordnotes = "A#,C#,F";
                break;
            case "Bm":
                chordnotes = "B,D,F#";
                break;
            case "Cm":
                chordnotes = "C,D#,G";
                break;
            case "C#m":
                chordnotes = "C#,E,G#";
                break;
            case "Dm":
                chordnotes = "D,F,A";
                break;
            case "D#m":
                chordnotes = "D#,F#,A#";
                break;
            case "Em":
                chordnotes = "E,G,B";
                break;
            case "Fm":
                chordnotes = "F,G#,C";
                break;
            case "F#m":
                chordnotes = "F#,A,C#";
                break;
            case "Gm":
                chordnotes = "G,A#,D";
                break;
            case "G#m":
                chordnotes = "G#,B,D#";
                break;

            // MINOR 6 CHORDS
            case "Am6":
                chordnotes = "A,C,E,F#";
                break;
            case "A#m6":
                chordnotes = "A#,C#,F,G";
                break;
            case "Bm6":
                chordnotes = "B,D,F#,G#";
                break;
            case "Cm6":
                chordnotes = "C,D#,G,A";
                break;
            case "C#m6":
                chordnotes = "C#,E,G#,A#";
                break;
            case "Dm6":
                chordnotes = "D,F,A,B";
                break;
            case "D#m6":
                chordnotes = "D#,F#,A#,C";
                break;
            case "Em6":
                chordnotes = "E,G,B,C#";
                break;
            case "Fm6":
                chordnotes = "F,G#,C,D";
                break;
            case "F#m6":
                chordnotes = "F#,A,C#,D#";
                break;
            case "Gm6":
                chordnotes = "G,A#,D,E";
                break;
            case "G#m6":
                chordnotes = "G#,B,D#,F";
                break;

            // MINOR 7 CHORDS
            case "Am7":
                chordnotes = "A,C,E,G";
                break;
            case "A#m7":
                chordnotes = "A#,C#,F,G#";
                break;
            case "Bm7":
                chordnotes = "B,D,F#,A";
                break;
            case "Cm7":
                chordnotes = "C,D#,G,A#";
                break;
            case "C#m7":
                chordnotes = "C#,E,G#,B";
                break;
            case "Dm7":
                chordnotes = "D,F,A,C";
                break;
            case "D#m7":
                chordnotes = "D#,F#,A#,C#";
                break;
            case "Em7":
                chordnotes = "E,G,B,D";
                break;
            case "Fm7":
                chordnotes = "F,G#,C,D#";
                break;
            case "F#m7":
                chordnotes = "F#,A,C#,E";
                break;
            case "Gm7":
                chordnotes = "G,A#,D,F";
                break;
            case "G#m7":
                chordnotes = "G#,B,D#,F#";
                break;

            // SUS (SUS4) CHORDS
            case "Asus":
                chordnotes = "A,D,E";
                break;
            case "A#sus":
                chordnotes = "A#,D#,F";
                break;
            case "Bsus":
                chordnotes = "B,E,F#";
                break;
            case "Csus":
                chordnotes = "C,F,G";
                break;
            case "C#sus":
                chordnotes = "C#,F#,G#";
                break;
            case "Dsus":
                chordnotes = "D,G,A";
                break;
            case "D#sus":
                chordnotes = "D#,G#,A#";
                break;
            case "Esus":
                chordnotes = "E,A,B";
                break;
            case "Fsus":
                chordnotes = "F,A#,C";
                break;
            case "F#sus":
                chordnotes = "F#,B,C#";
                break;
            case "Gsus":
                chordnotes = "G,C,D";
                break;
            case "G#sus":
                chordnotes = "G#,C#,D#";
                break;

            // SUS2 / 2 CHORDS
            case "A2":
                chordnotes = "A,B,E";
                break;
            case "A#2":
                chordnotes = "A#,C,F";
                break;
            case "B2":
                chordnotes = "B,C#,F#";
                break;
            case "C2":
                chordnotes = "C,D,G";
                break;
            case "C#2":
                chordnotes = "C#,D#,G#";
                break;
            case "D2":
                chordnotes = "D,E,A";
                break;
            case "D#2":
                chordnotes = "D#,F,A#";
                break;
            case "E2":
                chordnotes = "E,F#,B";
                break;
            case "F2":
                chordnotes = "F,G,C";
                break;
            case "F#2":
                chordnotes = "F#,G#,C#";
                break;
            case "G2":
                chordnotes = "G,A,D";
                break;
            case "G#2":
                chordnotes = "G#,A#,D#";
                break;

            // 9 CHORDS
            case "A9":
                chordnotes = "A,B,C#,E";
                break;
            case "A#9":
                chordnotes = "A#,C,D,F";
                break;
            case "B9":
                chordnotes = "B,C#,D#,F#";
                break;
            case "C9":
                chordnotes = "C,D,E,G";
                break;
            case "C#9":
                chordnotes = "C#,D#,F,G#";
                break;
            case "D9":
                chordnotes = "D,E,F#,A";
                break;
            case "D#9":
                chordnotes = "D#,F,G,A#";
                break;
            case "E9":
                chordnotes = "E,F#,G#,B";
                break;
            case "F9":
                chordnotes = "F,G,A,C";
                break;
            case "F#9":
                chordnotes = "F#,G#,A#,C#";
                break;
            case "G9":
                chordnotes = "G,A,B,D";
                break;
            case "G#9":
                chordnotes = "G#,A#,C,D#";
                break;

            // add9 CHORDS
            case "Aadd9":
                chordnotes = "A,C#,E,B";
                break;
            case "A#add9":
                chordnotes = "A#,D,F,C";
                break;
            case "Badd9":
                chordnotes = "B,D#,F#,C#";
                break;
            case "Cadd9":
                chordnotes = "C,E,G,D";
                break;
            case "C#add9":
                chordnotes = "C#,F,G#,D#";
                break;
            case "Dadd9":
                chordnotes = "D,F#,A,E";
                break;
            case "D#add9":
                chordnotes = "D#,G,A#,F";
                break;
            case "Eadd9":
                chordnotes = "E,G#,B,F#";
                break;
            case "Fadd9":
                chordnotes = "F,A,C,G";
                break;
            case "F#add9":
                chordnotes = "F#,A#,C#,G#";
                break;
            case "Gadd9":
                chordnotes = "G,B,D,A";
                break;
            case "G#add9":
                chordnotes = "G#,C,D#,A#";
                break;

            // DIMINISHED 7 CHORDS
            case "Adim":
                chordnotes = "A,C,D#,F#";
                break;
            case "A#dim":
                chordnotes = "A#,C#,E,G";
                break;
            case "Bdim":
                chordnotes = "B,D,F,G#";
                break;
            case "Cdim":
                chordnotes = "C,D#,F#,A";
                break;
            case "C#dim":
                chordnotes = "C#,E,G,A#";
                break;
            case "Ddim":
                chordnotes = "D,F,G#,B";
                break;
            case "D#dim":
                chordnotes = "D#,F#,A,C";
                break;
            case "Edim":
                chordnotes = "E,G,A#,C#";
                break;
            case "Fdim":
                chordnotes = "F,G#,B,D";
                break;
            case "F#dim":
                chordnotes = "F#,A,C,D#";
                break;
            case "Gdim":
                chordnotes = "G,A#,C#,E";
                break;
            case "G#dim":
                chordnotes = "G#,B,D,F";
                break;

            // MINOR 7 FLAT 5 (HALF DIMINISHED)
            case "Am7b5":
                chordnotes = "A,C,D#,G";
                break;
            case "A#m7b5":
                chordnotes = "A#,C#,E,G#";
                break;
            case "Bm7b5":
                chordnotes = "B,D,F,A";
                break;
            case "Cm7b5":
                chordnotes = "C,D#,F#,A#";
                break;
            case "C#m7b5":
                chordnotes = "C#,E,G,B";
                break;
            case "Dm7b5":
                chordnotes = "D,F,G#,C";
                break;
            case "D#m7b5":
                chordnotes = "D#,F#,A,C#";
                break;
            case "Em7b5":
                chordnotes = "E,G,A#,D";
                break;
            case "Fm7b5":
                chordnotes = "F,G#,B,D#";
                break;
            case "F#m7b5":
                chordnotes = "F#,A,C,E";
                break;
            case "Gm7b5":
                chordnotes = "G,A#,C#,F";
                break;
            case "G#m7b5":
                chordnotes = "G#,B,D,F#";
                break;

            // AUGMENTED (#5)
            case "Aaug":
                chordnotes = "A,C#,F";
                break;
            case "A#aug":
                chordnotes = "A#,D,F#";
                break;
            case "Baug":
                chordnotes = "B,D#,G";
                break;
            case "Caug":
                chordnotes = "C,E,G#";
                break;
            case "C#aug":
                chordnotes = "C#,F,A";
                break;
            case "Daug":
                chordnotes = "D,F#,A#";
                break;
            case "D#aug":
                chordnotes = "D#,G,B";
                break;
            case "Eaug":
                chordnotes = "E,G#,C";
                break;
            case "Faug":
                chordnotes = "F,A,C#";
                break;
            case "F#aug":
                chordnotes = "F#,A#,D";
                break;
            case "Gaug":
                chordnotes = "G,B,D#";
                break;
            case "G#aug":
                chordnotes = "G#,C,E";
                break;
            default:
                chordnotes = "";
                break;
        }

        return chordnotes;
    }
}
