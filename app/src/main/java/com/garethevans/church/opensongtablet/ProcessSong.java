package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfRenderer;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ProcessSong extends Activity {

    static String parseLyrics(String myLyrics, Context c) {
        myLyrics = myLyrics.replace("]\n\n","]\n");
        myLyrics = myLyrics.replaceAll("\r\n", "\n");
        myLyrics = myLyrics.replaceAll("\r", "\n");
        myLyrics = myLyrics.replaceAll("\\t", "    ");
        myLyrics = myLyrics.replaceAll("\f", "    ");
        myLyrics = myLyrics.replace("\r", "");
        myLyrics = myLyrics.replace("\t", "    ");
        myLyrics = myLyrics.replace("\b", "    ");
        myLyrics = myLyrics.replace("\f", "    ");
        myLyrics = myLyrics.replace("&#x27;", "'");
        myLyrics = myLyrics.replace("&#39;","'");
        myLyrics = myLyrics.replaceAll("\u0092", "'");
        myLyrics = myLyrics.replaceAll("\u0093", "'");
        myLyrics = myLyrics.replaceAll("\u2018", "'");
        myLyrics = myLyrics.replaceAll("\u2019", "'");

        if (!FullscreenActivity.whichSongFolder.contains(c.getResources().getString(R.string.slide)) &&
                !FullscreenActivity.whichSongFolder.contains(c.getResources().getString(R.string.image)) &&
                !FullscreenActivity.whichSongFolder.contains(c.getResources().getString(R.string.note)) &&
                !FullscreenActivity.whichSongFolder.contains(c.getResources().getString(R.string.scripture))) {
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

        // Replace [Verse] with [V] and [Verse 1] with [V1]
        String languageverse = c.getResources().getString(R.string.tag_verse);
        myLyrics = myLyrics.replace("["+languageverse+"]","[V]");
        myLyrics = myLyrics.replace("["+languageverse+" 1]","[V1]");
        myLyrics = myLyrics.replace("["+languageverse+" 2]","[V2]");
        myLyrics = myLyrics.replace("["+languageverse+" 3]","[V3]");
        myLyrics = myLyrics.replace("["+languageverse+" 4]","[V4]");
        myLyrics = myLyrics.replace("["+languageverse+" 5]","[V5]");
        myLyrics = myLyrics.replace("["+languageverse+" 6]","[V6]");
        myLyrics = myLyrics.replace("["+languageverse+" 7]","[V7]");
        myLyrics = myLyrics.replace("["+languageverse+" 8]","[V8]");
        myLyrics = myLyrics.replace("["+languageverse+" 9]","[V9]");

        // Replace [Chorus] with [C] and [Chorus 1] with [C1]
        String languagechorus = c.getResources().getString(R.string.tag_chorus);
        myLyrics = myLyrics.replace("["+languagechorus+"]","[C]");
        myLyrics = myLyrics.replace("["+languagechorus+" 1]","[C1]");
        myLyrics = myLyrics.replace("["+languagechorus+" 2]","[C2]");
        myLyrics = myLyrics.replace("["+languagechorus+" 3]","[C3]");
        myLyrics = myLyrics.replace("["+languagechorus+" 4]","[C4]");
        myLyrics = myLyrics.replace("["+languagechorus+" 5]","[C5]");
        myLyrics = myLyrics.replace("["+languagechorus+" 6]","[C6]");
        myLyrics = myLyrics.replace("["+languagechorus+" 7]","[C7]");
        myLyrics = myLyrics.replace("["+languagechorus+" 8]","[C8]");
        myLyrics = myLyrics.replace("["+languagechorus+" 9]","[C9]");

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

    static String fixStartOfLines(String lyrics) {
        String fixedlyrics = "";
        String[] lines = lyrics.split("\n");

        for (String line:lines) {
            if (!line.startsWith("[") && !line.startsWith(";") && !line.startsWith(".") && !line.startsWith(" ")) {
                line = " " + line;
            }
            fixedlyrics += line + "\n";
        }
        return fixedlyrics;
    }

    static String removeUnderScores(String myLyrics, Context c) {
        // Go through the lines and remove underscores if the line isn't an image location
        // Split the lyrics into a line by line array so we can fix individual lines
        String[] lineLyrics = myLyrics.split("\n");
        myLyrics = "";
        for (int l=0;l<lineLyrics.length;l++) {

            if (lineLyrics[l].contains("_")) {
                if (l>0 && !lineLyrics[l].contains("["+c.getResources().getString(R.string.image)+"_") &&
                        !lineLyrics[l-1].contains("["+c.getResources().getString(R.string.image)+"_")) {
                    if (FullscreenActivity.whichMode.equals("Presentation") && !FullscreenActivity.presoShowChords) {
                        lineLyrics[l] = lineLyrics[l].replace("_", "");
                    } else if ((FullscreenActivity.whichMode.equals("Stage") || FullscreenActivity.whichMode.equals("Performance")) &&
                            !FullscreenActivity.showChords) {
                        lineLyrics[l] = lineLyrics[l].replace("_", "");
                    } else {
                        lineLyrics[l] = lineLyrics[l].replace("_", " ");
                    }
                } else if (l==0 && !lineLyrics[l].contains("["+c.getResources().getString(R.string.image)+"_")) {

                    if (FullscreenActivity.whichMode.equals("Presentation") && !FullscreenActivity.presoShowChords) {
                        lineLyrics[l] = lineLyrics[l].replace("_", "");
                    } else if ((FullscreenActivity.whichMode.equals("Stage") || FullscreenActivity.whichMode.equals("Performance")) &&
                            !FullscreenActivity.showChords) {
                        lineLyrics[l] = lineLyrics[l].replace("_", "");
                    } else {
                        lineLyrics[l] = lineLyrics[l].replace("_", " ");
                    }
                }
            }
            myLyrics += lineLyrics[l] + "\n";
        }
        return myLyrics;
    }

    static String rebuildParsedLyrics(int length) {
        String tempLyrics = "";
        for (int x = 0; x < length; x++) {
            // First line of section should be the label, so replace it with label.
            if (FullscreenActivity.songSections[x].startsWith("[" + FullscreenActivity.songSectionsLabels[x] + "]")) {
                tempLyrics += FullscreenActivity.songSections[x] + "\n";
            } else {
                tempLyrics += FullscreenActivity.songSections[x] + "\n";
            }
        }
        FullscreenActivity.myParsedLyrics = tempLyrics.split("\n");

        return tempLyrics;
    }

    static void lookForSplitPoints() {
        // Script to determine 2 column split details
        int halfwaypoint = Math.round(FullscreenActivity.numrowstowrite / 2);

        // Look for nearest split point before halfway
        int splitpoint_1sthalf = 0;
        boolean gotityet = false;
        for (int scan = halfwaypoint; scan > 0; scan--) {
            if (!gotityet) {
                if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].startsWith("[")) {
                    gotityet = true;
                    splitpoint_1sthalf = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan]!=null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
                    gotityet = true;
                    splitpoint_1sthalf = scan;
                }
            }
        }

        // Look for nearest split point past halfway
        int splitpoint_2ndhalf = FullscreenActivity.numrowstowrite;
        boolean gotityet2 = false;
        for (int scan = halfwaypoint; scan < FullscreenActivity.myParsedLyrics.length; scan++) {
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
        for (int scan = thirdwaypoint; scan < FullscreenActivity.myParsedLyrics.length; scan++) {
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
        for (int scan = twothirdwaypoint; scan < FullscreenActivity.myParsedLyrics.length; scan++) {
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

        // Which is the best split point to use (closest to halfway) for 2 columns
        int split1stdiff = Math.abs(halfwaypoint - splitpoint_1sthalf);
        int split2nddiff = Math.abs(halfwaypoint - splitpoint_2ndhalf);


        if (split1stdiff <= split2nddiff) {
            FullscreenActivity.splitpoint = splitpoint_1sthalf;
        } else {
            FullscreenActivity.splitpoint = splitpoint_2ndhalf;
        }

        // If there is only one section, the splitpoint should be at the end
        if (FullscreenActivity.songSections.length==1) {
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

        // Now we know where the split points are in the full document
        // We need to know the sections the splits are in
        // By default the splitpoints should be after the number of sections
        // i.e. the document isn't split
        int halfsplit_section     = FullscreenActivity.songSections.length;
        int thirdsplit_section    = FullscreenActivity.songSections.length;
        int twothirdsplit_section = FullscreenActivity.songSections.length;
        int lineweareon = 0;
        // Go through the sections and get the line number we're on
        for (int s=0;s<FullscreenActivity.songSections.length;s++) {
            lineweareon += FullscreenActivity.sectionContents[s].length;
            if (halfsplit_section == FullscreenActivity.songSections.length && FullscreenActivity.splitpoint < lineweareon) {
                halfsplit_section = s;
            }
            if (thirdsplit_section == FullscreenActivity.songSections.length && FullscreenActivity.thirdsplitpoint < lineweareon) {
                thirdsplit_section = s;
            }
            if (twothirdsplit_section == FullscreenActivity.songSections.length && FullscreenActivity.twothirdsplitpoint < lineweareon) {
                twothirdsplit_section = s;
            }
        }
        FullscreenActivity.halfsplit_section = halfsplit_section;
        FullscreenActivity.thirdsplit_section = thirdsplit_section;
        FullscreenActivity.twothirdsplit_section = twothirdsplit_section;
    }

    static void processKey() {
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

    static void processTimeSig() {
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
            case "1/4":
                FullscreenActivity.timesigindex = 13;
                FullscreenActivity.beats = 4;
                FullscreenActivity.noteValue = 4;
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

    static int getSalutReceivedSection(String s) {
        int i=-1;
        Log.d("d","s="+s);
        if (s!=null && s.length()>0 && s.contains("___section___")) {
            s = s.replace("{\"description\":\"","");
            s = s.replace("\"}","");
            s = s.replace("___section___","");
            Log.d("d","s="+s);
            try {
                i = Integer.parseInt(s);
            } catch (Exception e) {
                i = -1;
            }
        }
        Log.d("d","i="+i);
        return i;
    }
    static String getSalutReceivedLocation(String string, Context c) {
        String[] s;
        string = string.replace("{\"description\":\"","");
        string = string.replace("\"}","");
        boolean exists;
        if (string.length()>0 && string.contains("_____") && !FullscreenActivity.receiveHostFiles) {
            // We have a song location!
            s = string.split("_____");
            // Check the song exists
            File testfile;
            if (s[0].equals(FullscreenActivity.mainfoldername)) {
                testfile = new File(FullscreenActivity.dir + "/" + s[1]);
                exists = testfile.exists();
            } else {
                testfile = new File(FullscreenActivity.dir + "/" + s[0] + "/" + s[1]);
                exists = testfile.exists();
            }

            if (exists && s.length == 3 && s[0] != null && s[1] != null && s[2] != null) {
                FullscreenActivity.whichSongFolder = s[0];
                FullscreenActivity.songfilename = s[1];
                FullscreenActivity.whichDirection = s[2];
                return "Location";
            } else {
                FullscreenActivity.myToastMessage = s[0] + "/" + s[1] + "\n" +
                        c.getResources().getString(R.string.songdoesntexist);
                ShowToast.showToast(c);
                return "";
            }
        } else if (string.length()>0 && string.contains("<lyrics>") && FullscreenActivity.receiveHostFiles) {
            FullscreenActivity.mySalutXML = string;
            Log.d("d","HostFile");
            return "HostFile";
        }

        return "";
    }

    static boolean isAutoScrollValid() {
        // Get the autoScrollDuration;
        if (FullscreenActivity.mDuration.isEmpty() && FullscreenActivity.autoscroll_default_or_prompt.equals("default")) {
            FullscreenActivity.autoScrollDuration = FullscreenActivity.default_autoscroll_songlength;
        } else if (FullscreenActivity.mDuration.isEmpty() && FullscreenActivity.autoscroll_default_or_prompt.equals("prompt")) {
            FullscreenActivity.autoScrollDuration = -1;
        } else {
            try {
                FullscreenActivity.autoScrollDuration = Integer.parseInt(FullscreenActivity.mDuration.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                FullscreenActivity.autoScrollDuration = 0;
            }
        }

        // Get the autoScrollDelay;
        if (FullscreenActivity.mPreDelay.isEmpty() && FullscreenActivity.autoscroll_default_or_prompt.equals("default")) {
            FullscreenActivity.autoScrollDelay = FullscreenActivity.default_autoscroll_predelay;
        } else if (FullscreenActivity.mDuration.isEmpty() && FullscreenActivity.autoscroll_default_or_prompt.equals("prompt")) {
            FullscreenActivity.autoScrollDelay = 0;
        } else {
            try {
                FullscreenActivity.autoScrollDelay = Integer.parseInt(FullscreenActivity.mPreDelay.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                FullscreenActivity.autoScrollDelay = 0;
            }
        }

        return (FullscreenActivity.autoScrollDuration > 0 && FullscreenActivity.autoScrollDelay >= 0) ||
                FullscreenActivity.usingdefaults;
    }

    static String removeUnwantedSymbolsAndSpaces(String string) {
        // Replace unwanted symbols
        // Split into lines
        //string = string.replace("|", "\n");
        if (FullscreenActivity.whichMode.equals("Presentation") && !FullscreenActivity.presoShowChords) {
            string = string.replace("_", "");
        } else if ((FullscreenActivity.whichMode.equals("Stage") || FullscreenActivity.whichMode.equals("Performance")) &&
                !FullscreenActivity.showChords) {
            string = string.replace("_", "");
        } else {
            string = string.replace("_", " ");
        }
        string = string.replace(",", " ");
        string = string.replace(".", " ");
        string = string.replace(":", " ");
        string = string.replace(";", " ");
        string = string.replace("!", " ");
        string = string.replace("'", "");
        string = string.replace("(", " ");
        string = string.replace(")", " ");
        //string = string.replace("-", " ");

        // Now remove any double spaces
        while (string.contains("  ")) {
            string = string.replace("  ", " ");
        }

        return string;
    }

    static String determineLineTypes(String string, Context c) {
        String type;
        if (string.indexOf(".")==0) {
            type = "chord";
        } else if (string.indexOf(";__" + c.getResources().getString(R.string.edit_song_capo))==0) {
            type = "capoinfo";
        } else if (string.indexOf(";__")==0) {
            type = "extra";
        //} else if (string.startsWith(";"+c.getString(R.string.music_score))) {
        //    type = "abcnotation";
        } else if (string.startsWith(";E |") || string.startsWith(";A |") ||
                string.startsWith(";D |") || string.startsWith(";G |") ||
                string.startsWith(";B |") || string.startsWith(";e |") ||
                string.startsWith(";E|") || string.startsWith(";A|") ||
                string.startsWith(";D|") || string.startsWith(";G|") ||
                string.startsWith(";B|") || string.startsWith(";e|") ||
                string.startsWith(";Ab|") || string.startsWith(";A#|") ||
                string.startsWith(";Bb|") || string.startsWith(";Cb|") ||
                string.startsWith(";C#|") || string.startsWith(";Db|") ||
                string.startsWith(";D#|") || string.startsWith(";Eb|") || string.startsWith(";eb|") ||
                string.startsWith(";Fb|") || string.startsWith(";F#|") ||
                string.startsWith(";Gb|") || string.startsWith(";G#|") ||
                string.startsWith("; ||")) {
            type = "tab";
        } else if (string.indexOf(";")==0) {
            type = "comment";
        } else if (string.indexOf("[")==0) {
            type = "heading";
        } else {
            type = "lyric";
        }
        return type;
    }

    private static String[] getChordPositions(String string) {
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

    private static String[] getChordSections(String string, String[] pos_string) {
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

    private static String[] getLyricSections(String string, String[] pos_string) {
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

    private static TableLayout.LayoutParams tablelayout_params() {
        return new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
    }

    private static TableRow.LayoutParams tablerow_params() {
        return new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT);
    }

    private static LinearLayout.LayoutParams linearlayout_params() {
        if (FullscreenActivity.scalingfiguredout) {
            return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        } else {
            return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private static TableRow capolinetoTableRow(Context c, String[] chords, float fontsize) {
        TableRow caporow  = new TableRow(c);
        caporow.setClipChildren(false);
        caporow.setClipToPadding(false);
        caporow.setPadding(0, -(int) ((float) FullscreenActivity.linespacing / fontsize), 0, 0);

        for (String bit:chords) {
            if (bit.indexOf(".")==0 && bit.length()>1) {
                bit = bit.substring(1);
            }
            TextView capobit  = new TextView(c);
            capobit.setLayoutParams(tablerow_params());
            FullscreenActivity.temptranspChords = bit;
            try {
                Transpose.capoTranspose();
            } catch (Exception e) {
                e.printStackTrace();
            }
            capobit.setText(FullscreenActivity.temptranspChords);
            capobit.setTextSize(fontsize * FullscreenActivity.chordfontscalesize);
            if (FullscreenActivity.whichMode.equals("Presentation")) {
                capobit.setTextColor(FullscreenActivity.lyricsCapoColor);
                capobit.setTypeface(FullscreenActivity.chordsfont);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    capobit.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                float shadow = fontsize/2.0f;
                if (shadow>24.0f) {
                    shadow = 24.0f;
                }
                capobit.setShadowLayer(shadow, 4, 4, FullscreenActivity.presoShadowColor);
            } else {
                capobit.setTextColor(FullscreenActivity.lyricsCapoColor);
                capobit.setTypeface(FullscreenActivity.chordsfont);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    capobit.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                float shadow = fontsize/2.0f;
                if (shadow>24.0f) {
                    shadow = 24.0f;
                }
                capobit.setShadowLayer(shadow, 4, 4, FullscreenActivity.lyricsBackgroundColor);
            }
            caporow.addView(capobit);
        }
        return caporow;
    }

    private static TableRow chordlinetoTableRow(Context c, String[] chords, float fontsize) {
        TableRow chordrow = new TableRow(c);
        chordrow.setLayoutParams(tablelayout_params());
        chordrow.setPadding(0, -(int) ((float) FullscreenActivity.linespacing / fontsize), 0, 0);
        chordrow.setClipChildren(false);
        chordrow.setClipToPadding(false);

        for (String bit:chords) {
            if (bit.indexOf(".")==0 && bit.length()>1) {
                bit = bit.substring(1);
            }
            TextView chordbit = new TextView(c);
            chordbit.setPadding(0,0,0,0);

            //bit = bit.replace("b","\u266d"); //Gives the proper b symbol if available in font
            chordbit.setText(bit);
            chordbit.setTextSize(fontsize * FullscreenActivity.chordfontscalesize);
            if (FullscreenActivity.whichMode.equals("Presentation")) {
                chordbit.setTextColor(FullscreenActivity.lyricsChordsColor);
                chordbit.setTypeface(FullscreenActivity.chordsfont);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    chordbit.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                float shadow = fontsize/2.0f;
                if (shadow>24.0f) {
                    shadow = 24.0f;
                }
                chordbit.setShadowLayer(shadow, 4, 4, FullscreenActivity.presoShadowColor);

            } else {
                chordbit.setTextColor(FullscreenActivity.lyricsChordsColor);
                chordbit.setTypeface(FullscreenActivity.chordsfont);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    chordbit.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                float shadow = fontsize/2.0f;
                if (shadow>24.0f) {
                    shadow = 24.0f;
                }
                chordbit.setShadowLayer(shadow, 4, 4, FullscreenActivity.lyricsBackgroundColor);
            }
            chordrow.addView(chordbit);
        }
        return chordrow;
    }

    @SuppressWarnings("deprecation")
    private static TableRow lyriclinetoTableRow(Context c, String[] lyrics, float fontsize) {
        TableRow lyricrow = new TableRow(c);
        if (FullscreenActivity.whichMode.equals("Presentation") && FullscreenActivity.scalingfiguredout &&
                !FullscreenActivity.presoShowChords) {
            lyricrow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            lyricrow.setGravity(FullscreenActivity.presoLyricsAlign);
        } else {
            lyricrow.setLayoutParams(tablelayout_params());
        }
        // set different layoutparams and set gravity
        lyricrow.setPadding(0, -(int) ((float) FullscreenActivity.linespacing / fontsize), 0, 0);
        lyricrow.setClipChildren(false);
        lyricrow.setClipToPadding(false);

        for (String bit:lyrics) {
            String imagetext;
            if (bit.contains("/Images/_cache/")) {
                FullscreenActivity.isImageSection = true;
                imagetext = bit;

            } else {
                imagetext = "";
            }

            if (!FullscreenActivity.whichSongFolder.contains(c.getResources().getString(R.string.image))) {
                if (FullscreenActivity.whichMode.equals("Presentation") && !FullscreenActivity.presoShowChords) {
                    bit = bit.replace("_", "");
                } else if ((FullscreenActivity.whichMode.equals("Stage") || FullscreenActivity.whichMode.equals("Performance")) &&
                        !FullscreenActivity.showChords) {
                    bit = bit.replace("_", "");
                } else {
                    bit = bit.replace("_", " ");
                }
            }

            TextView lyricbit = new TextView(c);
            if (FullscreenActivity.whichMode.equals("Presentation") && FullscreenActivity.scalingfiguredout &&
                    !FullscreenActivity.presoShowChords) {
                lyricbit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                lyricbit.setGravity(FullscreenActivity.presoLyricsAlign);
            } else {
                lyricbit.setLayoutParams(tablerow_params());
            }
            lyricbit.setText(bit);
            lyricbit.setTextSize(fontsize);
            if (FullscreenActivity.whichMode.equals("Presentation")) {

                lyricbit.setTextColor(FullscreenActivity.presoFontColor);
                lyricbit.setTypeface(FullscreenActivity.presofont);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    lyricbit.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                float shadow = fontsize/2.0f;
                if (shadow>24.0f) {
                    shadow = 24.0f;
                }
                lyricbit.setShadowLayer(shadow, 4, 4, FullscreenActivity.presoShadowColor);

                int w = PresentationService.ExternalDisplay.availableWidth_1col;
                // If we have turned off autoscale and aren't showing the chords, allow wrapping
                if (!FullscreenActivity.presoAutoScale && !FullscreenActivity.presoShowChords && w>0) {
                    TableRow.LayoutParams tllp = new TableRow.LayoutParams(w,TableRow.LayoutParams.WRAP_CONTENT);
                    lyricbit.setLayoutParams(tllp);
                    lyricbit.setSingleLine(false);
                    lyricbit.setTextSize(FullscreenActivity.presoFontSize);
                } else {
                    lyricbit.setSingleLine(true);
                }
            } else {
                lyricbit.setTextColor(FullscreenActivity.lyricsTextColor);
                lyricbit.setTypeface(FullscreenActivity.lyricsfont);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    lyricbit.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                float shadow = fontsize/2.0f;
                if (shadow>24.0f) {
                    shadow = 24.0f;
                }
                lyricbit.setShadowLayer(shadow, 4, 4, FullscreenActivity.lyricsBackgroundColor);
            }

            if (FullscreenActivity.isImageSection) {
                FullscreenActivity.isImageSection = false;
                ImageView img = new ImageView(c);

                // By default, the image should be the not found one
                Drawable drw = c.getResources().getDrawable(R.drawable.notfound);

                int maxwidth = 320;
                if (FullscreenActivity.myWidthAvail>0) {
                    maxwidth = (int) (0.25f * FullscreenActivity.myWidthAvail);
                }

                img.setMaxWidth(maxwidth);
                img.setMaxHeight(maxwidth);

                File checkfile = new File(imagetext);
                if (checkfile.exists()) {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;

                        //Returns null, sizes are in the options variable
                        BitmapFactory.decodeFile(imagetext, options);
                        int width = options.outWidth;
                        int height = options.outHeight;

                        int thumbheight = (int) ((float)height * ((float)maxwidth/(float)width));

                        Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagetext), maxwidth, thumbheight);
                        Resources res = c.getResources();
                        BitmapDrawable bd = new BitmapDrawable(res, ThumbImage);
                        if (ThumbImage!=null) {
                            img.setLayoutParams(new TableRow.LayoutParams(ThumbImage.getWidth(), ThumbImage.getHeight()));
                        }
                        img.setImageDrawable(bd);

                    } catch (Exception e1) {
                        // Didn't work
                        img.setImageDrawable(drw);
                    } catch (OutOfMemoryError e2) {
                            e2.printStackTrace();
                    }
                } else {
                    img.setImageDrawable(drw);
                }
                lyricrow.addView(img);
            } else {
                lyricrow.addView(lyricbit);
            }
        }
        return lyricrow;
    }

    private static TableRow commentlinetoTableRow(Context c, String[] comment, float fontsize, boolean tab) {
        TableRow commentrow = new TableRow(c);
        commentrow.setLayoutParams(tablelayout_params());
        commentrow.setClipChildren(false);
        commentrow.setClipToPadding(false);

        for (String bit:comment) {
            if (bit.indexOf(" ")==0 && bit.length()>1) {
                bit = bit.substring(1);
            }
            if (!FullscreenActivity.whichSongFolder.contains(c.getResources().getString(R.string.image))) {
                if (FullscreenActivity.whichMode.equals("Presentation") && !FullscreenActivity.presoShowChords) {
                    bit = bit.replace("_", "");
                } else if ((FullscreenActivity.whichMode.equals("Stage") || FullscreenActivity.whichMode.equals("Performance")) &&
                        !FullscreenActivity.showChords) {
                    bit = bit.replace("_", "");
                } else {
                    bit = bit.replace("_", " ");
                }
            }
            TextView lyricbit = new TextView(c);
            lyricbit.setLayoutParams(tablerow_params());
            lyricbit.setText(bit);
            lyricbit.setTextSize(fontsize * FullscreenActivity.commentfontscalesize);
            if (FullscreenActivity.whichMode.equals("Presentation")) {
                lyricbit.setTextColor(FullscreenActivity.presoFontColor);
                lyricbit.setTypeface(FullscreenActivity.presofont);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    lyricbit.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                float shadow = fontsize/2.0f;
                if (shadow>24.0f) {
                    shadow = 24.0f;
                }
                lyricbit.setShadowLayer(shadow, 4, 4, FullscreenActivity.presoShadowColor);
            } else {
                lyricbit.setTextColor(FullscreenActivity.lyricsTextColor);
                lyricbit.setTypeface(FullscreenActivity.lyricsfont);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    lyricbit.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                float shadow = fontsize/2.0f;
                if (shadow>24.0f) {
                    shadow = 24.0f;
                }
                lyricbit.setShadowLayer(shadow, 4, 4, FullscreenActivity.lyricsBackgroundColor);
            }
            if (tab) {
                // Set the comment text as monospaced to make it fit
                lyricbit.setTypeface(FullscreenActivity.typeface1);
            }
            commentrow.addView(lyricbit);
        }
        return commentrow;
    }

/*    @SuppressLint("SetJavaScriptEnabled")
    public static WebView abcnotationtoWebView(Context c, final String s) {
        final WebView wv = new WebView(c);
        boolean oktouse = false;
        if (!s.equals("")) {
            TableLayout.LayoutParams lp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            wv.setLayoutParams(lp);
            wv.getSettings().setJavaScriptEnabled(true);
            wv.loadUrl("file:///android_asset/ABC/abc.html");
            wv.setWebViewClient(new WebViewClient() {

                public void onPageFinished(WebView view, String url) {
                    String webstring = "";
                    try {
                        webstring = Uri.encode(s, "UTF-8");
                    } catch  (Exception e) {
                        Log.d("d","Error encoding");
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        wv.evaluateJavascript("javascript:displayOnly();", null);
                        wv.evaluateJavascript("javascript:updateABC('"+webstring+"');", null);
                    } else {
                        wv.loadUrl("javascript:displayOnly();");
                        wv.loadUrl("javascript:updateABC('"+webstring+"');");
                    }
                }
            });
            oktouse = true;
        }

        if (oktouse) {
            return wv;
        } else {
            return null;
        }
    }*/

    private static TextView titletoTextView(Context c, String title, float fontsize) {
        TextView titleview = new TextView(c);
        titleview.setLayoutParams(linearlayout_params());
        titleview.setText(title);
        titleview.setTextColor(FullscreenActivity.lyricsTextColor);
        titleview.setTypeface(FullscreenActivity.lyricsfont);
        titleview.setTextSize(fontsize * FullscreenActivity.headingfontscalesize);
        titleview.setPaintFlags(titleview.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        return titleview;
    }

    private static String howToProcessLines(int linenum, int totallines, String thislinetype, String nextlinetype, String previouslinetype) {
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
        } else if (thislinetype.equals("capoinfo")) {
            what = "capo_info";
        } else if (thislinetype.equals("extra")) {
            what = "extra_info";
        } else if (thislinetype.equals("tab")) {
            what = "guitar_tab";
        //} else if (thislinetype.equals("abcnotation")) {
        //    what = "abc_notation";
        } else {
            what = "null"; // Probably a lyric line with a chord above it - already dealt with
        }
        return what;
    }

    @SuppressWarnings("unused")
    public static String lyriclinetoHTML(String[] lyrics) {
        String lyrichtml = "";
        for (String bit:lyrics) {
            lyrichtml += "<td class=\"lyric\">"+bit.replace(" ","&nbsp;")+"</td>";
        }
        return lyrichtml;
    }

    private static String[] beautifyHeadings(String string, Context c) {

        if (string==null) {
            string = "";
        }

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
                string = string.replace("V", c.getResources().getString(R.string.tag_verse) + " ");
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
                string = string.replace("T", c.getResources().getString(R.string.tag_tag) + " ");
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
                string = string.replace("C", c.getResources().getString(R.string.tag_chorus) + " ");
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
                string = string.replace("B", c.getResources().getString(R.string.tag_bridge) + " ");
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
                string = string.replace("P", c.getResources().getString(R.string.tag_prechorus) + " ");
                section = "prechorus";
                break;
            default:
                section = "custom";
        }

        // Look for caps or English tags for non-English app users
        if (string.toLowerCase(FullscreenActivity.locale).contains(c.getResources().getString(R.string.tag_verse)) ||
                string.toLowerCase(FullscreenActivity.locale).contains("verse")) {
            section = "verse";
        } else if (string.toLowerCase(FullscreenActivity.locale).contains(c.getResources().getString(R.string.tag_prechorus)) ||
                string.toLowerCase(FullscreenActivity.locale).contains("prechorus") ||
                string.toLowerCase(FullscreenActivity.locale).contains("pre-chorus")){
            section = "prechorus";
        } else if (string.toLowerCase(FullscreenActivity.locale).contains(c.getResources().getString(R.string.tag_chorus)) ||
                string.toLowerCase(FullscreenActivity.locale).contains("chorus")){
            section = "chorus";
        } else if (string.toLowerCase(FullscreenActivity.locale).contains(c.getResources().getString(R.string.tag_tag)) ||
                string.toLowerCase(FullscreenActivity.locale).contains("tag")){
            section = "tag";
        } else if (string.toLowerCase(FullscreenActivity.locale).contains(c.getResources().getString(R.string.tag_bridge)) ||
                string.toLowerCase(FullscreenActivity.locale).contains("bridge")){
            section = "bridge";
        }
        String[] vals = new String[2];
        vals[0] = string;
        vals[1] = section;
        return vals;
    }

    private static String fixLineLength(String string, int newlength) {
        int extraspacesrequired = newlength - string.length();
        for (int x=0; x<extraspacesrequired; x++) {
            string += " ";
        }
        return string;
    }

    @SuppressWarnings("unused")
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

    static int getSectionColors(String type) {
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

    static String fixMultiLineFormat(String string, Context c) {
        // Best way to determine if the song is in multiline format is
        // Look for [v] or [c] case insensitive
        // And it needs to be followed by a line starting with 1 and 2
        try {
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
                            ((z < lines.length - 1 && lines[z + 1].startsWith(" 1")) || (z < lines.length - 2 && lines[z + 2].startsWith(" 1")))) ||

                            ((lines[z].toLowerCase(FullscreenActivity.locale).indexOf("[" + c.getResources().getString(R.string.tag_verse).toLowerCase(FullscreenActivity.locale) + "]") == 0) &&
                                    ((z < lines.length - 1 && lines[z + 1].startsWith(" 1")) || (z < lines.length - 2 && lines[z + 2].startsWith(" 1"))))) {
                        lines[z] = "__VERSEMULTILINE__";
                        gettingverse = true;
                        gettingchorus = false;
                    }
                    if ((lines[z].toLowerCase(FullscreenActivity.locale).indexOf("[c]") == 0 &&
                            ((z < lines.length - 1 && lines[z + 1].startsWith(" 1")) || (z < lines.length - 2 && lines[z + 2].startsWith(" 1")))) ||

                            ((lines[z].toLowerCase(FullscreenActivity.locale).indexOf("[" + c.getResources().getString(R.string.tag_chorus).toLowerCase(FullscreenActivity.locale) + "]") == 0) &&
                                    ((z < lines.length - 1 && lines[z + 1].startsWith(" 1")) || (z < lines.length - 2 && lines[z + 2].startsWith(" 1"))))) {
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
        } catch (Exception e) {
            return string;
        }
    }

    static String[] removeTagLines(String[] sections) {
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

    static String removeChordLines(String song) {
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

    static String getAllChords(String song) {
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

    static String removeCommentLines(String song) {
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

    private static String addchordstomultiline(String[] multiline, String chords) {
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

    static String[] splitSongIntoSections(String song, Context c) {

        song = song.replace("-!!", "");

        if (FullscreenActivity.whichMode.equals("Presentation") || FullscreenActivity.whichMode.equals("Stage")) {
            song = song.replace("||", "%%LATERSPLITHERE%%");
        } else {
            song = song.replace("||", "");
        }

        // Need to go back to chord lines that might have to have %%LATERSPLITHERE%% added
        if (!FullscreenActivity.whichSongFolder.contains(c.getResources().getString(R.string.scripture))) {
            song = song.replace("\n\n", "%%__SPLITHERE__%%");
        }

        String[] temp = song.split("\n");
        song = "";
        for (String t:temp) {
            if (!t.startsWith(";") && !t.startsWith(".")) {
                if (t.trim().startsWith("---")) {
                    t = t.replace(" ---", "[]");
                    t = t.replace("---", "[]");
                }
            }

            if (t.startsWith(".")||t.startsWith(";")) {
                song += t + "\n";
            } else {
                if (FullscreenActivity.whichMode.equals("Presentation") && !FullscreenActivity.presoShowChords) {
                    song += t.replace("|", "\n") + "\n";
                } else if (FullscreenActivity.whichMode.equals("Presentation") && FullscreenActivity.presoShowChords) {
                    song += t.replace("|", " ") + "\n";
                } else {
                    song += t + "\n";
                }
            }
        }

        if (FullscreenActivity.presenterChords.equals("N")) {
            // Split into lines
            song = "";
            for (String t:temp) {
                if (t.startsWith(".") || t.startsWith(";")) {
                    song += t + "\n";
                } else {
                    song += t.replace("|","\n") + "\n";
                }
            }
        } else {
            // Split into lines
            temp = song.split("\n");
            song = "";
            for (String t:temp) {
                if (t.startsWith(".") || t.startsWith(";")) {
                    song += t + "\n";
                } else {
                    song += t.replace("|"," ") + "\n";
                }
            }

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

        //return song.split("%%__SPLITHERE__%%");
    }

    static String[] splitLaterSplits(String[] currsections) {
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
            if (FullscreenActivity.trimSections) {
                if (updatedSections[y].endsWith("\n ") && updatedSections[y].length()>0) {
                    updatedSections[y] = updatedSections[y].substring(0,updatedSections[y].length()-1);
                }
                updatedSections[y] = updatedSections[y].trim();
            }

        }
        return updatedSections;
    }

    static String getSectionHeadings(String songsection) {
        String label = "";
        //songsection = songsection.trim();
        if (songsection.indexOf("[")==0) {
            int startoftag = songsection.indexOf("[");
            int endoftag = songsection.indexOf("]");
            if (endoftag < startoftag) {
                endoftag = songsection.length() - 1;
            }
            if (endoftag > startoftag) {
                label = songsection.substring(startoftag + 1, endoftag);
            } else {
                songsection = songsection.replace("[","");
                label = songsection.replace("]","");
            }
            FullscreenActivity.songSection_holder = label;
        }
        if (label.equals("")) {
            // If section is just a comment line, have no label
            int lines = songsection.split("\n").length;
            if (lines<2 || songsection.startsWith(";")) {
                label = "";
            } else {
                label = FullscreenActivity.songSection_holder;
            }
        }
        return label;
    }

    static String[] matchPresentationOrder(String[] currentSections, Context c) {

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

        return splitSongIntoSections(newSongText,c);

    }

    static String getSongTitle() {
        return FullscreenActivity.mTitle.toString();
    }

    static String getSongAuthor() {
        return FullscreenActivity.mAuthor.toString();
    }

    static String getSongKey() {
        // If key is set
        String keytext = "";
        if (!FullscreenActivity.mKey.isEmpty() && !FullscreenActivity.mKey.equals("")) {
            keytext = " (" + FullscreenActivity.mKey + ")";
        }
        return keytext;
    }

    static String getCapoInfo() {
        String s = "";
        // If we are using a capo, add the capo display
        if (!FullscreenActivity.mCapo.equals("")) {
            int mcapo;
            try {
                mcapo = Integer.parseInt(FullscreenActivity.mCapo);
            } catch (Exception e) {
                mcapo = -1;
            }
            if (mcapo>0) {
                if (FullscreenActivity.showCapoAsNumerals) {
                    //s = c.getString(R.string.edit_song_capo) + ": " + numberToNumeral(mcapo);
                    s = numberToNumeral(mcapo);
                } else {
                    //s = c.getString(R.string.edit_song_capo) + ": " + mcapo;
                    s = "" + mcapo;
                }
            }
        }
        return s;
    }

    static String getCapoNewKey() {
        String s = "";
        // If we are using a capo, add the capo display
        if (!FullscreenActivity.mCapo.equals("") && !FullscreenActivity.mKey.equals("") &&
                !FullscreenActivity.capokey.equals("")) {
            s = FullscreenActivity.capokey;
        }
        return s;
    }

    static String numberToNumeral(int num) {
        String s;
        switch (num) {
            default:
                s = "";
                break;
            case 1:
                s = "I";
                break;
            case 2:
                s = "II";
                break;
            case 3:
                s = "III";
                break;
            case 4:
                s = "IV";
                break;
            case 5:
                s = "V";
                break;
            case 6:
                s = "VI";
                break;
            case 7:
                s = "VII";
                break;
            case 8:
                s = "VIII";
                break;
            case 9:
                s = "IX";
                break;
            case 10:
                s = "X";
                break;
            case 11:
                s = "XI";
                break;
            case 12:
                s = "XII";
                break;
        }
        return s;
    }

    static String songSectionChordPro(Context c, int x, boolean onsong) {
        String chopro = "";
        String[] heading = beautifyHeadings(FullscreenActivity.songSectionsLabels[x],c);
        if (onsong) {
            chopro += heading[0].trim() + ":\n";
        } else {
            if (heading[1].equals("chorus")) {
                chopro += "{soc}\n";
            } else {
                chopro += "{c:" + heading[0].trim() + "}\n";
            }
        }

        int linenums = FullscreenActivity.sectionContents[x].length;

        // Go through each line and add the appropriate lyrics with chords in them
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

            switch (ProcessSong.howToProcessLines(y, linenums, FullscreenActivity.sectionLineTypes[x][y], nextlinetype, previouslinetype)) {
                // If this is a chord line followed by a lyric line.
                case "chord_then_lyric":
                    if (FullscreenActivity.sectionContents[x][y].length() > FullscreenActivity.sectionContents[x][y + 1].length()) {
                        FullscreenActivity.sectionContents[x][y + 1] = ProcessSong.fixLineLength(FullscreenActivity.sectionContents[x][y + 1], FullscreenActivity.sectionContents[x][y].length());
                    }
                    positions_returned = ProcessSong.getChordPositions(FullscreenActivity.sectionContents[x][y]);
                    chords_returned = ProcessSong.getChordSections(FullscreenActivity.sectionContents[x][y], positions_returned);
                    lyrics_returned = ProcessSong.getLyricSections(FullscreenActivity.sectionContents[x][y + 1], positions_returned);
                    for (int w = 0; w < lyrics_returned.length; w++) {
                        String chord_to_add = "";
                        if (w<chords_returned.length) {
                            if (chords_returned[w] != null && !chords_returned[w].trim().equals("")) {
                                chord_to_add = "[" + chords_returned[w].trim() + "]";
                            }
                        } else {
                            chord_to_add = "";
                        }
                        chopro += chord_to_add + lyrics_returned[w];
                    }
                    break;

                case "chord_only":
                    positions_returned = ProcessSong.getChordPositions(FullscreenActivity.sectionContents[x][y]);
                    chords_returned = ProcessSong.getChordSections(FullscreenActivity.sectionContents[x][y], positions_returned);
                    for (String aChords_returned : chords_returned) {
                        String chord_to_add = "";
                        if (aChords_returned != null && !aChords_returned.trim().equals("")) {
                            chord_to_add = "[" + aChords_returned.trim() + "]";
                        }
                        chopro += chord_to_add;
                    }
                    break;

                case "lyric_no_chord":
                    chopro += FullscreenActivity.sectionContents[x][y].trim();

                    break;

                case "comment_no_chord":
                    chopro += "{c:" + FullscreenActivity.sectionContents[x][y].trim() + "}";
                    break;
            }
            chopro += "\n";
            chopro = chopro.replace("][","]  [");
            chopro = chopro.replace("\n\n","\n");
        }

        if (heading[1].equals("chorus")) {
            chopro += "{eoc}\n";
        }
        chopro += "\n";
        return chopro;
    }

    static String songSectionText(Context c, int x) {
        String text = "";
        String[] heading = beautifyHeadings(FullscreenActivity.songSectionsLabels[x],c);
        text += heading[0].trim() + ":";

        int linenums = FullscreenActivity.sectionContents[x].length;

        // Go through each line and add the appropriate lyrics with chords in them
        for (int y = 0; y < linenums; y++) {
            if (FullscreenActivity.sectionContents[x][y].length() > 1 &&
                    FullscreenActivity.sectionContents[x][y].startsWith("[")) {
                text += "";
            } else if (FullscreenActivity.sectionContents[x][y].length() > 1 &&
                    FullscreenActivity.sectionContents[x][y].startsWith(" ") ||
                    FullscreenActivity.sectionContents[x][y].startsWith(".") ||
                    FullscreenActivity.sectionContents[x][y].startsWith(";")) {
                text += FullscreenActivity.sectionContents[x][y].substring(1);
            } else {
                text += FullscreenActivity.sectionContents[x][y];
            }
            text += "\n";

        }
        text += "\n";

        if (FullscreenActivity.trimSections) {
            text = text.trim();
        }
        return text;
    }
    @SuppressWarnings("all")
    public static LinearLayout songSectionView(Context c, int x, float fontsize, boolean projected) {

        final LinearLayout ll = new LinearLayout(c);

        ll.setLayoutParams(linearlayout_params());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0,0,0,0);
        ll.setClipChildren(false);
        ll.setClipToPadding(false);

        String[] returnvals = beautifyHeadings(FullscreenActivity.songSectionsLabels[x],c);

        ll.addView(titletoTextView(c, returnvals[0], fontsize));

        // Identify the section type
        if (x<FullscreenActivity.songSectionsTypes.length) {
            FullscreenActivity.songSectionsTypes[x] = returnvals[1];
        }
        int linenums = FullscreenActivity.sectionContents[x].length;

        String mCapo = FullscreenActivity.mCapo;
        if (mCapo==null || mCapo.isEmpty() || mCapo.equals("")) {
            mCapo = "0";
        }
        int mcapo = Integer.parseInt(mCapo);
        boolean showchords;
        if (projected) {
            showchords = FullscreenActivity.presoShowChords;
        } else {
            showchords = FullscreenActivity.showChords;
        }
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
                TableLayout tl = createTableLayout(c);

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
                            tl.addView(ProcessSong.capolinetoTableRow(c, chords_returned, fontsize));
                        }
                        if (!justcapo && donativechords) {
                            tl.addView(ProcessSong.chordlinetoTableRow(c, chords_returned, fontsize));
                        }
                        if (FullscreenActivity.showLyrics) {
                            tl.addView(ProcessSong.lyriclinetoTableRow(c, lyrics_returned, fontsize));
                        }
                        break;

                    case "chord_only":
                        chords_returned = new String[1];
                        chords_returned[0] = FullscreenActivity.sectionContents[x][y];
                        if (docapochords) {
                            tl.addView(ProcessSong.capolinetoTableRow(c, chords_returned, fontsize));
                        }
                        if (!justcapo && donativechords) {
                            tl.addView(ProcessSong.chordlinetoTableRow(c, chords_returned, fontsize));
                        }
                        break;

                    case "lyric_no_chord":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = FullscreenActivity.sectionContents[x][y];
                        if (FullscreenActivity.showLyrics) {
                            tl.addView(ProcessSong.lyriclinetoTableRow(c, lyrics_returned, fontsize));
                        }
                        break;

                    case "comment_no_chord":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = FullscreenActivity.sectionContents[x][y];
                        tl.addView(ProcessSong.commentlinetoTableRow(c, lyrics_returned, fontsize, false));
                        tl.setBackgroundColor(FullscreenActivity.lyricsCommentColor);
                        break;

                    case "extra_info":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = FullscreenActivity.sectionContents[x][y];
                        TableRow tr = commentlinetoTableRow(c, lyrics_returned, fontsize, false);
                        tr.setGravity(Gravity.RIGHT);
                        tl.addView(tr);
                        tl.setGravity(Gravity.RIGHT);
                        tl.setBackgroundColor(FullscreenActivity.lyricsCustomColor);
                        break;

                case "capo_info":
                    lyrics_returned = new String[1];
                    lyrics_returned[0] = FullscreenActivity.sectionContents[x][y];
                    Log.d("d","Capo line detected = "+lyrics_returned[0]);
                    TableRow trc = commentlinetoTableRow(c, lyrics_returned, fontsize, false);
                    if (trc.getChildAt(0)!=null) {
                        TextView tvcapo = (TextView) trc.getChildAt(0);
                        tvcapo.setTextColor(FullscreenActivity.lyricsCapoColor);
                    }
                    trc.setGravity(Gravity.LEFT);
                    tl.addView(trc);
                    tl.setGravity(Gravity.LEFT);
                    tl.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
                    break;

                case "guitar_tab":
                    lyrics_returned = new String[1];
                    lyrics_returned[0] = FullscreenActivity.sectionContents[x][y];
                    tl.addView(ProcessSong.commentlinetoTableRow(c, lyrics_returned, fontsize, true));
                    tl.setBackgroundColor(FullscreenActivity.lyricsCommentColor);
                    break;

                /*case "abc_notation":
                    WebView wv = ProcessSong.abcnotationtoWebView(c, FullscreenActivity.mNotation);
                    if (wv!=null) {
                        tl.addView(wv);
                    }
                    break;*/
                }
                ll.addView(tl);
            }
        TextView emptyline = new TextView(c);
        emptyline.setLayoutParams(linearlayout_params());
        emptyline.setText(" ");
        emptyline.setTextSize(fontsize*0.5f);
        if (!FullscreenActivity.trimSectionSpace) {
            ll.addView(emptyline);
        }
        return ll;
    }

    static LinearLayout projectedSectionView(Context c, int x, float fontsize) {
        final LinearLayout ll = new LinearLayout(c);

        if (FullscreenActivity.whichMode.equals("Presentation") && !FullscreenActivity.presoShowChords) {
            ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            ll.setGravity(FullscreenActivity.presoLyricsAlign);
        } else {
            ll.setLayoutParams(linearlayout_params());
        }
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0,0,0,0);
        ll.setClipChildren(false);
        ll.setClipToPadding(false);

        String[] whattoprocess;
        String[] linetypes;
        int linenums;

        if (!FullscreenActivity.whichMode.equals("Presentation")) {
            // Identify the section type
            String[] returnvals = beautifyHeadings(FullscreenActivity.songSectionsLabels[x],c);
            if (x<FullscreenActivity.songSectionsTypes.length) {
                FullscreenActivity.songSectionsTypes[x] = returnvals[1];
            }
            ll.addView(titletoTextView(c, returnvals[0], fontsize));
            whattoprocess = FullscreenActivity.sectionContents[x];
            linetypes = FullscreenActivity.sectionLineTypes[x];
            linenums = FullscreenActivity.sectionContents[x].length;

        } else {
            whattoprocess = FullscreenActivity.projectedContents[x];
            linetypes = FullscreenActivity.projectedLineTypes[x];
            linenums = whattoprocess.length;
        }

        String mCapo = FullscreenActivity.mCapo;
        if (mCapo==null || mCapo.isEmpty() || mCapo.equals("")) {
            mCapo = "0";
        }
        int mcapo = Integer.parseInt(mCapo);
        boolean showchordspreso = FullscreenActivity.presoShowChords;
        boolean showcapochords = FullscreenActivity.showCapoChords;
        boolean shownativeandcapochords = FullscreenActivity.showNativeAndCapoChords;
        boolean transposablechordformat = !FullscreenActivity.oldchordformat.equals("4") && !FullscreenActivity.oldchordformat.equals("5");

        // Decide if capo chords are valid and should be shown
        boolean docapochords = showchordspreso && showcapochords && mcapo>0 && mcapo<12 && transposablechordformat;

        // Decide if normal chords should be shown
        // They can't be shown if showchords is true but shownativeandcapochords is false;
        boolean justcapo = docapochords && !shownativeandcapochords;
        boolean donativechords = showchordspreso && !justcapo;

        for (int y = 0; y < linenums; y++) {
            // Go through the section a line at a time
            String nextlinetype = "";
            String previouslinetype = "";
            if (y < linenums - 1) {
                //nextlinetype = FullscreenActivity.sectionLineTypes[x][y + 1];
                nextlinetype = linetypes[y+1];
            }
            if (y > 0) {
                //previouslinetype = FullscreenActivity.sectionLineTypes[x][y - 1];
                previouslinetype = linetypes[y-1];
            }

            String[] positions_returned;
            String[] chords_returned;
            String[] lyrics_returned;
            TableLayout tl = createTableLayout(c);

            if (FullscreenActivity.whichMode.equals("Presentation") && !FullscreenActivity.presoShowChords) {
                tl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }


            String what = ProcessSong.howToProcessLines(y, linenums, linetypes[y], nextlinetype, previouslinetype);
            switch (what) {
                // If this is a chord line followed by a lyric line.

                case "chord_then_lyric":
                    if (whattoprocess[y].length() > whattoprocess[y + 1].length()) {
                        whattoprocess[y + 1] = ProcessSong.fixLineLength(whattoprocess[y + 1], whattoprocess[y].length());
                    }
                    //positions_returned = ProcessSong.getChordPositions(FullscreenActivity.sectionContents[x][y]);
                    //chords_returned = ProcessSong.getChordSections(FullscreenActivity.sectionContents[x][y], positions_returned);
                    //lyrics_returned = ProcessSong.getLyricSections(FullscreenActivity.sectionContents[x][y + 1], positions_returned);
                    positions_returned = ProcessSong.getChordPositions(whattoprocess[y]);
                    chords_returned = ProcessSong.getChordSections(whattoprocess[y], positions_returned);
                    lyrics_returned = ProcessSong.getLyricSections(whattoprocess[y + 1], positions_returned);

                    if (docapochords) {
                        tl.addView(ProcessSong.capolinetoTableRow(c, chords_returned, fontsize));
                    }
                    if (!justcapo && donativechords) {
                        tl.addView(ProcessSong.chordlinetoTableRow(c, chords_returned, fontsize));
                    }
                    if (FullscreenActivity.showLyrics) {
                        tl.addView(ProcessSong.lyriclinetoTableRow(c, lyrics_returned, fontsize));
                    }
                    break;

                case "chord_only":
                    chords_returned = new String[1];
                    chords_returned[0] = whattoprocess[y];
                    if (docapochords) {
                        tl.addView(ProcessSong.capolinetoTableRow(c, chords_returned, fontsize));
                    }
                    if (!justcapo && donativechords) {
                        tl.addView(ProcessSong.chordlinetoTableRow(c, chords_returned, fontsize));
                    }
                    break;

                case "lyric_no_chord":
                case "lyric":
                    lyrics_returned = new String[1];
                    lyrics_returned[0] = whattoprocess[y];
                    if (FullscreenActivity.showLyrics) {
                        tl.addView(ProcessSong.lyriclinetoTableRow(c, lyrics_returned, fontsize));
                    }
                    break;

                case "comment_no_chord":
                    lyrics_returned = new String[1];
                    lyrics_returned[0] = whattoprocess[y];
                    tl.addView(ProcessSong.commentlinetoTableRow(c, lyrics_returned, fontsize, false));
                    break;

                case "extra_info":
                    lyrics_returned = new String[1];
                    lyrics_returned[0] = whattoprocess[y];
                    TableRow tr = commentlinetoTableRow(c, lyrics_returned, fontsize, false);
                    tr.setGravity(Gravity.RIGHT);
                    tl.addView(tr);
                    tl.setGravity(Gravity.RIGHT);
                    break;

                case "guitar_tab":
                    lyrics_returned = new String[1];
                    lyrics_returned[0] = whattoprocess[y];
                    tl.addView(ProcessSong.commentlinetoTableRow(c, lyrics_returned, fontsize, true));
                    break;

            }
            ll.addView(tl);
        }
        TextView emptyline = new TextView(c);
        emptyline.setLayoutParams(linearlayout_params());
        emptyline.setText(" ");
        emptyline.setTextSize(fontsize*0.5f);
        ll.addView(emptyline);
        return ll;
    }

    static LinearLayout createLinearLayout(Context c) {
        final LinearLayout ll = new LinearLayout(c);
        ll.setLayoutParams(linearlayout_params());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setClipChildren(false);
        ll.setClipToPadding(false);
        return ll;
    }

    private static TableLayout createTableLayout(Context c) {
        TableLayout tl = new TableLayout(c);
        tl.setLayoutParams(linearlayout_params());
        tl.setClipChildren(false);
        tl.setClipToPadding(false);
        return tl;
    }

    static Bitmap createPDFPage(Context c, int pagewidth, int pageheight, String scale) {
        String tempsongtitle = FullscreenActivity.songfilename.replace(".pdf", "");
        tempsongtitle = tempsongtitle.replace(".PDF", "");
        FullscreenActivity.mTitle = tempsongtitle;
        FullscreenActivity.mAuthor = "";

        // This only works for post Lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LoadXML.getFileLocation();
            // FileDescriptor for file, it allows you to close file when you are done with it
            ParcelFileDescriptor mFileDescriptor = null;
            PdfRenderer mPdfRenderer = null;

            try {
                mFileDescriptor = ParcelFileDescriptor.open(FullscreenActivity.file, ParcelFileDescriptor.MODE_READ_ONLY);
                if (mFileDescriptor != null) {
                    mPdfRenderer = new PdfRenderer(mFileDescriptor);
                    FullscreenActivity.pdfPageCount = mPdfRenderer.getPageCount();
                }
            } catch (IOException e) {
                e.printStackTrace();
                FullscreenActivity.pdfPageCount = 0;
            }

            if (FullscreenActivity.pdfPageCurrent >= FullscreenActivity.pdfPageCount) {
                FullscreenActivity.pdfPageCurrent = 0;
            }

            // Open page 0
            PdfRenderer.Page mCurrentPage = null;
            if (mPdfRenderer != null) {
                //noinspection AndroidLintNewApi
                mCurrentPage = mPdfRenderer.openPage(FullscreenActivity.pdfPageCurrent);
            }

            // Get pdf size from page
            int pdfwidth;
            int pdfheight;
            if (mCurrentPage != null) {
                pdfwidth = mCurrentPage.getWidth();
                pdfheight = mCurrentPage.getHeight();
            } else {
                pdfwidth = 1;
                pdfheight = 1;
            }

            switch (scale) {
                case "Y":
                    float xscale = (float) pagewidth / (float) pdfwidth;
                    float yscale = (float) pageheight / (float) pdfheight;
                    if (xscale > yscale) {
                        xscale = yscale;
                    } else {
                        yscale = xscale;
                    }
                    pdfheight = (int) ((float) pdfheight * yscale);
                    pdfwidth = (int) ((float) pdfwidth * xscale);
                    break;

                case "W":
                    pdfheight = (int) (((float) pagewidth / (float) pdfwidth) * (float) pdfheight);
                    pdfwidth = pagewidth;
                    break;

                default:
                    // This means pdf will never be bigger than needed (even if scale is off)
                    // This avoids massive files calling out of memory error
                    if (pdfwidth > pagewidth) {
                        pdfheight = (int) (((float) pagewidth / (float) pdfwidth) * (float) pdfheight);
                        pdfwidth = pagewidth;
                    }
                    break;
            }
            if (pdfwidth == 0) {
                pdfwidth = 1;
            }
            if (pdfheight == 0) {
                pdfheight = 1;
            }
            Bitmap bitmap = Bitmap.createBitmap(pdfwidth, pdfheight, Bitmap.Config.ARGB_8888);
            // Be aware this pdf might have transparency.  For now, I've just set the background
            // of the image view to white.  This is fine for most PDF files.

            // Pdf page is rendered on Bitmap
            if (mCurrentPage != null) {
                try {
                    if (PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY>-1) {
                        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mCurrentPage != null) {
                mCurrentPage.close();
            }

            if (mPdfRenderer != null) {
                mPdfRenderer.close();
            }

            if (mFileDescriptor != null) {
                try {
                    mFileDescriptor.close();
                } catch (Exception e) {
                    // Problem closing the file descriptor, but not critical
                }
            }
            return bitmap;

        } else {
            // Make the image to be displayed on the screen a pdf icon
            FullscreenActivity.myToastMessage = c.getResources().getString(R.string.nothighenoughapi);
            ShowToast.showToast(c);

            return null;
        }
    }

    @SuppressWarnings("all")
    public static float getScaleValue(float x, float y, float fontsize) {
        float scale = 1.0f;
        if (FullscreenActivity.thissong_scale==null) {
            FullscreenActivity.thissong_scale = FullscreenActivity.toggleYScale;
        }
        float maxscale = FullscreenActivity.mMaxFontSize/fontsize;
        if (FullscreenActivity.thissong_scale.equals("Y")) {
            if (x>y) {
                scale = y;
            } else {
                scale = x;
            }
            if (scale>maxscale) {
                scale = maxscale;
            }
        }

        if (FullscreenActivity.thissong_scale.equals("W")) {
            scale = x;
            if (scale>maxscale) {
                scale = maxscale;
            }
        }

        if (FullscreenActivity.thissong_scale.equals("N")) {
            scale = FullscreenActivity.mFontSize / fontsize;
        }
        return scale;
    }

    static float getStageScaleValue(float x, float y) {
        float scale;
        if (x>y) {
            scale = y;
        } else {
            scale = x;
        }
        return scale;
    }

    static RelativeLayout preparePerformanceBoxView(Context c, int m, int padding) {
        RelativeLayout boxbit  = new RelativeLayout(c);
        LinearLayout.LayoutParams llp = linearlayout_params();
        llp.setMargins(0,0,m,0);
        boxbit.setLayoutParams(llp);
        boxbit.setBackgroundResource(R.drawable.section_box);
        GradientDrawable drawable = (GradientDrawable) boxbit.getBackground();
        drawable.setColor(0x00000000);                             // Makes the box transparent
        drawable.setStroke(1, FullscreenActivity.lyricsTextColor); // set stroke width and stroke color
        drawable.setCornerRadius(padding);
        int linewidth = (int) (padding - ((float)padding/6.0f))/2;
        boxbit.setPadding(padding-linewidth,padding-linewidth,padding-linewidth,padding-linewidth);
        return boxbit;
    }

    @SuppressWarnings("all")
    static LinearLayout prepareProjectedBoxView(Context c, int m, int padding) {
        LinearLayout boxbit  = createLinearLayout(c);
        LinearLayout.LayoutParams llp = linearlayout_params();
        llp.setMargins(0,0,m,0);
        boxbit.setLayoutParams(llp);
        if (FullscreenActivity.whichMode.equals("Presentation") || FullscreenActivity.whichMode.equals("Stage")) {
            boxbit.setGravity(Gravity.CENTER_VERTICAL);
        }
        if (FullscreenActivity.whichMode.equals("Presentation")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                boxbit.setBackground(null);
                boxbit.setGravity(Gravity.CENTER);
            } else {
                boxbit.setBackgroundDrawable(null);
                boxbit.setGravity(Gravity.CENTER);
            }
        } else {
            boxbit.setBackgroundResource(R.drawable.section_box);
            GradientDrawable drawable = (GradientDrawable) boxbit.getBackground();
            drawable.setColor(0x00000000);                                    // Makes the box transparent
            drawable.setStroke(1, FullscreenActivity.lyricsTextColor); // set stroke width and stroke color
            drawable.setCornerRadius(padding);
            boxbit.setPadding(padding, padding, padding, padding);
        }
        return boxbit;
    }

    @SuppressWarnings("all")
    static LinearLayout prepareStageBoxView(Context c, int m, int padding) {
        LinearLayout boxbit  = new LinearLayout(c);
        LinearLayout.LayoutParams llp = linearlayout_params();
        llp.setMargins(m,m,m,padding);
        boxbit.setLayoutParams(llp);
        boxbit.setBackgroundResource(R.drawable.section_box);
        GradientDrawable drawable = (GradientDrawable) boxbit.getBackground();
        drawable.setColor(0x00000000);                             // Makes the box transparent
        drawable.setStroke(1, FullscreenActivity.lyricsTextColor); // set stroke width and stroke color
        drawable.setCornerRadius(padding);
        int linewidth = (int) (padding - ((float)padding/6.0f))/2;
        boxbit.setPadding(padding-linewidth,padding-linewidth,padding-linewidth,padding-linewidth);
        return boxbit;
    }

    static LinearLayout preparePerformanceColumnView(Context c) {
        LinearLayout column = new LinearLayout(c);
        column.setLayoutParams(linearlayout_params());
        column.setOrientation(LinearLayout.VERTICAL);
        column.setClipChildren(false);
        column.setClipToPadding(false);
        return column;
    }

    @SuppressWarnings("all")
    public static LinearLayout preparePerformanceSongBitView(Context c,boolean horizontal) {
        LinearLayout songbit = new LinearLayout(c);
        if (horizontal) {
            songbit.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            songbit.setOrientation(LinearLayout.VERTICAL);
        }
        songbit.setLayoutParams(linearlayout_params());
        songbit.setClipChildren(false);
        songbit.setClipToPadding(false);
        return songbit;
    }

    static LinearLayout prepareStageSongBitView(Context c) {
        LinearLayout songbit = new LinearLayout(c);
        songbit.setOrientation(LinearLayout.VERTICAL);
        songbit.setLayoutParams(linearlayout_params());
        songbit.setClipChildren(false);
        songbit.setClipToPadding(false);
        return songbit;
    }

    static float setScaledFontSize(int s) {
        float tempfontsize = 12.0f * FullscreenActivity.sectionScaleValue[s];

        int start = (int) tempfontsize;
        float end = tempfontsize - start;
        if (end<0.5) {
            return (float) start - 0.1f;
        } else {
            return (float) start + 0.4f;
        }
    }

    static float getProjectedFontSize(float scale) {
        float tempfontsize = 12.0f * scale;
        int start = (int) tempfontsize;
        float end = tempfontsize - start;
        if (end<0.5) {
            return (float) start - 0.1f;
        } else {
            return (float) start + 0.4f;
        }
    }

    static void addExtraInfo(Context c) {
        String nextinset = "";
        if (FullscreenActivity.setView) {
            // Get the index in the set
            if (!FullscreenActivity.nextSongInSet.equals("")) {
                FullscreenActivity.nextSongKeyInSet = LoadXML.grabNextSongInSetKey(c,FullscreenActivity.nextSongInSet);
                nextinset = ";__" + c.getString(R.string.next) + ": " + FullscreenActivity.nextSongInSet;
                if (!FullscreenActivity.nextSongKeyInSet.equals("")) {
                    nextinset = nextinset + " (" + FullscreenActivity.nextSongKeyInSet + ")";
                }
            } else {
                nextinset = ";__"  + c.getResources().getString(R.string.lastsong);
            }
        }

        String capoDetails = "";
        /*if (FullscreenActivity.showCapoChords && !FullscreenActivity.mCapo.equals("")) {
            //capoDetails = ";__" + c.getResources().getString(R.string.edit_song_capo) + " " + FullscreenActivity.mCapo + "\n\n";
            //FullscreenActivity.myToastMessage = c.getResources().getString(R.string.edit_song_capo) + " " + FullscreenActivity.mCapo + "\n\n";
            //ShowToast.showToast(c);
        }*/

        String stickyNotes = "";
        if (FullscreenActivity.toggleAutoSticky.equals("T")||FullscreenActivity.toggleAutoSticky.equals("B")) {
            String notes[] = FullscreenActivity.mNotes.split("\n");
            stickyNotes += ";__"+c.getString(R.string.note)+": ";
            for (String line:notes) {
                stickyNotes += ";__" + line + "\n";
            }
            stickyNotes = stickyNotes.replace(";__"+c.getString(R.string.note)+": "+";__",";__"+c.getString(R.string.note)+": ");
            if (FullscreenActivity.toggleAutoSticky.equals("T") && !FullscreenActivity.mNotes.equals("")) {
                FullscreenActivity.myLyrics = stickyNotes + "\n" + FullscreenActivity.myLyrics;
            }
        }

        // If we want to add this to the top of the song page,
        if (FullscreenActivity.setView &&
                FullscreenActivity.indexSongInSet < FullscreenActivity.mSetList.length &&
                FullscreenActivity.showNextInSet.equals(("top"))) {
            FullscreenActivity.myLyrics = nextinset + "\n" + FullscreenActivity.myLyrics;
        }

        if (FullscreenActivity.toggleAutoSticky.equals("B")) {
            if (!FullscreenActivity.mNotes.equals("")) {
                FullscreenActivity.myLyrics = FullscreenActivity.myLyrics + "\n\n" + stickyNotes;
            }
        }

        // If we want to add this to the top of the song page,
        if (FullscreenActivity.setView &&
                FullscreenActivity.indexSongInSet < FullscreenActivity.mSetList.length &&
                FullscreenActivity.showNextInSet.equals(("bottom"))) {
            FullscreenActivity.myLyrics = FullscreenActivity.myLyrics + "\n\n" + nextinset;
        }

        if (!capoDetails.equals("")) {
            FullscreenActivity.myLyrics = capoDetails + FullscreenActivity.myLyrics;
        }

    }

    // The stuff for PresenterMode
    static Button makePresenterSetButton(int x, Context c) {
        Button newButton = new Button(c);
        String buttonText = FullscreenActivity.mSetList[x];
        newButton.setText(buttonText);
        newButton.setBackgroundResource(R.drawable.present_section_setbutton);
        newButton.setTextSize(10.0f);
        newButton.setTextColor(0xffffffff);
        newButton.setTransformationMethod(null);
        newButton.setPadding(10, 10, 10, 10);
        newButton.setMinimumHeight(0);
        newButton.setMinHeight(0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 5, 5, 20);
        newButton.setLayoutParams(params);
        return newButton;
    }
    static void highlightPresenterSetButton(Button b) {
        b.setBackgroundResource(R.drawable.present_section_setbutton_active);
        b.setTextSize(10.0f);
        b.setTextColor(0xff000000);
        b.setPadding(10, 10, 10, 10);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
    }
    static void unhighlightPresenterSetButton(Button b) {
        b.setBackgroundResource(R.drawable.present_section_setbutton);
        b.setTextSize(10.0f);
        b.setTextColor(0xffffffff);
        b.setPadding(10, 10, 10, 10);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
    }
    static LinearLayout makePresenterSongButtonLayout(Context c) {
        LinearLayout ll = new LinearLayout(c);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 5, 5, 10);
        ll.setLayoutParams(params);

        return ll;
    }
    static TextView makePresenterSongButtonSection(Context c, String s) {
        TextView tv = new TextView(c);
        tv.setText(s);
        tv.setTextColor(0xffffffff);
        tv.setTextSize(10.0f);
        tv.setPadding(5, 5, 10, 5);
        return tv;
    }
    static Button makePresenterSongButtonContent(Context c, String s) {
        Button b = new Button(c);
        b.setText(s.trim());
        b.setTransformationMethod(null);
        b.setBackgroundResource(R.drawable.present_section_button);
        b.setTextSize(10.0f);
        b.setTextColor(0xffffffff);
        b.setPadding(10, 10, 10, 10);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
        return b;
    }
    static void highlightPresenterSongButton(Button b) {
        b.setBackgroundResource(R.drawable.present_section_button_active);
        b.setTextSize(10.0f);
        b.setTextColor(0xff000000);
        b.setPadding(10, 10, 10, 10);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
    }
    static void unhighlightPresenterSongButton(Button b) {

        b.setBackgroundResource(R.drawable.present_section_button);
        b.setTextSize(10.0f);
        b.setTextColor(0xffffffff);
        b.setPadding(10, 10, 10, 10);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
    }

    // The stuff for the highlighter notes
    static File getHighlightFile(Context c) {
        String layout;
        String highlighterfile;
        if (c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layout = "_p";
        } else {
            layout = "_l";
        }
        if (FullscreenActivity.whichSongFolder.equals(c.getString(R.string.mainfoldername)) ||
                FullscreenActivity.whichSongFolder.equals("")) {
            highlighterfile = FullscreenActivity.mainfoldername + "_" + FullscreenActivity.songfilename;
        } else {
            highlighterfile = FullscreenActivity.whichSongFolder.replace("/","_") + "_" + FullscreenActivity.songfilename;
        }
        String page = "";
        if (FullscreenActivity.isPDF) {
            // Because pdf files can have multiple pages, this allows different notes.
            page = "_" + FullscreenActivity.pdfPageCurrent;
        }
        highlighterfile =  highlighterfile + layout + page + ".png";

        // This file may or may not exist
        return new File (FullscreenActivity.dirhighlighter,highlighterfile);
    }

}