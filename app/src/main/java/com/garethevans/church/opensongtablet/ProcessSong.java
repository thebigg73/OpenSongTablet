package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class ProcessSong extends Activity {

    public static String parseLyrics(String myLyrics) {
        myLyrics = myLyrics.replace("\n \n","\n\n");
        myLyrics = myLyrics.replace("]\n\n","]\n");
        myLyrics = myLyrics.replace("\n\n","\n");
        myLyrics = myLyrics.replaceAll("\r\n", "\n");
        myLyrics = myLyrics.replaceAll("\r", "\n");
        myLyrics = myLyrics.replaceAll("\t", "    ");
        myLyrics = myLyrics.replaceAll("\\t", "    ");
        myLyrics = myLyrics.replaceAll("\b", "    ");
        myLyrics = myLyrics.replaceAll("\f", "    ");
        myLyrics = myLyrics.replace("\r", "");
        myLyrics = myLyrics.replace("\t", "    ");
        myLyrics = myLyrics.replace("\b", "    ");
        myLyrics = myLyrics.replace("\f", "    ");
        myLyrics = myLyrics.replace("&#x27;", "'");
        myLyrics = myLyrics.replaceAll("\u0092", "'");
        myLyrics = myLyrics.replaceAll("\u0093", "'");
        myLyrics = myLyrics.replaceAll("\u2018", "'");
        myLyrics = myLyrics.replaceAll("\u2019", "'");

        if (!FullscreenActivity.whichSongFolder.contains(FullscreenActivity.slide) && !FullscreenActivity.whichSongFolder.contains(FullscreenActivity.image) && !FullscreenActivity.whichSongFolder.contains(FullscreenActivity.note) && !FullscreenActivity.whichSongFolder.contains(FullscreenActivity.scripture)) {
            myLyrics = myLyrics.replace("Slide 1", "[V1]");
            myLyrics = myLyrics.replace("Slide 2", "[V2]");
            myLyrics = myLyrics.replace("Slide 3", "[V3]");
            myLyrics = myLyrics.replace("Slide 4", "[V4]");
            myLyrics = myLyrics.replace("Slide 5", "[V5]");
        }

        // Make double tags into single ones
        myLyrics = myLyrics.replace("[[", "[");
        myLyrics = myLyrics.replace("]]", "]");

        // Make lowercase start tags into caps
        myLyrics = myLyrics.replace("[v", "[V");
        myLyrics = myLyrics.replace("[b", "[B");
        myLyrics = myLyrics.replace("[c", "[C");
        myLyrics = myLyrics.replace("[t", "[T");
        myLyrics = myLyrics.replace("[p", "[P");

        // Try to convert ISO / Windows
        myLyrics = myLyrics.replace("\0x91", "'");

        // Get rid of BOMs and stuff
        myLyrics = myLyrics.replace("\uFEFF","");
        myLyrics = myLyrics.replace("\uFEFF","");
        myLyrics = myLyrics.replace("[&#x27;]","");
        myLyrics = myLyrics.replace("[\\xEF]","");
        myLyrics = myLyrics.replace("[\\xBB]","");
        myLyrics = myLyrics.replace("[\\xFF]","");
        myLyrics = myLyrics.replace("\\xEF","");
        myLyrics = myLyrics.replace("\\xBB","");
        myLyrics = myLyrics.replace("\\xFF","");

        return myLyrics;
    }

    public static String removeUnderScores(String myLyrics) {
        // Go through the lines and remove underscores if the line isn't an image location
        // Split the lyrics into a line by line array so we can fix individual lines
        String[] lineLyrics = myLyrics.split("\n");
        myLyrics = "";
        for (int l=0;l<lineLyrics.length;l++) {

            if (lineLyrics[l].contains("_")) {
                if (l>0 && !lineLyrics[l].contains("["+FullscreenActivity.image+"_") && !lineLyrics[l-1].contains("["+FullscreenActivity.image+"_")) {
                    if (!FullscreenActivity.showChords) {
                        lineLyrics[l] = lineLyrics[l].replace("_","");
                    } else {
                        lineLyrics[l] = lineLyrics[l].replace("_"," ");
                    }
                } else if (l==0 && !lineLyrics[l].contains("["+FullscreenActivity.image+"_")) {
                    if (!FullscreenActivity.showChords || FullscreenActivity.whichMode.equals("Presenter")) {
                        lineLyrics[l] = lineLyrics[l].replace("_","");
                    } else {
                        lineLyrics[l] = lineLyrics[l].replace("_"," ");
                    }
                }
            }
            myLyrics += lineLyrics[l] + "\n";
        }
        return myLyrics;
    }

    public static void lookForSplitPoints() {
        // Script to determine 2 column split details
        int halfwaypoint = Math.round(FullscreenActivity.numrowstowrite / 2);
        // Look for nearest split point before halfway
        int splitpoint_1sthalf = 0;
        boolean gotityet = false;
        for (int scan = halfwaypoint; scan > 0; scan--) {
            if (!gotityet) {
                if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
                    gotityet = true;
                    splitpoint_1sthalf = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
                    gotityet = true;
                    splitpoint_1sthalf = scan + 1;
                }
            }
        }

        // Look for nearest split point past halfway
        int splitpoint_2ndhalf = FullscreenActivity.numrowstowrite;
        boolean gotityet2 = false;
        for (int scan = halfwaypoint; scan < FullscreenActivity.numrowstowrite; scan++) {
            if (!gotityet2) {
                if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
                    gotityet2 = true;
                    splitpoint_2ndhalf = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
                    gotityet2 = true;
                    splitpoint_2ndhalf = scan + 1;
                }
            }
        }

        // Script to determine 3 columns split details
        int thirdwaypoint = Math.round(FullscreenActivity.numrowstowrite / 3);
        int twothirdwaypoint = thirdwaypoint * 2;

        // Look for nearest split point before thirdway
        int splitpoint_beforethirdway = 0;
        boolean gotityet_beforethirdway = false;
        for (int scan = thirdwaypoint; scan > 0; scan--) {
            if (!gotityet_beforethirdway) {
                if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
                    gotityet_beforethirdway = true;
                    splitpoint_beforethirdway = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
                    gotityet_beforethirdway = true;
                    splitpoint_beforethirdway = scan + 1;
                }
            }
        }

        // Look for nearest split point past thirdway
        int splitpoint_pastthirdway = thirdwaypoint;
        boolean gotityet_pastthirdway = false;
        for (int scan = thirdwaypoint; scan < FullscreenActivity.numrowstowrite; scan++) {
            if (!gotityet_pastthirdway) {
                if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
                    gotityet_pastthirdway = true;
                    splitpoint_pastthirdway = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
                    gotityet_pastthirdway = true;
                    splitpoint_pastthirdway = scan + 1;
                }
            }
        }

        // Look for nearest split point before twothirdway
        int splitpoint_beforetwothirdway = thirdwaypoint;
        boolean gotityet_beforetwothirdway = false;
        for (int scan = twothirdwaypoint; scan > 0; scan--) {
            if (!gotityet_beforetwothirdway) {
                if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
                    gotityet_beforetwothirdway = true;
                    splitpoint_beforetwothirdway = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
                    gotityet_beforetwothirdway = true;
                    splitpoint_beforetwothirdway = scan + 1;
                }
            }
        }

        // Look for nearest split point past twothirdway
        int splitpoint_pasttwothirdway = twothirdwaypoint;
        boolean gotityet_pasttwothirdway = false;
        for (int scan = twothirdwaypoint; scan < FullscreenActivity.numrowstowrite; scan++) {
            if (!gotityet_pasttwothirdway) {
                if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
                    gotityet_pasttwothirdway = true;
                    splitpoint_pasttwothirdway = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
                    gotityet_pasttwothirdway = true;
                    splitpoint_pasttwothirdway = scan + 1;
                }
            }
        }

        if (!gotityet_beforethirdway) {
            splitpoint_beforethirdway = 0;
        }
        if (!gotityet_pastthirdway) {
            splitpoint_pastthirdway = 0;
        }
        if (!gotityet_beforetwothirdway) {
            splitpoint_beforetwothirdway = splitpoint_beforethirdway;
        }
        if (!gotityet_pasttwothirdway) {
            splitpoint_pasttwothirdway = FullscreenActivity.numrowstowrite;
        }

        // Which is the best split point to use (closest to halfway) for 2
        // columns
        int split1stdiff = Math.abs(halfwaypoint - splitpoint_1sthalf);
        int split2nddiff = Math.abs(halfwaypoint - splitpoint_2ndhalf);

        if (split1stdiff <= split2nddiff) {
            FullscreenActivity.splitpoint = splitpoint_1sthalf;
        } else {
            FullscreenActivity.splitpoint = splitpoint_2ndhalf;
        }

        FullscreenActivity.botherwithcolumns = true;

        // Which is the best split point to use (closest to thirdway) for 3 columns
        int splitprethirddiff = Math.abs(thirdwaypoint - splitpoint_beforethirdway);
        int splitpastthirddiff = Math.abs(thirdwaypoint - splitpoint_pastthirdway);
        int splitpretwothirddiff = Math.abs(twothirdwaypoint - splitpoint_beforetwothirdway);
        int splitpasttwothirddiff = Math.abs(twothirdwaypoint - splitpoint_pasttwothirdway);

        if (splitprethirddiff <= splitpastthirddiff) {
            FullscreenActivity.thirdsplitpoint = splitpoint_beforethirdway;
        } else {
            FullscreenActivity.thirdsplitpoint = splitpoint_pastthirdway;
        }

        if (splitpretwothirddiff <= splitpasttwothirddiff) {
            FullscreenActivity.twothirdsplitpoint = splitpoint_beforetwothirdway;
        } else {
            FullscreenActivity.twothirdsplitpoint = splitpoint_pasttwothirdway;
        }
    }

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
            case "2/2":
                FullscreenActivity.timesigindex = 1;
                FullscreenActivity.beats = 2;
                FullscreenActivity.noteValue = 2;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "2/4":
                FullscreenActivity.timesigindex = 2;
                FullscreenActivity.beats = 2;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "3/2":
                FullscreenActivity.timesigindex = 3;
                FullscreenActivity.beats = 3;
                FullscreenActivity.noteValue = 2;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "3/4":
                FullscreenActivity.timesigindex = 4;
                FullscreenActivity.beats = 3;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "3/8":
                FullscreenActivity.timesigindex = 5;
                FullscreenActivity.beats = 3;
                FullscreenActivity.noteValue = 8;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "4/4":
                FullscreenActivity.timesigindex = 6;
                FullscreenActivity.beats = 4;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "5/4":
                FullscreenActivity.timesigindex = 7;
                FullscreenActivity.beats = 5;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "5/8":
                FullscreenActivity.timesigindex = 8;
                FullscreenActivity.beats = 5;
                FullscreenActivity.noteValue = 8;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "6/4":
                FullscreenActivity.timesigindex = 9;
                FullscreenActivity.beats = 6;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "6/8":
                FullscreenActivity.timesigindex = 10;
                FullscreenActivity.beats = 6;
                FullscreenActivity.noteValue = 8;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "7/4":
                FullscreenActivity.timesigindex = 11;
                FullscreenActivity.beats = 7;
                FullscreenActivity.noteValue = 4;
                FullscreenActivity.mTimeSigValid = true;
                break;
            case "7/8":
                FullscreenActivity.timesigindex = 12;
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
        // If the character isn't a " " and the character before is " " or "|" it's a new chord
        // Add the positions to an array
        ArrayList<String> chordpositions = new ArrayList<>();

        // Set the start of the line as the first bit
        chordpositions.add("0");

        // In order to identify chords at the end of the line
        // (My method looks for a following space)
        // Add a space to the search string.
        string += " ";

        for (int x = 1; x < string.length(); x++) {

            String thischar = "";
            boolean thischarempty = false;
            if (x<string.length()-1) {
                thischar = string.substring(x,x+1);
            }
            if (thischar.equals(" ") || thischar.equals("|")) {
                thischarempty = true;
            }

            String prevchar = "";
            boolean prevcharempty = false;
            if (x>0) {
                prevchar = string.substring(x-1,x);
            }
            if (prevchar.equals(" ") || prevchar.equals("|") || x==0) {
                prevcharempty = true;
            }

            if ((!thischarempty && prevcharempty) || (thischarempty && x==0)) {
                // This is a chord position
                chordpositions.add(x + "");
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
        int endpos = -1;

        if (string==null) {
            string="";
        }
        if (pos_string==null) {
            pos_string = new String[0];
        }

        for (int x=0;x<pos_string.length;x++) {
            if (pos_string[x].equals("0")) {
                // First chord is at the start of the line
                startpos = 0;
            } else if (x == pos_string.length - 1) {
                // Last chord, so end position is end of the line
                // First get the second last section
                endpos = Integer.parseInt(pos_string[x]);
                if (startpos < endpos) {
                    chordsections.add(string.substring(startpos, endpos));
                }

                // Now get the last one
                startpos = Integer.parseInt(pos_string[x]);
                endpos = string.length();
                if (startpos < endpos) {
                    chordsections.add(string.substring(startpos, endpos));
                }

            } else {
                // We are at the start of a chord somewhere other than the start or end
                // Get the bit of text in the previous section;
                endpos = Integer.parseInt(pos_string[x]);
                if (startpos < endpos) {
                    chordsections.add(string.substring(startpos, endpos));
                }
                startpos = endpos;
            }
        }
        if (startpos==0 && endpos==-1) {
            // This is just a chord line, so add the whole line
            chordsections.add(string);
        }
        String[] sections = new String[chordsections.size()];
        sections = chordsections.toArray(sections);

        return sections;
    }

    public static String[] getLyricSections(String string, String[] pos_string) {
        // Go through the chord positions and extract the substrings
        ArrayList<String> lyricsections = new ArrayList<>();
        int startpos = 0;
        int endpos = -1;

        if (string==null) {
            string = "";
        }
        if (pos_string==null) {
            pos_string = new String[0];
        }

        for (int x=0;x<pos_string.length;x++) {
            if (pos_string[x].equals("0")) {
                // First chord is at the start of the line
                startpos = 0;
            } else if (x == pos_string.length - 1) {
                // Last chord, so end position is end of the line
                // First get the second last section
                endpos = Integer.parseInt(pos_string[x]);
                if (startpos < endpos) {
                    lyricsections.add(string.substring(startpos, endpos));
                }

                // Now get the last one
                startpos = Integer.parseInt(pos_string[x]);
                endpos = string.length();
                if (startpos < endpos) {
                    lyricsections.add(string.substring(startpos, endpos));
                }

            } else {
                // We are at the start of a chord somewhere other than the start or end
                // Get the bit of text in the previous section;
                endpos = Integer.parseInt(pos_string[x]);
                if (startpos < endpos) {
                    lyricsections.add(string.substring(startpos, endpos));
                }
                startpos = endpos;
            }
        }

        if (startpos==0 && endpos<0) {
            // Just add the line
            lyricsections.add(string);
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
            chordhtml += "<td class=\"chord\" name=\""+bit.trim()+"\">"+bit+"</td>";
        }
        return chordhtml;
    }

    public static TableRow capolinetoTableRow(Context c, String[] chords) {
        TableRow caporow  = new TableRow(c);

        for (String bit:chords) {
            if (bit.indexOf(".")==0 && bit.length()>1) {
                bit = bit.substring(1);
            }
            TextView capobit  = new TextView(c);
            FullscreenActivity.temptranspChords = bit;
            try {
                Transpose.capoTranspose();
            } catch (Exception e) {
                e.printStackTrace();
            }
            capobit.setText(FullscreenActivity.temptranspChords);
            capobit.setTextSize(6.0f * FullscreenActivity.chordfontscalesize);
            capobit.setTextColor(FullscreenActivity.lyricsCapoColor);
            capobit.setTypeface(FullscreenActivity.chordsfont);
            caporow.addView(capobit);
        }
        return caporow;
    }

    public static TableRow chordlinetoTableRow(Context c, String[] chords) {
        TableRow chordrow = new TableRow(c);

        for (String bit:chords) {
            if (bit.indexOf(".")==0 && bit.length()>1) {
                bit = bit.substring(1);
            }
            TextView chordbit = new TextView(c);
            //bit = bit.replace("b","\u266d"); //Gives the proper b symbol if available in font
            chordbit.setText(bit);
            chordbit.setTextSize(6.0f * FullscreenActivity.chordfontscalesize);
            chordbit.setTextColor(FullscreenActivity.lyricsChordsColor);
            chordbit.setTypeface(FullscreenActivity.chordsfont);
            chordrow.addView(chordbit);
        }
        return chordrow;
    }

    public static TableRow lyriclinetoTableRow(Context c, String[] lyrics) {
        TableRow lyricrow = new TableRow(c);

        for (String bit:lyrics) {
            if (bit.indexOf(" ")==0 && bit.length()>1) {
                bit = bit.substring(1);
            }
            if (!FullscreenActivity.whichSongFolder.contains(FullscreenActivity.image)) {
                bit = bit.replace("_","");
            }
            TextView lyricbit = new TextView(c);
            lyricbit.setText(bit);
            lyricbit.setTextSize(6.0f);
            lyricbit.setTextColor(FullscreenActivity.lyricsTextColor);
            lyricbit.setTypeface(FullscreenActivity.lyricsfont);
            lyricrow.addView(lyricbit);
        }
        return lyricrow;
    }

    public static TableRow commentlinetoTableRow(Context c, String[] comment) {
        TableRow commentrow = new TableRow(c);

        for (String bit:comment) {
            if (bit.indexOf(" ")==0 && bit.length()>1) {
                bit = bit.substring(1);
            }
            if (!FullscreenActivity.whichSongFolder.contains(FullscreenActivity.image)) {
                bit = bit.replace("_","");
            }
            TextView lyricbit = new TextView(c);
            lyricbit.setText(bit);
            lyricbit.setTextSize(6.0f*FullscreenActivity.commentfontscalesize);
            lyricbit.setTextColor(FullscreenActivity.lyricsTextColor);
            lyricbit.setTypeface(FullscreenActivity.lyricsfont);
            commentrow.addView(lyricbit);
        }
        return commentrow;
    }

    public static TextView titletoTextView (Context c, String title) {
        TextView titleview = new TextView(c);
        titleview.setText(title);
        titleview.setTextColor(FullscreenActivity.lyricsTextColor);
        titleview.setTypeface(FullscreenActivity.lyricsfont);
        titleview.setTextSize(6.0f * FullscreenActivity.headingfontscalesize);
        titleview.setPaintFlags(titleview.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        return titleview;
    }

    public static String howToProcessLines(int linenum, int totallines, String thislinetype, String nextlinetype, String previouslinetype) {
        String what;
        // If this is a chord line followed by a lyric line.
        if (linenum < totallines - 1 && thislinetype.equals("chord") &&
                (nextlinetype.equals("lyric") || nextlinetype.equals("comment"))) {
            what = "chord_then_lyric";
        } else if (thislinetype.equals("chord") && (nextlinetype.equals("") || nextlinetype.equals("chord"))) {
            what = "chord_only";
        } else if (thislinetype.equals("lyric") && !previouslinetype.equals("chord")) {
            what = "lyric_no_chord";
        } else if (thislinetype.equals("comment") && !previouslinetype.equals("chord")) {
            what = "comment_no_chord";
        } else {
            what = "null"; // Probably a lyric line with a chord above it - already dealt with
        }
        return what;
    }

    public static String lyriclinetoHTML(String[] lyrics) {
        String lyrichtml = "";
        for (String bit:lyrics) {
            lyrichtml += "<td class=\"lyric\">"+bit.replace(" ","&nbsp;")+"</td>";
        }
        return lyrichtml;
    }

    public static String[] beautifyHeadings(String string) {

        string = string.replace("[","");
        string = string.replace("]","");
        String section;

        if (!FullscreenActivity.foundSongSections_heading.contains(string)) {
            FullscreenActivity.foundSongSections_heading.add(string);
        }

        switch (string) {
            case "V":
            case "V1":
            case "V2":
            case "V3":
            case "V4":
            case "V5":
            case "V6":
            case "V7":
            case "V8":
            case "V9":
            case "V10":
                string = string.replace("V", FullscreenActivity.tag_verse + " ");
                section = "verse";
                break;
            case "T":
            case "T1":
            case "T2":
            case "T3":
            case "T4":
            case "T5":
            case "T6":
            case "T7":
            case "T8":
            case "T9":
            case "T10":
                string = string.replace("T", FullscreenActivity.tag_tag + " ");
                section = "tag";
                break;
            case "C":
            case "C1":
            case "C2":
            case "C3":
            case "C4":
            case "C5":
            case "C6":
            case "C7":
            case "C8":
            case "C9":
            case "C10":
                string = string.replace("C", FullscreenActivity.tag_chorus + " ");
                section = "chorus";
                break;
            case "B":
            case "B1":
            case "B2":
            case "B3":
            case "B4":
            case "B5":
            case "B6":
            case "B7":
            case "B8":
            case "B9":
            case "B10":
                string = string.replace("B", FullscreenActivity.tag_bridge + " ");
                section = "bridge";
                break;
            case "P":
            case "P1":
            case "P2":
            case "P3":
            case "P4":
            case "P5":
            case "P6":
            case "P7":
            case "P8":
            case "P9":
            case "P10":
                string = string.replace("P", FullscreenActivity.tag_prechorus + " ");
                section = "prechorus";
                break;
            default:
                section = "custom";
        }
        String[] vals = new String[2];
        vals[0] = string;
        vals[1] = section;
        return vals;
    }

    public static String fixLineLength(String string,int newlength) {
        int extraspacesrequired = newlength - string.length();
        for (int x=0; x<extraspacesrequired; x++) {
            string += " ";
        }
        return string;
    }

    public static String songHTML (String string) {
        return  "" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                ".page       {background-color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsBackgroundColor)) + ";}\n" +
                ".heading    {color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; text-decoration:underline}\n" +
                ".lyrictable {border-spacing:0; border-collapse: collapse; border:0px;}\n" +
                SetTypeFace.setupWebViewLyricFont(FullscreenActivity.mylyricsfontnum) +
                SetTypeFace.setupWebViewChordFont(FullscreenActivity.mychordsfontnum) +
                "</style>\n" +
                "</head>\n" +
                "<body class=\"page\"\">\n" +
                "<table id=\"mysection\">\n" +
                "<tr>\n" +
                "<td>\n" +

                string +

                "</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";
    }

    public static int getSectionColors(String type) {
        int colortouse;
        switch (type) {
            case "verse":
                colortouse = FullscreenActivity.lyricsVerseColor;
                break;
            case "chorus":
                colortouse = FullscreenActivity.lyricsChorusColor;
                break;
            case "prechorus":
                colortouse = FullscreenActivity.lyricsPreChorusColor;
                break;
            case "bridge":
                colortouse = FullscreenActivity.lyricsBridgeColor;
                break;
            case "tag":
                colortouse = FullscreenActivity.lyricsTagColor;
                break;
            case "comment":
                colortouse = FullscreenActivity.lyricsCommentColor;
                break;
            default:
                colortouse = FullscreenActivity.lyricsCustomColor;
                break;
        }
        return colortouse;
    }

    public static String fixMultiLineFormat(String string) {
        // Best way to determine if the song is in multiline format is
        // Look for [v] or [c] case insensitive
        // And it needs to be followed by a line starting with 1 and 2
        boolean has_multiline_vtag = string.toLowerCase(FullscreenActivity.locale).contains("[v]");
        boolean has_multiline_ctag = string.toLowerCase(FullscreenActivity.locale).contains("[c]");
        boolean has_multiline_1tag = string.toLowerCase(FullscreenActivity.locale).contains("\n1");
        boolean has_multiline_2tag = string.toLowerCase(FullscreenActivity.locale).contains("\n2");

        if ((has_multiline_vtag || has_multiline_ctag) && has_multiline_1tag && has_multiline_2tag) {

            // Ok the song is in the multiline format
            // [V]
            // .G     C
            // 1Verse 1
            // 2Verse 2

            // Create empty verse and chorus strings up to 9 verses/choruses
            String[] verse = {"", "", "", "", "", "", "", "", ""};
            String[] chorus = {"", "", "", "", "", "", "", "", ""};

            String versechords = "";
            String choruschords = "";

            // Split the string into separate lines
            String[] lines = string.split("\n");

            // Go through the lines and look for tags and line numbers
            boolean gettingverse = false;
            boolean gettingchorus = false;
            for (int z = 0; z < lines.length; z++) {
                if ((lines[z].toLowerCase(FullscreenActivity.locale).indexOf("[v]") == 0 &&
                        ((z<lines.length-1 && lines[z+1].startsWith("1")) || (z<lines.length-2 && lines[z+2].startsWith("1")))) ||

                        ((lines[z].toLowerCase(FullscreenActivity.locale).indexOf("[" + FullscreenActivity.tag_verse.toLowerCase(FullscreenActivity.locale) + "]") == 0) &&
                                ((z<lines.length-1 && lines[z+1].startsWith("1")) || (z<lines.length-2 && lines[z+2].startsWith("1"))))) {
                    lines[z] = "__VERSEMULTILINE__";
                    gettingverse = true;
                    gettingchorus = false;
                }
                if ((lines[z].toLowerCase(FullscreenActivity.locale).indexOf("[c]") == 0 &&
                            ((z<lines.length-1 && lines[z+1].startsWith("1")) || (z<lines.length-2 && lines[z+2].startsWith("1")))) ||

                            ((lines[z].toLowerCase(FullscreenActivity.locale).indexOf("[" + FullscreenActivity.tag_chorus.toLowerCase(FullscreenActivity.locale) + "]") == 0) &&
                                    ((z<lines.length-1 && lines[z+1].startsWith("1")) || (z<lines.length-2 && lines[z+2].startsWith("1"))))) {
                    lines[z] = "__CHORUSMULTILINE__";
                    gettingchorus = true;
                    gettingverse = false;
                } else if (lines[z].indexOf("[") == 0) {
                    gettingchorus = false;
                    gettingverse = false;
                }

                if (gettingverse) {
                    if (lines[z].startsWith(".")) {
                        versechords += lines[z] + "\n";
                        lines[z] = "__REMOVED__";
                    } else if (Character.isDigit((lines[z] + " ").charAt(0))) {
                        int vnum = Integer.parseInt((lines[z] + " ").substring(0, 1));
                        if (verse[vnum].equals("")) {
                            verse[vnum] = "[V" + vnum + "]\n";
                        }
                        verse[vnum] += lines[z].substring(1) + "\n";
                        lines[z] = "__REMOVED__";
                    }
                } else if (gettingchorus) {
                    if (lines[z].startsWith(".")) {
                        choruschords += lines[z] + "\n";
                        lines[z] = "__REMOVED__";
                    } else if (Character.isDigit((lines[z] + " ").charAt(0))) {
                        int cnum = Integer.parseInt((lines[z] + " ").substring(0, 1));
                        if (chorus[cnum].equals("")) {
                            chorus[cnum] = "[C" + cnum + "]\n";
                        }
                        chorus[cnum] += lines[z].substring(1) + "\n";
                        lines[z] = "__REMOVED__";
                    }
                }


            }

            // Get the replacement text
            String versereplacement = addchordstomultiline(verse, versechords);
            String chorusreplacement = addchordstomultiline(chorus, choruschords);

            // Now go back through the lines and extract the new improved version
            String improvedlyrics = "";
            for (String thisline : lines) {
                if (thisline.equals("__VERSEMULTILINE__")) {
                    thisline = versereplacement;
                } else if (thisline.equals("__CHORUSMULTILINE__")) {
                    thisline = chorusreplacement;
                }
                if (!thisline.equals("__REMOVED__")) {
                    improvedlyrics += thisline + "\n";
                }
            }

            return improvedlyrics;
        } else {
            // Not multiline format
            return string;
        }
    }

    public static String[] removeTagLines(String[] sections) {
        for (int x=0; x<sections.length; x++) {
            int start = sections[x].indexOf("[");
            int end = sections[x].indexOf("]");
            if (end>start && start>-1) {
                String remove1 = sections[x].substring(start,end+1) + "\n";
                String remove2 = sections[x].substring(start,end+1);
                sections[x] = sections[x].replace(remove1,"");
                sections[x] = sections[x].replace(remove2,"");
            }
        }
        return sections;
    }

    public static String removeChordLines(String song) {
        // Split the song into separate lines
        String[] lines = song.split("\n");
        String newsong = "";

        for (String thisline:lines) {
            if (!thisline.startsWith(".")) {
                newsong += thisline + "\n";
            }
        }
        return newsong;
    }

    public static String getAllChords(String song) {
        // Split the song into separate lines
        String[] lines = song.split("\n");
        String chordsonly = "";

        for (String thisline:lines) {
            if (thisline.startsWith(".")) {
                chordsonly += thisline + " ";
            }
        }
        return chordsonly.replace("."," ");
    }

    public static String removeCommentLines (String song) {
        // Split the song into separate lines
        String[] lines = song.split("\n");
        String newsong = "";

        for (String thisline:lines) {
            if (!thisline.startsWith(";")) {
                newsong += thisline + "\n";
            }
        }
        return newsong;

    }

    public static String addchordstomultiline(String[] multiline, String chords) {
        String[] chordlines = chords.split("\n");
        String replacementtext = "";

        // Go through each verse/chorus in turn
        for (String sections:multiline) {
            String[] section = sections.split("\n");

            if (section.length == chordlines.length+1) {
                replacementtext += section[0] + "\n";
                // Only works if there are the same number of lyric lines as chords!
                for (int x=0; x<chordlines.length; x++) {
                    replacementtext += chordlines[x]+"\n"+section[x+1]+"\n";
                }
                replacementtext += "\n";
            } else {
                replacementtext += sections+"\n";
            }
        }
        return replacementtext;
    }

    public static String[] splitSongIntoSections(String song) {

        song = song.replace("-!!", "");
        song = song.replace("||", "%%LATERSPLITHERE%%");
        // Need to go back to chord lines that might have to have %%LATERSPLITHERE%% added
        if (!FullscreenActivity.whichSongFolder.contains(FullscreenActivity.scripture)) {
            song = song.replace("\n\n", "%%__SPLITHERE__%%");
            song = song.replace("---", "");
        } else {
            song = song.replace("---", "[]");
        }
        if (FullscreenActivity.presenterChords.equals("N")) {
            song = song.replace("|", "\n");
        } else {
            song = song.replace("|", " ");
        }
        song = song.replace("\n[","\n%%__SPLITHERE__%%\n[");

        // Get rid of double splits
        song = song.replace("%%__SPLITHERE__%%%%__SPLITHERE__%%","%%__SPLITHERE__%%");
        song = song.replace("%%__SPLITHERE__%%\n%%__SPLITHERE__%%","%%__SPLITHERE__%%");
        song = song.replace("%%__SPLITHERE__%%\n\n%%__SPLITHERE__%%","%%__SPLITHERE__%%");
        song = song.replace("%%__SPLITHERE__%%\n \n%%__SPLITHERE__%%","%%__SPLITHERE__%%");
        song = song.replace("\n%%__SPLITHERE__%%","%%__SPLITHERE__%%");
        song = song.replace("%%__SPLITHERE__%%\n","%%__SPLITHERE__%%");

        // Check that we don't have empty sections
        String[] check = song.split("%%__SPLITHERE__%%");
        String newsong = "";
        for (String checkthis:check) {
            if (checkthis!=null && !checkthis.isEmpty() && !checkthis.equals("") && !checkthis.equals(" ")) {
                newsong += checkthis + "%%__SPLITHERE__%%";
            }
        }

        return newsong.split("%%__SPLITHERE__%%");
    }

    public static String[] splitLaterSplits(String[] currsections) {
        ArrayList<String> newbits = new ArrayList<>();
        for (int z=0; z<currsections.length; z++) {
            // If currsection doesn't have extra split points, add this section to the array
            if (currsections[z]!=null && !currsections[z].contains("%%LATERSPLITHERE%%")) {
                newbits.add(currsections[z]);
            } else {
                String[] splitcurr;
                // If a section has LATERSPLITHERE in it, we need to fix it for the chords we need to extract the chords
                if (currsections[z].contains("%%LATERSPLITHERE%%")) {
                    String[] tempsection = currsections[z].split("\n");
                    for (int line = 0; line < tempsection.length; line++) {
                        // Go through each line and look for %%LATERSPLITHERE%%
                        if (tempsection[line].contains("%%LATERSPLITHERE%%") && line > 0 && tempsection[line - 1].startsWith(".")) {
                            int pos = tempsection[line].indexOf("%%LATERSPLITHERE%%");
                            String grabbedchords = "%%LATERSPLITHERE%%";
                            if (pos>-1 && (pos+2) < tempsection[line - 1].length()) {
                                grabbedchords += "." + tempsection[line - 1].substring(pos+2) + "\n";
                                tempsection[line - 1] = tempsection[line - 1].substring(0, pos);
                            }
                            tempsection[line] = tempsection[line].replace("%%LATERSPLITHERE%%", grabbedchords);
                        }
                    }

                    // Put the section back as the tempsection
                    currsections[z] = "";
                    for (String thisline:tempsection) {
                        currsections[z] += thisline +"\n";
                    }
                    splitcurr = currsections[z].split("%%LATERSPLITHERE%%");
                    Collections.addAll(newbits, splitcurr);
                }
            }
        }
        if (newbits.size() < 1) {
            newbits.add("");
        }
        // Now make a new String array
        String[] updatedSections = new String[newbits.size()];
        for (int y=0;y<newbits.size();y++) {
            updatedSections[y] = newbits.get(y);
        }
        return updatedSections;
    }

    public static void collapseSections() {
        //FullscreenActivity.songSections = new String[1];
        //FullscreenActivity.songSections[0] = FullscreenActivity.myLyrics;
        //FullscreenActivity.songSectionsTypes = new String[1];
        //FullscreenActivity.sectionContents = new String[1][1];
        //FullscreenActivity.sectionContents[0][0] = FullscreenActivity.myLyrics;
        //FullscreenActivity.sectionviews = new View[1];
        //FullscreenActivity.sectionScaleValue = new float[1];
        //FullscreenActivity.sectionrendered = new boolean[1];
        //FullscreenActivity.viewwidth = new int[1];
        //FullscreenActivity.viewheight = new int[1];
    }

    public static String getSectionHeadings(String songsection) {
        String label = "";
        songsection = songsection.trim();
        if (songsection.indexOf("[")==0) {
            int startoftag = songsection.indexOf("[");
            int endoftag = songsection.indexOf("]");
            if (endoftag < startoftag) {
                endoftag = songsection.length() - 1;
            }
            label = songsection.substring(startoftag + 1, endoftag);
        }
        return label;
    }

    public static String[] matchPresentationOrder(String[] currentSections) {

        // Get the currentSectionLabels - these will change after we reorder the song
        String[] currentSectionLabels = new String[currentSections.length];
        for (int sl=0; sl < currentSections.length; sl++) {
            currentSectionLabels[sl] = ProcessSong.getSectionHeadings(currentSections[sl]);
        }

        // mPresentation probably looks like "Intro V1 V2 C V3 C C Guitar Solo C Outro"
        // We need to identify the sections in the song that are in here
        // What if sections aren't in the song (e.g. Intro V2 and Outro)
        // The other issue is that custom tags (e.g. Guitar Solo) can have spaces in them

        String tempPresentationOrder = FullscreenActivity.mPresentation + " ";
        String errors = "";

        // Go through each tag in the song
        for (String tag:currentSectionLabels) {
            if (tag.equals("") || tag.equals(" ")) {
                Log.d("d","Empty search");
            } else if (tempPresentationOrder.contains(tag)) {
                tempPresentationOrder = tempPresentationOrder.replace(tag + " ", "<__" + tag + "__>");
            } else {
                errors += tag + " - not found in presentation order\n";
            }
        }

        // tempPresentationOrder now looks like "Intro <__V1__>V2 <__C__><__V3__><__C__><__C__><__Guitar Solo__><__C__>Outro "
        // Assuming V2 and Outro aren't in the song anymore
        // Split the string by <__
        String[] tempPresOrderArray = tempPresentationOrder.split("<__");
        // tempPresOrderArray now looks like "Intro ", "V1__>V2 ", "C__>", "V3__>", "C__>", "C__>", "Guitar Solo__>", "C__>Outro "
        // So, if entry doesn't contain __> it isn't in the song
        // Also, anything after __> isn't in the song
        for (int d=0; d<tempPresOrderArray.length; d++) {
            if (!tempPresOrderArray[d].contains("__>")) {
                if (!tempPresOrderArray[d].equals("") && !tempPresOrderArray[d].equals(" ")) {
                    errors += tempPresOrderArray[d] + " - not found in song\n";
                }
                tempPresOrderArray[d] = "";
                // tempPresOrderArray now looks like "", "V1__>V2 ", "C__>", "V3__>", "C__>", "C__>", "Guitar Solo__>", "C__>Outro "
            } else {
                String goodbit = tempPresOrderArray[d].substring(0,tempPresOrderArray[d].indexOf("__>"));
                String badbit = tempPresOrderArray[d].replace(goodbit+"__>","");
                tempPresOrderArray[d] = goodbit;
                if (!badbit.equals("") && !badbit.equals(" ")) {
                    errors += badbit + " - not found in song\n";
                }
                // tempPresOrderArray now looks like "", "V1", "C", "V3", "C", "C", "Guitar Solo", "C"
            }
        }

        // Go through the tempPresOrderArray and add the sections back together as a string
        String newSongText = "";

        for (String aTempPresOrderArray : tempPresOrderArray) {
            if (!aTempPresOrderArray.equals("")) {
                for (int a = 0; a < currentSectionLabels.length; a++) {
                    if (currentSectionLabels[a].trim().equals(aTempPresOrderArray.trim())) {
                        newSongText += currentSections[a] + "\n";
                    }
                }
            }
        }

        // Display any errors
        FullscreenActivity.myToastMessage = errors;

        return splitSongIntoSections(newSongText);

    }

    public static String getSongAndAuthor() {
        // If key is set
        String keytext = "";
        if (!FullscreenActivity.mKey.isEmpty() && !FullscreenActivity.mKey.equals("")) {
            keytext = " (" + FullscreenActivity.mKey + ")";
        }
        return FullscreenActivity.songfilename + keytext + "\n" + FullscreenActivity.mAuthor;
    }

    public static LinearLayout songSectionView(Context c, int x) {

            LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            final LinearLayout ll = new LinearLayout(c);
            ll.setLayoutParams(llparams);
            ll.setOrientation(LinearLayout.VERTICAL);

            // Add section title
            String[] returnvals = beautifyHeadings(FullscreenActivity.songSectionsLabels[x]);
            ll.addView(ProcessSong.titletoTextView(c, returnvals[0]));

            // Identify the section type
            FullscreenActivity.songSectionsTypes[x] = returnvals[1];
            int linenums = FullscreenActivity.sectionContents[x].length;

        String mCapo = FullscreenActivity.mCapo;
        if (mCapo==null || mCapo.isEmpty() || mCapo.equals("")) {
            mCapo = "0";
        }
        int mcapo = Integer.parseInt(mCapo);
        boolean showchords = FullscreenActivity.showChords;
        boolean showcapochords = FullscreenActivity.showCapoChords;
        boolean shownativeandcapochords = FullscreenActivity.showNativeAndCapoChords;
        boolean transposablechordformat = !FullscreenActivity.oldchordformat.equals("4") && !FullscreenActivity.oldchordformat.equals("5");

        // Decide if capo chords are valid and should be shown
        boolean docapochords = showchords && showcapochords && mcapo>0 && mcapo<12 && transposablechordformat;

        // Decide if normal chords should be shown
        // They can't be shown if showchords is true but shownativeandcapochords is false;
        boolean justcapo = docapochords && !shownativeandcapochords;
        boolean donativechords = showchords && !justcapo;

        for (int y = 0; y < linenums; y++) {
                // Go through the section a line at a time
                String nextlinetype = "";
                String previouslinetype = "";
                if (y < linenums - 1) {
                    nextlinetype = FullscreenActivity.sectionLineTypes[x][y + 1];
                }
                if (y > 0) {
                    previouslinetype = FullscreenActivity.sectionLineTypes[x][y - 1];
                }

                String[] positions_returned;
                String[] chords_returned;
                String[] lyrics_returned;
                TableLayout tl = new TableLayout(c);

                switch (ProcessSong.howToProcessLines(y, linenums, FullscreenActivity.sectionLineTypes[x][y], nextlinetype, previouslinetype)) {
                    // If this is a chord line followed by a lyric line.
                    case "chord_then_lyric":
                        if (FullscreenActivity.sectionContents[x][y].length() > FullscreenActivity.sectionContents[x][y + 1].length()) {
                            FullscreenActivity.sectionContents[x][y + 1] = ProcessSong.fixLineLength(FullscreenActivity.sectionContents[x][y + 1], FullscreenActivity.sectionContents[x][y].length());
                        }
                        positions_returned = ProcessSong.getChordPositions(FullscreenActivity.sectionContents[x][y]);
                        chords_returned = ProcessSong.getChordSections(FullscreenActivity.sectionContents[x][y], positions_returned);
                        lyrics_returned = ProcessSong.getLyricSections(FullscreenActivity.sectionContents[x][y + 1], positions_returned);
                        if (docapochords) {
                            tl.addView(ProcessSong.capolinetoTableRow(c, chords_returned));
                        }
                        if (!justcapo && donativechords) {
                            tl.addView(ProcessSong.chordlinetoTableRow(c, chords_returned));
                        }
                        if (FullscreenActivity.showLyrics) {
                            tl.addView(ProcessSong.lyriclinetoTableRow(c, lyrics_returned));
                        }
                        break;

                    case "chord_only":
                        chords_returned = new String[1];
                        chords_returned[0] = FullscreenActivity.sectionContents[x][y];
                        if (docapochords) {
                            tl.addView(ProcessSong.capolinetoTableRow(c, chords_returned));
                        }
                        if (!justcapo && donativechords) {
                            tl.addView(ProcessSong.chordlinetoTableRow(c, chords_returned));
                        }
                        break;

                    case "lyric_no_chord":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = FullscreenActivity.sectionContents[x][y];
                        if (FullscreenActivity.showLyrics) {
                            tl.addView(ProcessSong.lyriclinetoTableRow(c, lyrics_returned));
                        }
                        break;

                    case "comment_no_chord":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = FullscreenActivity.sectionContents[x][y];
                        tl.addView(ProcessSong.commentlinetoTableRow(c, lyrics_returned));
                        tl.setBackgroundColor(FullscreenActivity.lyricsCommentColor);
                        break;

                }
                ll.addView(tl);
            }
        return ll;
    }

    public static TextView createTextView(Context c, String text, float size, int textcolor, Typeface typeface) {
        TextView newtextbox = new TextView(c);
        newtextbox.setText(text);
        newtextbox.setTextSize(size);
        newtextbox.setTextColor(textcolor);
        newtextbox.setTypeface(typeface);

        return newtextbox;
    }

    public static LinearLayout createLinearLayout(Context c) {
        LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        final LinearLayout ll = new LinearLayout(c);
        ll.setLayoutParams(llparams);
        ll.setOrientation(LinearLayout.VERTICAL);
        return ll;
    }

}