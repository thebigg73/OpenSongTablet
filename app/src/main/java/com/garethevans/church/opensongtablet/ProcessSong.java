package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfRenderer;
import android.graphics.text.LineBreaker;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class ProcessSong extends Activity {

    private final String TAG = "ProcessSong";

    String parseLyrics(String myLyrics, Context c) {
        myLyrics = myLyrics.replace("]\n\n", "]\n");
        myLyrics = myLyrics.replaceAll("\r\n", "\n");
        myLyrics = myLyrics.replaceAll("\r", "\n");
        myLyrics = myLyrics.replaceAll("\\t", "    ");
        myLyrics = myLyrics.replaceAll("\f", "    ");
        myLyrics = myLyrics.replace("\r", "");
        myLyrics = myLyrics.replace("\t", "    ");
        myLyrics = myLyrics.replace("\b", "    ");
        myLyrics = myLyrics.replace("\f", "    ");
        myLyrics = myLyrics.replace("&#27;", "'");
        myLyrics = myLyrics.replace("&#x27;", "'");
        myLyrics = myLyrics.replace("&#027;", "'");
        myLyrics = myLyrics.replace("&#39;", "'");
        myLyrics = myLyrics.replace("&#34;", "'");
        myLyrics = myLyrics.replace("&#039;", "'");
        myLyrics = myLyrics.replace("&ndash;", "-");
        myLyrics = myLyrics.replace("&mdash;", "-");
        myLyrics = myLyrics.replace("&apos;", "'");
        myLyrics = myLyrics.replace("&lt;", "<");
        myLyrics = myLyrics.replace("&gt;", ">");
        myLyrics = myLyrics.replace("&quot;", "\"");
        myLyrics = myLyrics.replace("&rdquo;", "'");
        myLyrics = myLyrics.replace("&rdquor;", "'");
        myLyrics = myLyrics.replace("&rsquo;", "'");
        myLyrics = myLyrics.replace("&amp;rsquo;", "'");
        myLyrics = myLyrics.replace("&rdquor;", "'");
        myLyrics = myLyrics.replaceAll("\u0092", "'");
        myLyrics = myLyrics.replaceAll("\u0093", "'");
        myLyrics = myLyrics.replaceAll("\u2018", "'");
        myLyrics = myLyrics.replaceAll("\u2019", "'");

        // If UG has been bad, replace these bits:
        myLyrics = myLyrics.replace("pre class=\"\"", "");

        if (!StaticVariables.whichSongFolder.contains(c.getResources().getString(R.string.slide)) &&
                !StaticVariables.whichSongFolder.contains(c.getResources().getString(R.string.image)) &&
                !StaticVariables.whichSongFolder.contains(c.getResources().getString(R.string.note)) &&
                !StaticVariables.whichSongFolder.contains(c.getResources().getString(R.string.scripture))) {
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
        String languageverse_lowercase = languageverse.toLowerCase(StaticVariables.locale);
        String languageverse_uppercase = languageverse.toUpperCase(StaticVariables.locale);
        myLyrics = myLyrics.replace("[" + languageverse_lowercase, "[" + languageverse);
        myLyrics = myLyrics.replace("[" + languageverse_uppercase, "[" + languageverse);
        myLyrics = myLyrics.replace("[" + languageverse + "]", "[V]");
        myLyrics = myLyrics.replace("[" + languageverse + " 1]", "[V1]");
        myLyrics = myLyrics.replace("[" + languageverse + " 2]", "[V2]");
        myLyrics = myLyrics.replace("[" + languageverse + " 3]", "[V3]");
        myLyrics = myLyrics.replace("[" + languageverse + " 4]", "[V4]");
        myLyrics = myLyrics.replace("[" + languageverse + " 5]", "[V5]");
        myLyrics = myLyrics.replace("[" + languageverse + " 6]", "[V6]");
        myLyrics = myLyrics.replace("[" + languageverse + " 7]", "[V7]");
        myLyrics = myLyrics.replace("[" + languageverse + " 8]", "[V8]");
        myLyrics = myLyrics.replace("[" + languageverse + " 9]", "[V9]");

        // Replace [Chorus] with [C] and [Chorus 1] with [C1]
        String languagechorus = c.getResources().getString(R.string.tag_chorus);
        String languagechorus_lowercase = languagechorus.toLowerCase(StaticVariables.locale);
        String languagechorus_uppercase = languagechorus.toUpperCase(StaticVariables.locale);
        myLyrics = myLyrics.replace("[" + languagechorus_lowercase, "[" + languagechorus);
        myLyrics = myLyrics.replace("[" + languagechorus_uppercase, "[" + languagechorus);
        myLyrics = myLyrics.replace("[" + languagechorus + "]", "[C]");
        myLyrics = myLyrics.replace("[" + languagechorus + " 1]", "[C1]");
        myLyrics = myLyrics.replace("[" + languagechorus + " 2]", "[C2]");
        myLyrics = myLyrics.replace("[" + languagechorus + " 3]", "[C3]");
        myLyrics = myLyrics.replace("[" + languagechorus + " 4]", "[C4]");
        myLyrics = myLyrics.replace("[" + languagechorus + " 5]", "[C5]");
        myLyrics = myLyrics.replace("[" + languagechorus + " 6]", "[C6]");
        myLyrics = myLyrics.replace("[" + languagechorus + " 7]", "[C7]");
        myLyrics = myLyrics.replace("[" + languagechorus + " 8]", "[C8]");
        myLyrics = myLyrics.replace("[" + languagechorus + " 9]", "[C9]");

        // Try to convert ISO / Windows
        myLyrics = myLyrics.replace("\0x91", "'");

        // Get rid of BOMs and stuff
        myLyrics = myLyrics.replace("\uFEFF", "");
        myLyrics = myLyrics.replace("\uFEFF", "");
        myLyrics = myLyrics.replace("[&#x27;]", "");
        myLyrics = myLyrics.replace("[\\xEF]", "");
        myLyrics = myLyrics.replace("[\\xBB]", "");
        myLyrics = myLyrics.replace("[\\xFF]", "");
        myLyrics = myLyrics.replace("\\xEF", "");
        myLyrics = myLyrics.replace("\\xBB", "");
        myLyrics = myLyrics.replace("\\xFF", "");

        return myLyrics;
    }

    String fixStartOfLines(String lyrics) {
        StringBuilder fixedlyrics = new StringBuilder();
        String[] lines = lyrics.split("\n");

        for (String line : lines) {
            if (!line.startsWith("[") && !line.startsWith(";") && !line.startsWith(".") && !line.startsWith(" ") &&
                    !line.startsWith("1") && !line.startsWith("2") && !line.startsWith("3") && !line.startsWith("4") &&
                    !line.startsWith("5") && !line.startsWith("6") && !line.startsWith("7") && !line.startsWith("8") &&
                    !line.startsWith("9") && !line.startsWith("-")) {
                line = " " + line;
            }
            fixedlyrics.append(line).append("\n");
        }
        return fixedlyrics.toString();
    }

    String fixlinebreaks(String string) {
        string = string.replace("\r\n", "\n");
        string = string.replace("\n\r", "\n");
        string = string.replace("\r", "\n");
        string = string.replace("<br>", "\n");
        string = string.replace("<p>", "\n\n");
        return string;
    }

    String removeUnderScores(Context c, Preferences preferences, String myLyrics) {
        // Go through the lines and remove underscores if the line isn't an image location
        // Split the lyrics into a line by line array so we can fix individual lines
        String[] lineLyrics = myLyrics.split("\n");
        StringBuilder myLyricsBuilder = new StringBuilder();
        for (int l = 0; l < lineLyrics.length; l++) {

            if (lineLyrics[l].contains("_")) {
                if (l > 0 && !lineLyrics[l].contains("[" + c.getResources().getString(R.string.image) + "_") &&
                        !lineLyrics[l - 1].contains("[" + c.getResources().getString(R.string.image) + "_")) {
                    if (StaticVariables.whichMode.equals("Presentation") && !preferences.getMyPreferenceBoolean(c, "presoShowChords", false)) {
                        lineLyrics[l] = lineLyrics[l].replace("_", "");
                    } else if ((StaticVariables.whichMode.equals("Stage") || StaticVariables.whichMode.equals("Performance")) &&
                            !preferences.getMyPreferenceBoolean(c, "displayChords", true)) {
                        lineLyrics[l] = lineLyrics[l].replace("_", "");
                    } else {
                        lineLyrics[l] = lineLyrics[l].replace("_", " ");
                    }
                } else if (l == 0 && !lineLyrics[l].contains("[" + c.getResources().getString(R.string.image) + "_")) {

                    if (StaticVariables.whichMode.equals("Presentation") && !preferences.getMyPreferenceBoolean(c, "presoShowChords", false)) {
                        lineLyrics[l] = lineLyrics[l].replace("_", "");
                    } else if ((StaticVariables.whichMode.equals("Stage") || StaticVariables.whichMode.equals("Performance")) &&
                            !preferences.getMyPreferenceBoolean(c, "displayChords", true)) {
                        lineLyrics[l] = lineLyrics[l].replace("_", "");
                    } else {
                        lineLyrics[l] = lineLyrics[l].replace("_", " ");
                    }
                }
            }
            myLyricsBuilder.append(lineLyrics[l]).append("\n");
        }
        myLyrics = myLyricsBuilder.toString();
        return myLyrics;
    }

    String removeUnwantedSymbolsAndSpaces(Context c, Preferences preferences, String string) {
        // Replace unwanted symbols
        // Split into lines
        //string = string.replace("|", "\n");
        if (StaticVariables.whichMode.equals("Presentation") && !preferences.getMyPreferenceBoolean(c, "presoShowChords", false)) {
            string = string.replace("_", "");
        } else if ((StaticVariables.whichMode.equals("Stage") || StaticVariables.whichMode.equals("Performance")) &&
                !preferences.getMyPreferenceBoolean(c, "displayChords", true)) {
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

    String[] beautifyHeadings(String string, Context c) {

        if (string == null) {
            string = "";
        }

        string = string.replace("[", "");
        string = string.replace("]", "");
        String section;

        // Fix for filtered section labels
        if (string.contains(":V") || string.contains(":C") ||
        string.contains(":B") || string.contains(":T") ||
        string.contains(":P")) {
            string = string.substring(string.indexOf(":")+1);
        }

        if (!FullscreenActivity.foundSongSections_heading.contains(string)) {
            FullscreenActivity.foundSongSections_heading.add(string);
        }

        switch (string) {
            case "V-":
            case "V - ":
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
            case "V1-":
            case "V2-":
            case "V3-":
            case "V4-":
            case "V5-":
            case "V6-":
            case "V7-":
            case "V8-":
            case "V9-":
            case "V10-":
            case "V1 -":
            case "V2 -":
            case "V3 -":
            case "V4 -":
            case "V5 -":
            case "V6 -":
            case "V7 -":
            case "V8 -":
            case "V9 -":
            case "V - 10":
                string = removeAnnotatedSections(string);
                string = string.replace("V", c.getResources().getString(R.string.tag_verse) + " ");
                section = "verse";
                break;

            case "T-":
            case "T -":
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
                string = removeAnnotatedSections(string);
                string = string.replace("T", c.getResources().getString(R.string.tag_tag) + " ");
                section = "tag";
                break;

            case "C-":
            case "C -":
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
                string = removeAnnotatedSections(string);
                string = string.replace("C", c.getResources().getString(R.string.tag_chorus) + " ");
                section = "chorus";
                break;

            case "B-":
            case "B -":
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
                string = removeAnnotatedSections(string);
                string = string.replace("B", c.getResources().getString(R.string.tag_bridge) + " ");
                section = "bridge";
                break;

            case "P-":
            case "P -":
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
                string = removeAnnotatedSections(string);
                string = string.replace("P", c.getResources().getString(R.string.tag_prechorus) + " ");
                section = "prechorus";
                break;
            default:
                string = removeAnnotatedSections(string);
                section = "custom";
                break;
        }

        // Look for caps or English tags for non-English app users
        if (string.toLowerCase(StaticVariables.locale).contains(c.getResources().getString(R.string.tag_verse)) ||
                string.toLowerCase(StaticVariables.locale).contains("verse")) {
            section = "verse";
        } else if (string.toLowerCase(StaticVariables.locale).contains(c.getResources().getString(R.string.tag_prechorus)) ||
                string.toLowerCase(StaticVariables.locale).contains("prechorus") ||
                string.toLowerCase(StaticVariables.locale).contains("pre-chorus")) {
            section = "prechorus";
        } else if (string.toLowerCase(StaticVariables.locale).contains(c.getResources().getString(R.string.tag_chorus)) ||
                string.toLowerCase(StaticVariables.locale).contains("chorus")) {
            section = "chorus";
        } else if (string.toLowerCase(StaticVariables.locale).contains(c.getResources().getString(R.string.tag_tag)) ||
                string.toLowerCase(StaticVariables.locale).contains("tag")) {
            section = "tag";
        } else if (string.toLowerCase(StaticVariables.locale).contains(c.getResources().getString(R.string.tag_bridge)) ||
                string.toLowerCase(StaticVariables.locale).contains("bridge")) {
            section = "bridge";
        }

        string = string.trim();
        String[] vals = new String[2];
        vals[0] = string;
        vals[1] = section;
        return vals;
    }

    String howToProcessLines(int linenum, int totallines, String thislinetype, String nextlinetype, String previouslinetype) {
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
        } else if (thislinetype.equals("heading")) {
            what = "heading";
            //} else if (thislinetype.equals("abcnotation")) {
            //    what = "abc_notation";
        } else {

            what = "null"; // Probably a lyric line with a chord above it - already dealt with
        }
        return what;
    }

    private String removeAnnotatedSections(String s) {
        // List things to remove
        String[] removethisbit = {
                "V-", "V1-", "V2-", "V3-", "V4-", "V5-", "V6-", "V7-", "V8-", "V9-", "V10-",
                "V -", "V1 -", "V2 -", "V3 -", "V4 -", "V5 -", "V6 -", "V7 -", "V8 -", "V9 -", "V10 -",
                "C-", "C1-", "C2-", "C3-", "C4-", "C5-", "C6-", "C7-", "C8-", "C9-", "C10-",
                "C -", "C1 -", "C2 -", "C3 -", "C4 -", "C5 -", "C6 -", "C7 -", "C8 -", "C9 -", "C10 -",
                "P-", "P1-", "P2-", "P3-", "P4-", "P5-", "P6-", "P7-", "P8-", "P9-", "P10-",
                "P -", "P1 -", "P2 -", "P3 -", "P4 -", "P5 -", "P6 -", "P7 -", "P8 -", "P9 -", "P10 -",
                "T-", "T1-", "T2-", "T3-", "T4-", "T5-", "T6-", "T7-", "T8-", "T9-", "T10-",
                "T -", "T1 -", "T2 -", "T3 -", "T4 -", "T5 -", "T6 -", "T7 -", "T8 -", "T9 -", "T10 -",
                "B-", "B -", "I-", "I -"
        };

        for (String sr : removethisbit) {
            s = s.replace(sr, "");
        }
        return s;
    }

    String rebuildParsedLyrics(int length) {
        StringBuilder tempLyrics = new StringBuilder();
        for (int x = 0; x < length; x++) {
            // First line of section should be the label, so replace it with label.
            if (StaticVariables.songSections[x].startsWith("[" + StaticVariables.songSectionsLabels[x] + "]")) {
                tempLyrics.append(StaticVariables.songSections[x]).append("\n");
            } else if (StaticVariables.songSectionsLabels[x] != null &&
                    !StaticVariables.songSectionsLabels[x].isEmpty()) {
                tempLyrics.append("[").append(StaticVariables.songSectionsLabels[x]).append("]\n");
                tempLyrics.append(StaticVariables.songSections[x]).append("\n");
            }
        }
        FullscreenActivity.myParsedLyrics = tempLyrics.toString().split("\n");

        return tempLyrics.toString();
    }

    void lookForSplitPoints() {
        // Script to determine 2 column split details
        int halfwaypoint = Math.round((float) FullscreenActivity.numrowstowrite / 2.0f);

        // Look for nearest split point before halfway
        int splitpoint_1sthalf = 0;
        boolean gotityet = false;
        for (int scan = halfwaypoint; scan > 0; scan--) {
            if (!gotityet) {
                if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].startsWith("[")) {
                    gotityet = true;
                    splitpoint_1sthalf = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
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
                if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
                    gotityet2 = true;
                    splitpoint_2ndhalf = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
                    gotityet2 = true;
                    splitpoint_2ndhalf = scan + 1;
                }
            }
        }

        // Script to determine 3 columns split details
        int thirdwaypoint = Math.round(FullscreenActivity.numrowstowrite / 3.0f);
        int twothirdwaypoint = thirdwaypoint * 2;

        // Look for nearest split point before thirdway
        int splitpoint_beforethirdway = 0;
        boolean gotityet_beforethirdway = false;
        for (int scan = thirdwaypoint; scan > 0; scan--) {
            if (!gotityet_beforethirdway) {
                if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
                    gotityet_beforethirdway = true;
                    splitpoint_beforethirdway = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
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
                if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
                    gotityet_pastthirdway = true;
                    splitpoint_pastthirdway = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
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
                if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
                    gotityet_beforetwothirdway = true;
                    splitpoint_beforetwothirdway = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
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
                if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
                    gotityet_pasttwothirdway = true;
                    splitpoint_pasttwothirdway = scan;
                } else if (FullscreenActivity.myParsedLyrics[scan] != null && FullscreenActivity.myParsedLyrics[scan].length() == 0) {
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
        if (StaticVariables.songSections.length == 1) {
            FullscreenActivity.splitpoint = splitpoint_2ndhalf;
        }

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
        int halfsplit_section = StaticVariables.songSections.length;
        int thirdsplit_section = StaticVariables.songSections.length;
        int twothirdsplit_section = StaticVariables.songSections.length;
        int lineweareon = 0;
        // Go through the sections and get the line number we're on
        for (int s = 0; s < StaticVariables.songSections.length; s++) {
            lineweareon += StaticVariables.sectionContents[s].length;
            if (halfsplit_section == StaticVariables.songSections.length && FullscreenActivity.splitpoint < lineweareon) {
                halfsplit_section = s;
            }
            if (thirdsplit_section == StaticVariables.songSections.length && FullscreenActivity.thirdsplitpoint < lineweareon) {
                thirdsplit_section = s;
            }
            if (twothirdsplit_section == StaticVariables.songSections.length && FullscreenActivity.twothirdsplitpoint < lineweareon) {
                twothirdsplit_section = s;
            }
        }
        FullscreenActivity.halfsplit_section = halfsplit_section;
        FullscreenActivity.thirdsplit_section = thirdsplit_section;
        FullscreenActivity.twothirdsplit_section = twothirdsplit_section;
    }
    String fixLineLength(String string, int newlength) {
        int extraspacesrequired = newlength - string.length();
        StringBuilder stringBuilder = new StringBuilder(string);
        for (int x = 0; x < extraspacesrequired; x++) {
            stringBuilder.append(" ");
        }
        string = stringBuilder.toString();
        return string;
    }

    private String validCustomPadString(Context c, Preferences preferences, StorageAccess storageAccess, String s, String custom) {
        if (custom != null && !custom.isEmpty()) {
            // Null is the built in auto pad.  So, not using that.  Test it exists.
            Uri uri = storageAccess.getUriForItem(c, preferences, "Pads", "", custom);
            if (storageAccess.uriExists(c, uri)) {
                s = "custom_" + custom;
            }
        }
        return s;
    }

    void processKey(Context c, Preferences preferences, StorageAccess storageAccess) {
        switch (StaticVariables.mKey) {
            case "A":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "a",
                        preferences.getMyPreferenceString(c, "customPadA", ""));
                FullscreenActivity.keyindex = 1;
                break;
            case "A#":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "asharp",
                        preferences.getMyPreferenceString(c, "customPadBb", ""));
                FullscreenActivity.keyindex = 2;
                break;
            case "Bb":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "asharp",
                        preferences.getMyPreferenceString(c, "customPadBb", ""));
                FullscreenActivity.keyindex = 3;
                break;
            case "B":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "b",
                        preferences.getMyPreferenceString(c, "customPadB", ""));
                FullscreenActivity.keyindex = 4;
                break;
            case "C":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "c",
                        preferences.getMyPreferenceString(c, "customPadC", ""));
                FullscreenActivity.keyindex = 5;
                break;
            case "C#":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "csharp",
                        preferences.getMyPreferenceString(c, "customPadDb", ""));
                FullscreenActivity.keyindex = 6;
                break;
            case "Db":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "csharp",
                        preferences.getMyPreferenceString(c, "customPadDb", ""));
                FullscreenActivity.keyindex = 7;
                break;
            case "D":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "d",
                        preferences.getMyPreferenceString(c, "customPadD", ""));
                FullscreenActivity.keyindex = 8;
                break;
            case "D#":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "dsharp",
                        preferences.getMyPreferenceString(c, "customPadEb", ""));
                FullscreenActivity.keyindex = 9;
                break;
            case "Eb":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "dsharp",
                        preferences.getMyPreferenceString(c, "customPadEb", ""));
                FullscreenActivity.keyindex = 10;
                break;
            case "E":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "e",
                        preferences.getMyPreferenceString(c, "customPadE", ""));
                FullscreenActivity.keyindex = 11;
                break;
            case "F":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "f",
                        preferences.getMyPreferenceString(c, "customPadF", ""));
                FullscreenActivity.keyindex = 12;
                break;
            case "F#":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "fsharp",
                        preferences.getMyPreferenceString(c, "customPadGb", ""));
                FullscreenActivity.keyindex = 13;
                break;
            case "Gb":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "fsharp",
                        preferences.getMyPreferenceString(c, "customPadGb", ""));
                FullscreenActivity.keyindex = 14;
                break;
            case "G":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "g",
                        preferences.getMyPreferenceString(c, "customPadG", ""));
                FullscreenActivity.keyindex = 15;
                break;
            case "G#":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "gsharp",
                        preferences.getMyPreferenceString(c, "customPadAb", ""));
                FullscreenActivity.keyindex = 16;
                break;
            case "Ab":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "gsharp",
                        preferences.getMyPreferenceString(c, "customPadAb", ""));
                FullscreenActivity.keyindex = 17;
                break;
            case "Am":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "am",
                        preferences.getMyPreferenceString(c, "customPadAm", ""));
                FullscreenActivity.keyindex = 18;
                break;
            case "A#m":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "asharpm",
                        preferences.getMyPreferenceString(c, "customPadBbm", ""));
                FullscreenActivity.keyindex = 19;
                break;
            case "Bbm":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "asharpm",
                        preferences.getMyPreferenceString(c, "customPadBbm", ""));
                FullscreenActivity.keyindex = 20;
                break;
            case "Bm":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "bm",
                        preferences.getMyPreferenceString(c, "customPadBm", ""));
                FullscreenActivity.keyindex = 21;
                break;
            case "Cm":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "cm",
                        preferences.getMyPreferenceString(c, "customPadCm", ""));
                FullscreenActivity.keyindex = 22;
                break;
            case "C#m":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "csharpm",
                        preferences.getMyPreferenceString(c, "customPadDbm", ""));
                FullscreenActivity.keyindex = 23;
                break;
            case "Dbm":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "csharpm",
                        preferences.getMyPreferenceString(c, "customPadDbm", ""));
                FullscreenActivity.keyindex = 24;
                break;
            case "Dm":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "dm",
                        preferences.getMyPreferenceString(c, "customPadDm", ""));
                FullscreenActivity.keyindex = 25;
                break;
            case "D#m":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "dsharpm",
                        preferences.getMyPreferenceString(c, "customPadEbm", ""));
                FullscreenActivity.keyindex = 26;
                break;
            case "Ebm":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "dsharpm",
                        preferences.getMyPreferenceString(c, "customPadEbm", ""));
                FullscreenActivity.keyindex = 27;
                break;
            case "Em":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "em",
                        preferences.getMyPreferenceString(c, "customPadEm", ""));
                FullscreenActivity.keyindex = 28;
                break;
            case "Fm":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "fm",
                        preferences.getMyPreferenceString(c, "customPadFm", ""));
                FullscreenActivity.keyindex = 29;
                break;
            case "F#m":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "fsharpm",
                        preferences.getMyPreferenceString(c, "customPadGbm", ""));
                FullscreenActivity.keyindex = 30;
                break;
            case "Gbm":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "fsharpm",
                        preferences.getMyPreferenceString(c, "customPadGbm", ""));
                FullscreenActivity.keyindex = 31;
                break;
            case "Gm":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "gm",
                        preferences.getMyPreferenceString(c, "customPadGm", ""));
                FullscreenActivity.keyindex = 32;
                break;
            case "G#m":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "gsharpm",
                        preferences.getMyPreferenceString(c, "customPadAbm", ""));
                FullscreenActivity.keyindex = 33;
                break;
            case "Abm":
                StaticVariables.pad_filename = validCustomPadString(c, preferences, storageAccess, "gsharpm",
                        preferences.getMyPreferenceString(c, "customPadAbm", ""));
                FullscreenActivity.keyindex = 34;
                break;
            default:
                StaticVariables.pad_filename = "";
                FullscreenActivity.keyindex = 0;
        }
    }

    static void processTimeSig() {
        switch (StaticVariables.mTimeSig) {
            case "2/2":
                FullscreenActivity.beats = 2;
                FullscreenActivity.noteValue = 2;
                StaticVariables.mTimeSigValid = true;
                break;
            case "2/4":
                FullscreenActivity.beats = 2;
                FullscreenActivity.noteValue = 4;
                StaticVariables.mTimeSigValid = true;
                break;
            case "3/2":
                FullscreenActivity.beats = 3;
                FullscreenActivity.noteValue = 2;
                StaticVariables.mTimeSigValid = true;
                break;
            case "3/4":
                FullscreenActivity.beats = 3;
                FullscreenActivity.noteValue = 4;
                StaticVariables.mTimeSigValid = true;
                break;
            case "3/8":
                FullscreenActivity.beats = 3;
                FullscreenActivity.noteValue = 8;
                StaticVariables.mTimeSigValid = true;
                break;
            case "4/4":
                FullscreenActivity.beats = 4;
                FullscreenActivity.noteValue = 4;
                StaticVariables.mTimeSigValid = true;
                break;
            case "5/4":
                FullscreenActivity.beats = 5;
                FullscreenActivity.noteValue = 4;
                StaticVariables.mTimeSigValid = true;
                break;
            case "5/8":
                FullscreenActivity.beats = 5;
                FullscreenActivity.noteValue = 8;
                StaticVariables.mTimeSigValid = true;
                break;
            case "6/4":
                FullscreenActivity.beats = 6;
                FullscreenActivity.noteValue = 4;
                StaticVariables.mTimeSigValid = true;
                break;
            case "6/8":
                FullscreenActivity.beats = 6;
                FullscreenActivity.noteValue = 8;
                StaticVariables.mTimeSigValid = true;
                break;
            case "7/4":
                FullscreenActivity.beats = 7;
                FullscreenActivity.noteValue = 4;
                StaticVariables.mTimeSigValid = true;
                break;
            case "7/8":
                FullscreenActivity.beats = 7;
                FullscreenActivity.noteValue = 8;
                StaticVariables.mTimeSigValid = true;
                break;
            case "1/4":
                FullscreenActivity.beats = 1;
                FullscreenActivity.noteValue = 4;
                StaticVariables.mTimeSigValid = true;
                break;
            default:
                FullscreenActivity.beats = 4;
                FullscreenActivity.noteValue = 4;
                StaticVariables.mTimeSigValid = false;
                break;
        }
    }

    boolean isAutoScrollValid(Context c, Preferences preferences) {
        // Get the autoScrollDuration;
        if (StaticVariables.mDuration.isEmpty() &&
                preferences.getMyPreferenceBoolean(c, "autoscrollUseDefaultTime", false)) {
            StaticVariables.autoScrollDuration = preferences.getMyPreferenceInt(c, "autoscrollDefaultSongLength", 180);
        } else if (StaticVariables.mDuration.isEmpty() &&
                !preferences.getMyPreferenceBoolean(c, "autoscrollUseDefaultTime", false)) {
            StaticVariables.autoScrollDuration = -1;
        } else {
            try {
                StaticVariables.autoScrollDuration = Integer.parseInt(StaticVariables.mDuration.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                StaticVariables.autoScrollDuration = 0;
            }
        }

        // Get the autoScrollDelay;
        if (StaticVariables.mPreDelay.isEmpty() && preferences.getMyPreferenceBoolean(c, "autoscrollUseDefaultTime", false)) {
            StaticVariables.autoScrollDelay = preferences.getMyPreferenceInt(c, "autoscrollDefaultSongPreDelay", 10);
        } else if (StaticVariables.mDuration.isEmpty() && !preferences.getMyPreferenceBoolean(c, "autoscrollUseDefaultTime", false)) {
            StaticVariables.autoScrollDelay = 0;
        } else {
            try {
                StaticVariables.autoScrollDelay = Integer.parseInt(StaticVariables.mPreDelay.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                StaticVariables.autoScrollDelay = 0;
            }
        }

        return (StaticVariables.autoScrollDuration > 0 && StaticVariables.autoScrollDelay >= 0) ||
                StaticVariables.usingdefaults;
    }

    String determineLineTypes(String string, Context c) {
        String type;
        if (string.indexOf(".") == 0) {
            type = "chord";
        } else if (string.indexOf(";__" + c.getResources().getString(R.string.edit_song_capo)) == 0) {
            type = "capoinfo";
        } else if (string.indexOf(";__") == 0) {
            type = "extra";
            //} else if (string.startsWith(";"+c.getString(R.string.music_score))) {
            //    type = "abcnotation";
        } else if (string.startsWith(";") && string.length() > 4 && (string.indexOf("|") == 2 || string.indexOf("|") == 3)) {
            // Used to do this by identifying type of string start or drum start
            // Now just look for ;*| or ;**| where * is anything such as ;e | or ;BD|
            type = "tab";
        } else if (string.startsWith(";") && string.contains("1") && string.contains("+") && string.contains("2")) {
            // Drum tab count line
            type = "tab";
        } else if (string.startsWith(";")) {
            type = "comment";
        } else if (string.startsWith("[")) {
            type = "heading";
        } else {
            type = "lyric";
        }
        return type;
    }

    String[] getChordPositions(String string, String lyric) {
        // IV - Lyric is now needed. Part of preventing lyrics starting too close after a chords above a run of spaces
        // Given a chord line, get the character positions that each chord starts at
        // Go through the line character by character
        // If the character isn't a " " and the character before is " " or "|" it's a new chord
        // Add the positions to an array
        ArrayList<String> chordpositions = new ArrayList<>();
        String inString = string;
        boolean thischarempty;
        boolean prevcharempty;

        if (inString.startsWith(".")) { inString = inString.replaceFirst("."," "); }
        // In order to identify chords at the end of the line
        // (My method needs a following space)
        // Add a space to the search string.
        inString = inString + " ";

        for (int x = 1; x < (inString.length()); x++) {
            thischarempty =  inString.startsWith(" ", x);
            prevcharempty =  inString.startsWith(" ", x - 1);

            // Add the start of chord and the end of a chord where it ends above a space in the lyric
            if ((!thischarempty && prevcharempty) || (thischarempty && !prevcharempty && lyric.startsWith(" ", x - 1))) {
                    chordpositions.add(x + "");
            }

        }

        String[] chordpos = new String[chordpositions.size()];
        chordpos = chordpositions.toArray(chordpos);
        return chordpos;
    }

    String[] getChordSections(String string, String[] pos_string) {
        // Go through the chord positions and extract the substrings
        ArrayList<String> chordsections = new ArrayList<>();
        int startpos = 0;
        int endpos = -1;

        if (string == null) {
            string = "";
        }
        if (pos_string == null) {
            pos_string = new String[0];
        }

        for (int x = 0; x < pos_string.length; x++) {
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
        if (startpos == 0 && endpos == -1) {
            // This is just a chord line, so add the whole line
            chordsections.add(string);
        }
        String[] sections = new String[chordsections.size()];
        sections = chordsections.toArray(sections);

        return sections;
    }

    String[] getLyricSections(String string, String[] pos_string) {
        // Go through the chord positions and extract the substrings
        ArrayList<String> lyricsections = new ArrayList<>();
        int startpos = 0;
        int endpos = -1;

        if (string == null) {
            string = "";
        }
        if (pos_string == null) {
            pos_string = new String[0];
        }

        for (int x = 0; x < pos_string.length; x++) {
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

        if (startpos == 0 && endpos < 0) {
            // Just add the line
            lyricsections.add(string);
        }

        String[] sections = new String[lyricsections.size()];
        sections = lyricsections.toArray(sections);

        return sections;
    }

    private TableLayout.LayoutParams tablelayout_params() {
        return new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
    }

    private TableRow.LayoutParams tablerow_params() {
        return new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams linearlayout_params() {
        if (FullscreenActivity.scalingfiguredout) {
            return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        } else {
            return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private TableRow capolinetoTableRow(Context c, Preferences preferences, int lyricsCapoColor,
                                        String[] chords, float fontsize) {
        Transpose transpose = new Transpose();
        TableRow caporow = new TableRow(c);
        caporow.setClipChildren(false);
        caporow.setClipToPadding(false);
        caporow.setPadding(0, 0, 0, 0);
        int trimval = (int) (fontsize * preferences.getMyPreferenceFloat(c, "scaleChords", 1.0f) * preferences.getMyPreferenceFloat(c, "lineSpacing", 0.1f));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && preferences.getMyPreferenceBoolean(c, "trimLines", false)) {
            caporow.setPadding(0, -trimval, 0, -trimval);
            caporow.setGravity(Gravity.CENTER_VERTICAL);
        }

        for (String bit : chords) {
            if (bit.indexOf(".") == 0 && bit.length() > 1) {
                bit = bit.substring(1);
            }
            TextView capobit = new TextView(c);
            capobit.setLayoutParams(tablerow_params());
            StaticVariables.temptranspChords = bit;
            try {
                transpose.capoTranspose(c, preferences);
            } catch (Exception e) {
                e.printStackTrace();
            }
            capobit.setText(StaticVariables.temptranspChords);
            capobit.setTextSize(fontsize * preferences.getMyPreferenceFloat(c, "scaleChords", 1.0f));
            capobit.setTextColor(lyricsCapoColor);
            capobit.setTypeface(StaticVariables.typefaceChords);
            if (preferences.getMyPreferenceBoolean(c, "displayBoldChordsHeadings", false)) {
                capobit.setPaintFlags(capobit.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            }

            if (preferences.getMyPreferenceBoolean(c, "trimLines", false)) {
                capobit.setSingleLine();
                capobit.setIncludeFontPadding(false);
                capobit.setGravity(Gravity.CENTER_VERTICAL);
                capobit.setPadding(0, -trimval, 0, -trimval);
                capobit.setLineSpacing(0f, 0f);
            }
            caporow.addView(capobit);
        }
        return caporow;
    }

    private TableRow chordlinetoTableRow(Context c, Preferences preferences, int lyricsChordsColor,
                                         String[] chords, float fontsize) {
        TableRow chordrow = new TableRow(c);

        int presoLyricsAlign = preferences.getMyPreferenceInt(c, "presoLyricsAlign", Gravity.CENTER);

        if (StaticVariables.whichMode.equals("Presentation")) {
            TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams();
            layoutParams.width = TableLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = presoLyricsAlign;
        } else {
            chordrow.setLayoutParams(tablelayout_params());
        }

        chordrow.setPadding(0, 0, 0, 0);
        int trimval = (int) (fontsize * preferences.getMyPreferenceFloat(c, "scaleChords", 1.0f) * preferences.getMyPreferenceFloat(c, "lineSpacing", 0.1f));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && preferences.getMyPreferenceBoolean(c, "trimLines", false)) {
            chordrow.setPadding(0, -trimval, 0, -trimval);
            chordrow.setGravity(Gravity.CENTER_VERTICAL);
        }
        chordrow.setClipChildren(false);
        chordrow.setClipToPadding(false);

        for (String bit : chords) {
            if (bit.indexOf(".") == 0 && bit.length() > 1) {
                bit = bit.substring(1);
            }
            TextView chordbit = new TextView(c);
            chordbit.setText(bit);
            chordbit.setTextSize(fontsize * preferences.getMyPreferenceFloat(c, "scaleChords", 1.0f));
            chordbit.setTextColor(lyricsChordsColor);
            chordbit.setTypeface(StaticVariables.typefaceChords);

            if (StaticVariables.whichMode.equals("Presentation")) {
                if (!preferences.getMyPreferenceBoolean(c, "presoAutoScale", true) &&
                        !preferences.getMyPreferenceBoolean(c, "presoShowChords", false)) {
                    chordbit.setSingleLine(true);
                    chordbit.setTextSize(preferences.getMyPreferenceFloat(c, "fontSizePreso", 14.0f));
                } else if (!preferences.getMyPreferenceBoolean(c, "presoAutoScale", true) &&
                        preferences.getMyPreferenceBoolean(c, "presoShowChords", false)) {
                    chordbit.setSingleLine(true);
                    chordbit.setTextSize(preferences.getMyPreferenceFloat(c, "fontSizePreso", 14.0f));
                } else {
                    chordbit.setSingleLine(true);
                }
            }


            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && preferences.getMyPreferenceBoolean(c, "trimLines", false)) {
                //chordbit.setSingleLine();
                chordbit.setIncludeFontPadding(false);
                chordbit.setGravity(Gravity.CENTER_VERTICAL);
                chordbit.setPadding(0, -trimval, 0, -trimval);
                chordbit.setLineSpacing(0f, 0f);
            }
            chordrow.addView(chordbit);
        }
        return chordrow;
    }

    private TableRow lyriclinetoTableRow(Context c, int lyricsTextColor, int presoFontColor,
                                         String[] lyrics, float fontsize,
                                         StorageAccess storageAccess, Preferences preferences, Boolean presentation) {

        TableRow lyricrow = new TableRow(c);
        int presoLyricsAlign = preferences.getMyPreferenceInt(c, "presoLyricsAlign", Gravity.CENTER);
        int presotextAlign = presotextAlignFromGravity(presoLyricsAlign);
        if (StaticVariables.whichMode.equals("Presentation") && FullscreenActivity.scalingfiguredout) {
            TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams();
            layoutParams.width = TableLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = TableLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = presoLyricsAlign;
            lyricrow.setLayoutParams(layoutParams);
        } else {
            lyricrow.setLayoutParams(tablelayout_params());
        }

        int trimval = (int) (fontsize * preferences.getMyPreferenceFloat(c, "lineSpacing", 0.1f));
        lyricrow.setPadding(0, 0, 0, 0);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && preferences.getMyPreferenceBoolean(c, "trimLines", false)) {
            lyricrow.setPadding(0, -trimval, 0, -trimval);
            lyricrow.setGravity(Gravity.CENTER_VERTICAL);
        }

        // set different layoutparams and set gravity
        lyricrow.setClipChildren(false);
        lyricrow.setClipToPadding(false);

        // IV - Used when a lyricsOnly song is processed
        boolean lyricsOnly = false;
        // IV - Used to process a request to bold a line
        boolean fakeBold = false;

        for (String bit : lyrics) {
            String imagetext;
            if ((bit.toLowerCase(Locale.ROOT).endsWith(".png") || bit.toLowerCase(Locale.ROOT).endsWith(".jpg") ||
                    bit.toLowerCase(Locale.ROOT).endsWith(".gif")) ||
                    (bit.toLowerCase(Locale.ROOT).contains("content://") || bit.toLowerCase(Locale.ROOT).contains("file://"))) {
                FullscreenActivity.isImageSection = true;
                imagetext = bit.trim();
            } else {
                imagetext = "";
            }

            // IV - ' B_' at a line start is the marker for Bold of line
            if (bit.startsWith("B_")) {
                fakeBold = true;
                bit = bit.replace("B_", "");
            }

            if (!StaticVariables.whichSongFolder.contains(c.getResources().getString(R.string.image))) {
                // IV - If lyric line only, assemble and do the line in one go
                if ((presentation && !preferences.getMyPreferenceBoolean(c, "presoShowChords", true)) || (!presentation && !preferences.getMyPreferenceBoolean(c, "displayChords", true))) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(lyrics[0]);
                    for (int i = 1; i < lyrics.length; i++) {
                        sb.append(lyrics[i]);
                    }
                    bit = sb.toString();
                    if (!StaticVariables.whichMode.equals("Performance")) {
                        // IV - Remove (....) comments when presenting lyrics only
                        bit = bit.replaceAll("\\(.*?\\)", "");
                    }
                    // IV -   Remove any bold marker, typical word splits, white space and then trim - beautify!
                    bit = bit.replace("B_", "").replaceAll("_", "").replaceAll("\\s+-\\s+", "").replaceAll("\\s{2,}", " ").trim();
                    // IV - 2 spaces added to reduce occurance of right edge overrun
                    if (StaticVariables.whichMode.equals("Performance")) {
                        bit = bit + "  ";
                    } else {
                        // And before so that block text shadow has spaces on both sides
                        bit = "  " + bit + "  ";
                    }
                    // IV - flag used to break loop
                    lyricsOnly = true;
                } else {
                    bit = bit.replace("_", " ");
                }
            }

            TextView lyricbit = new TextView(c);

            if (StaticVariables.whichMode.equals("Presentation") && FullscreenActivity.scalingfiguredout &&
                    !preferences.getMyPreferenceBoolean(c, "presoShowChords", false)) {
                lyricbit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                lyricbit.setGravity(presoLyricsAlign);
            } else {
                lyricbit.setLayoutParams(tablerow_params());
            }
            // IV - Only use if the bit is not 'empty'.  This means the chord line spacing is used
            if (!bit.replace(" ", "").isEmpty()) {
                lyricbit.setText(bit);
            }

            lyricbit.setTextSize(fontsize);
            if (StaticVariables.whichMode.equals("Presentation")) {
                lyricbit.setTextColor(presoFontColor);
                if (preferences.getMyPreferenceBoolean(c, "presoLyricsBold", false) || fakeBold) {
                    lyricbit.setPaintFlags(lyricbit.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                }
                lyricbit.setTypeface(StaticVariables.typefacePreso);

                int w = StaticVariables.cast_availableWidth_1col;
                // If we have turned off autoscale and aren't showing the chords, allow wrapping
                if (!preferences.getMyPreferenceBoolean(c, "presoAutoScale", true) &&
                        !preferences.getMyPreferenceBoolean(c, "presoShowChords", false) && w > 0) {
                    TableRow.LayoutParams tllp = new TableRow.LayoutParams(w, TableRow.LayoutParams.WRAP_CONTENT);
                    lyricbit.setLayoutParams(tllp);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        lyricbit.setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED);
                    }
                    lyricbit.setSingleLine(false);
                    lyricbit.setGravity(presoLyricsAlign);
                    lyricbit.setTextAlignment(presotextAlign);
                    lyricbit.setTextSize(preferences.getMyPreferenceFloat(c, "fontSizePreso", 14.0f));
                } else if (!preferences.getMyPreferenceBoolean(c, "presoAutoScale", true) &&
                        preferences.getMyPreferenceBoolean(c, "presoShowChords", false)){
                    TableRow.LayoutParams tllp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                    lyricbit.setLayoutParams(tllp);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        lyricbit.setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED);
                    }
                    lyricbit.setSingleLine(true);
                    lyricbit.setTextAlignment(presotextAlign);
                    lyricbit.setTextSize(preferences.getMyPreferenceFloat(c, "fontSizePreso", 14.0f));
                } else {
                    lyricbit.setSingleLine(true);
                }

            } else {
                lyricbit.setTextColor(lyricsTextColor);
                // IV - Support bold lyrics when presentation
                if ((presentation && preferences.getMyPreferenceBoolean(c, "presoLyricsBold", false)) || fakeBold) {
                    lyricbit.setPaintFlags(lyricbit.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                }
                lyricbit.setTypeface(StaticVariables.typefaceLyrics);
            }

            if (FullscreenActivity.isImageSection) {
                FullscreenActivity.isImageSection = false;
                ImageView img = new ImageView(c);

                // By default, the image should be the not found one
                Drawable drw = ResourcesCompat.getDrawable(c.getResources(),R.drawable.notfound,null);

                int maxwidth = 320;
                if (FullscreenActivity.myWidthAvail > 0) {
                    maxwidth = (int) (0.25f * (float) FullscreenActivity.myWidthAvail);
                }

                img.setMaxWidth(maxwidth);
                img.setMaxHeight(maxwidth);

                Uri uri = Uri.parse(imagetext);
                InputStream inputStream = storageAccess.getInputStream(c, uri);
                if (inputStream != null) {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;

                        BitmapFactory.decodeStream(inputStream, null, options);
                        //Returns null, sizes are in the options variable
                        int width = options.outWidth;
                        int height = options.outHeight;

                        if (width == 0) {
                            width = maxwidth;
                        }
                        if (height == 0) {
                            // Assume a 4:3
                            height = (int) (((float) width / 4.0f) * 3.0f);
                        }

                        int thumbheight = (int) ((float) height * ((float) maxwidth / (float) width));

                        inputStream = storageAccess.getInputStream(c, uri);
                        Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(inputStream), maxwidth, thumbheight);
                        Resources res = c.getResources();
                        BitmapDrawable bd = new BitmapDrawable(res, ThumbImage);
                        if (ThumbImage != null) {
                            img.setLayoutParams(new TableRow.LayoutParams(ThumbImage.getWidth(), ThumbImage.getHeight()));
                        }
                        img.setImageDrawable(bd);

                    } catch (Exception e1) {
                        // Didn't work
                        e1.printStackTrace();
                        img.setImageDrawable(drw);
                    } catch (OutOfMemoryError e2) {
                        e2.printStackTrace();
                    }
                } else {
                    img.setImageDrawable(drw);
                }
                lyricrow.addView(img);
            } else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && preferences.getMyPreferenceBoolean(c, "trimLines", false)) {
                    lyricbit.setIncludeFontPadding(false);
                    lyricbit.setGravity(Gravity.CENTER_VERTICAL);
                    lyricbit.setPadding(0, -trimval, 0, -trimval);
                    lyricbit.setLineSpacing(0f, 0f);
                }
                lyricrow.addView(lyricbit);
            }
            // IV quick exit after doing a lyrics only line in one go above
            if (lyricsOnly) {
                break;
            }
        }
        return lyricrow;
    }

    private TableRow commentlinetoTableRow(Context c, Preferences preferences,
                                           int presoFontColor, int lyricsTextColor,
                                           String[] comment, float fontsize, boolean tab) {
        TableRow commentrow = new TableRow(c);
        commentrow.setLayoutParams(tablelayout_params());

        int trimval = (int) (fontsize * preferences.getMyPreferenceFloat(c, "lineSpacing", 0.1f));
        commentrow.setPadding(0, 0, 0, 0);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && preferences.getMyPreferenceBoolean(c, "trimLines", false)) {
            commentrow.setPadding(0, -trimval, 0, -trimval);
            commentrow.setGravity(Gravity.CENTER_VERTICAL);
        }


        commentrow.setClipChildren(false);
        commentrow.setClipToPadding(false);

        for (String bit : comment) {
            /*if (bit.startsWith(" ") && bit.length() > 1) {
                bit = bit.substring(1);
            }*/
            if (bit.startsWith("__")) {
                bit = bit.replace("__", "");
            }
            if (!StaticVariables.whichSongFolder.contains(c.getResources().getString(R.string.image))) {
                if (StaticVariables.whichMode.equals("Presentation") && !preferences.getMyPreferenceBoolean(c, "presoShowChords", false)) {
                    bit = bit.replace("_", "");
                } else if ((StaticVariables.whichMode.equals("Stage") || StaticVariables.whichMode.equals("Performance")) &&
                        !preferences.getMyPreferenceBoolean(c, "displayChords", true)) {
                    bit = bit.replace("_", "");
                } else {
                    bit = bit.replace("_", " ");
                }
            }

            TextView lyricbit = new TextView(c);

            lyricbit.setLayoutParams(tablerow_params());
            lyricbit.setText(bit);
            lyricbit.setTextSize(fontsize * preferences.getMyPreferenceFloat(c, "scaleComments", 0.8f));
            if (StaticVariables.whichMode.equals("Presentation")) {
                lyricbit.setTextColor(presoFontColor);
                lyricbit.setTypeface(StaticVariables.typefacePreso);
                if (preferences.getMyPreferenceBoolean(c, "presoLyricsBold", false)) {
                    lyricbit.setPaintFlags(lyricbit.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                }

            } else {
                lyricbit.setTextColor(lyricsTextColor);
                lyricbit.setTypeface(StaticVariables.typefaceLyrics);

            }
            if (tab) {
                // Set the comment text as monospaced to make it fit
                lyricbit.setTypeface(StaticVariables.typefaceMono);
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && preferences.getMyPreferenceBoolean(c, "trimLines", false)) {
                lyricbit.setIncludeFontPadding(false);
                lyricbit.setGravity(Gravity.CENTER_VERTICAL);
                lyricbit.setPadding(0, -trimval, 0, -trimval);
                lyricbit.setLineSpacing(0f, 0f);
            }
            commentrow.addView(lyricbit);
        }
        return commentrow;
    }

    private TextView titletoTextView(Context c, Preferences preferences, int lyricsTextColor, String title, float fontsize) {
        TextView titleview = new TextView(c);
        titleview.setLayoutParams(linearlayout_params());
        // IV - Trim
        titleview.setText(title.trim());
        titleview.setTextColor(lyricsTextColor);
        titleview.setTypeface(StaticVariables.typefaceLyrics);
        titleview.setTextSize(fontsize * preferences.getMyPreferenceFloat(c, "scaleHeadings", 0.6f));
        if (preferences.getMyPreferenceBoolean(c, "displayBoldChordsHeadings", false)) {
            titleview.setPaintFlags(titleview.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
        } else {
            titleview.setPaintFlags(titleview.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
        return titleview;
    }


    int getSectionColors(String type, int lyricsVerseColor, int lyricsChorusColor, int lyricsPreChorusColor,
                         int lyricsBridgeColor, int lyricsTagColor, int lyricsCommentColor, int lyricsCustomColor) {
        int colortouse;
        switch (type) {
            case "verse":
                colortouse = lyricsVerseColor;
                break;
            case "chorus":
                colortouse = lyricsChorusColor;
                break;
            case "prechorus":
                colortouse = lyricsPreChorusColor;
                break;
            case "bridge":
                colortouse = lyricsBridgeColor;
                break;
            case "tag":
                colortouse = lyricsTagColor;
                break;
            case "comment":
                colortouse = lyricsCommentColor;
                break;
            default:
                colortouse = lyricsCustomColor;
                break;
        }
        return colortouse;
    }

    String fixMultiLineFormat(Context c, Preferences preferences, String string) {
        if (!preferences.getMyPreferenceBoolean(c, "multiLineVerseKeepCompact", false)) {
            // Best way to determine if the song is in multiline format is
            // Look for [v] or [c] case insensitive
            // And it needs to be followed by a line starting with 1 and 2
            try {
                String[] sl = string.split("\n");
                boolean has_multiline_vtag = false;
                boolean has_multiline_ctag = false;
                boolean has_multiline_1tag = false;
                boolean has_multiline_2tag = false;

                for (String l : sl) {
                    if (l.toLowerCase(StaticVariables.locale).startsWith("[v]")) {
                        has_multiline_vtag = true;
                    } else if (l.toLowerCase(StaticVariables.locale).startsWith("[c]")) {
                        has_multiline_ctag = true;
                    } else if (l.toLowerCase(StaticVariables.locale).startsWith("1") ||
                            l.toLowerCase(StaticVariables.locale).startsWith(" 1")) {
                        has_multiline_1tag = true;
                    } else if (l.toLowerCase(StaticVariables.locale).startsWith("2") ||
                            l.toLowerCase(StaticVariables.locale).startsWith(" 2")) {
                        has_multiline_2tag = true;
                    }
                }

                if ((has_multiline_vtag || has_multiline_ctag) && has_multiline_1tag && has_multiline_2tag) {

                    // Reset the available song sections
                    // Ok the song is in the multiline format
                    // [V]
                    // .G     C
                    // 1Verse 1
                    // 2Verse 2

                    // Create empty verse and chorus strings up to 9 verses/choruses
                    String[] verse = {"", "", "", "", "", "", "", "", ""};
                    String[] chorus = {"", "", "", "", "", "", "", "", ""};

                    StringBuilder versechords = new StringBuilder();
                    StringBuilder choruschords = new StringBuilder();

                    // Split the string into separate lines
                    String[] lines = string.split("\n");

                    // Go through the lines and look for tags and line numbers
                    boolean gettingverse = false;
                    boolean gettingchorus = false;
                    for (int z = 0; z < lines.length; z++) {
                        String l = lines[z];
                        String l_1 = "";
                        String l_2 = "";

                        if (lines.length > z + 1) {
                            l_1 = lines[z + 1];
                        }
                        if (lines.length > z + 2) {
                            l_2 = lines[z + 2];
                        }

                        boolean mlv = isMultiLine(l, l_1, l_2, "v");
                        boolean mlc = isMultiLine(l, l_1, l_2, "c");

                        if (mlv) {
                            lines[z] = "__VERSEMULTILINE__";
                            gettingverse = true;
                            gettingchorus = false;
                        } else if (mlc) {
                            lines[z] = "__CHORUSMULTILINE__";
                            gettingverse = false;
                            gettingchorus = true;
                        } else if (l.startsWith("[")) {
                            gettingverse = false;
                            gettingchorus = false;
                        }

                        if (gettingverse) {
                            if (lines[z].startsWith(".")) {
                                versechords.append(lines[z]).append("\n");
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
                                choruschords.append(lines[z]).append("\n");
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
                    String versereplacement = addchordstomultiline(verse, versechords.toString());
                    String chorusreplacement = addchordstomultiline(chorus, choruschords.toString());

                    // Now go back through the lines and extract the new improved version
                    StringBuilder improvedlyrics = new StringBuilder();
                    for (String thisline : lines) {
                        if (thisline.equals("__VERSEMULTILINE__")) {
                            thisline = versereplacement;
                        } else if (thisline.equals("__CHORUSMULTILINE__")) {
                            thisline = chorusreplacement;
                        }
                        if (!thisline.equals("__REMOVED__")) {
                            improvedlyrics.append(thisline).append("\n");
                        }
                    }

                    return improvedlyrics.toString();
                } else {
                    // Not multiline format
                    return string;
                }
            } catch (Exception e) {
                return string;
            }
        } else {
            return string;
        }
    }

    private boolean isMultiLine(String l, String l_1, String l_2, String type) {
        boolean isit = false;
        l = l.toLowerCase(StaticVariables.locale);

        if (l.startsWith("[" + type + "]") &&
                (l_1.startsWith("1") || l_1.startsWith(" 1") || l_2.startsWith("1") || l_2.startsWith(" 1"))) {
            isit = true;
        }
        return isit;
    }

    private String addchordstomultiline(String[] multiline, String chords) {
        String[] chordlines = chords.split("\n");
        StringBuilder replacementtext = new StringBuilder();

        // Go through each verse/chorus in turn
        for (String sections : multiline) {
            String[] section = sections.split("\n");

            if (section.length == chordlines.length + 1) {
                replacementtext.append(section[0]).append("\n");
                // Only works if there are the same number of lyric lines as chords!
                for (int x = 0; x < chordlines.length; x++) {
                    replacementtext.append(chordlines[x]).append("\n").append(section[x + 1]).append("\n");
                }
                replacementtext.append("\n");
            } else {
                replacementtext.append(sections).append("\n");
            }
        }
        return replacementtext.toString();
    }

    String[] removeTagLines(String[] sections) {
        for (int x = 0; x < sections.length; x++) {
            int start = sections[x].indexOf("[");
            int end = sections[x].indexOf("]");
            if (end > start && start > -1) {
                String remove1 = sections[x].substring(start, end + 1) + "\n";
                String remove2 = sections[x].substring(start, end + 1);
                sections[x] = sections[x].replace(remove1, "");
                sections[x] = sections[x].replace(remove2, "");
            }
        }
        return sections;
    }

    String removeChordLines(String song) {
        // Split the song into separate lines
        String[] lines = song.split("\n");
        StringBuilder newsong = new StringBuilder();

        for (String thisline : lines) {
            if (!thisline.startsWith(".")) {
                newsong.append(thisline).append("\n");
            }
        }
        return newsong.toString();
    }

    String getAllChords(String song) {
        // Split the song into separate lines
        String[] lines = song.split("\n");
        StringBuilder chordsonly = new StringBuilder();

        for (String thisline : lines) {
            if (thisline.startsWith(".")) {
                chordsonly.append(thisline).append(" ");
            }
        }
        return chordsonly.toString().replace(".", " ");
    }

    String removeCommentLines(String song) {
        // Split the song into separate lines
        String[] lines = song.split("\n");
        StringBuilder newsong = new StringBuilder();

        for (String thisline : lines) {
            if (!thisline.startsWith(";")) {
                newsong.append(thisline).append("\n");
            }
        }
        return newsong.toString();

    }

    String[] splitSongIntoSections(Context c, Preferences preferences, String song) {

        song = song.replace("-!!", "");

        // IV - Protect any chord repeat bar lines
        song = song.replace("||:","··>").replace(":||","<··");

        if (StaticVariables.whichMode.equals("Presentation") || StaticVariables.whichMode.equals("Stage")) {
            // IV - Song block dividers ||| are not processed as splits
            song = song.replace("|||","|").replace("||", "%%LATERSPLITHERE%%");
        } else {
            song = song.replace("||", "");
        }

        // Need to go back to chord lines that might have to have %%LATERSPLITHERE%% added
        if (!StaticVariables.whichSongFolder.contains(c.getResources().getString(R.string.scripture))) {
            song = song.replace("\n\n", "%%__SPLITHERE__%%");
        }

        String[] temp = song.split("\n");
        StringBuilder songBuilder = new StringBuilder();
        for (String t : temp) {
            if (!t.startsWith(";") && !t.startsWith(".")) {
                if (t.trim().startsWith("---")) {
                    t = t.replace(" ---", "[]");
                    t = t.replace("---", "[]");
                }
            }

            if (t.startsWith(".") || t.startsWith(";")) {
                songBuilder.append(t).append("\n");
            } else {
                if (StaticVariables.whichMode.equals("Presentation") && !preferences.getMyPreferenceBoolean(c, "presoShowChords", false)) {
                    songBuilder.append(t.replace("|", "\n")).append("\n");
                } else if (StaticVariables.whichMode.equals("Presentation") && preferences.getMyPreferenceBoolean(c, "presoShowChords", false)) {
                    songBuilder.append(t.replace("|", " ")).append("\n");
                } else {
                    songBuilder.append(t).append("\n");
                }
            }
        }
        song = songBuilder.toString();

        if (StaticVariables.whichMode.equals("Presentation") && preferences.getMyPreferenceBoolean(c, "presoShowChords", false)) {
            // Split into lines
            StringBuilder songBuilder1 = new StringBuilder();
            for (String t : temp) {
                if (t.startsWith(".") || t.startsWith(";")) {
                    songBuilder1.append(t).append("\n");
                } else {
                    songBuilder1.append(t.replace("|", "\n")).append("\n");
                }
            }
            song = songBuilder1.toString();
        } else {
            // Split into lines
            temp = song.split("\n");
            StringBuilder songBuilder1 = new StringBuilder();
            for (String t : temp) {
                if (t.startsWith(".") || t.startsWith(";"))  {
                    songBuilder1.append(t).append("\n");
                } else {
                    songBuilder1.append(t.replace("|", " ")).append("\n");
                }
            }
            song = songBuilder1.toString();

        }
        song = song.replace("\n[", "\n%%__SPLITHERE__%%\n[");

        // Get rid of double splits
        song = song.replace("%%__SPLITHERE__%%%%__SPLITHERE__%%", "%%__SPLITHERE__%%");
        song = song.replace("%%__SPLITHERE__%%\n%%__SPLITHERE__%%", "%%__SPLITHERE__%%");
        song = song.replace("%%__SPLITHERE__%%\n\n%%__SPLITHERE__%%", "%%__SPLITHERE__%%");
        song = song.replace("%%__SPLITHERE__%%\n \n%%__SPLITHERE__%%", "%%__SPLITHERE__%%");
        song = song.replace("\n%%__SPLITHERE__%%", "%%__SPLITHERE__%%");
        song = song.replace("%%__SPLITHERE__%%\n", "%%__SPLITHERE__%%");

        // Check that we don't have empty sections
        String[] check = song.split("%%__SPLITHERE__%%");
        StringBuilder newsong = new StringBuilder();
        for (String checkthis : check) {
            if (checkthis != null && !checkthis.isEmpty() && !checkthis.equals(" ")) {
                newsong.append(checkthis).append("%%__SPLITHERE__%%");
            }
        }

        // IV - Reset any chord repeat bar lines. Split the lyrics
        return newsong.toString().replace("··>","||:").replace("<··",":||").split("%%__SPLITHERE__%%");
    }

    String[] splitLaterSplits(Context c, Preferences preferences, String[] currsections) {
        ArrayList<String> newbits = new ArrayList<>();
        for (int z = 0; z < currsections.length; z++) {
            // If currsection doesn't have extra split points, add this section to the array
            if (currsections[z] != null && !currsections[z].contains("%%LATERSPLITHERE%%")) {
                newbits.add(currsections[z]);
            } else {
                String[] splitcurr;
                // If a section has LATERSPLITHERE in it, we need to fix it for the chords we need to extract the chords
                if (currsections[z] != null && currsections[z].contains("%%LATERSPLITHERE%%")) {
                    String[] tempsection = currsections[z].split("\n");
                    for (int line = 0; line < tempsection.length; line++) {
                        // Go through each line and look for %%LATERSPLITHERE%%
                        if (tempsection[line].contains("%%LATERSPLITHERE%%") && line > 0 && tempsection[line - 1].startsWith(".")) {
                            int pos = tempsection[line].indexOf("%%LATERSPLITHERE%%");
                            String grabbedchords = "%%LATERSPLITHERE%%";
                            if (pos > -1 && (pos + 2) < tempsection[line - 1].length()) {
                                grabbedchords += "." + tempsection[line - 1].substring(pos + 2) + "\n";
                                tempsection[line - 1] = tempsection[line - 1].substring(0, pos);
                            }
                            tempsection[line] = tempsection[line].replace("%%LATERSPLITHERE%%", grabbedchords);
                        }
                    }

                    // Put the section back as the tempsection
                    currsections[z] = "";
                    for (String thisline : tempsection) {
                        currsections[z] += thisline + "\n";
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
        for (int y = 0; y < newbits.size(); y++) {
            updatedSections[y] = newbits.get(y);
            if (preferences.getMyPreferenceBoolean(c, "trimSections", true)) {
                if (updatedSections[y].endsWith("\n ") && updatedSections[y].length() > 0) {
                    updatedSections[y] = updatedSections[y].substring(0, updatedSections[y].length() - 1);
                }
                updatedSections[y] = updatedSections[y].trim();
            }

        }
        return updatedSections;
    }

    String getSectionHeadings(String songsection) {
        String label = "";
        //songsection = songsection.trim();
        if (songsection.trim().startsWith("[")) {
            int startoftag = songsection.indexOf("[");
            int endoftag = songsection.indexOf("]");
            if (endoftag < startoftag) {
                endoftag = songsection.length() - 1;
            }
            if (endoftag > startoftag) {
                label = songsection.substring(startoftag + 1, endoftag);
            } else {
                songsection = songsection.replace("[", "");
                label = songsection.replace("]", "");
            }
            StaticVariables.songSection_holder = label;
        }
        if (label.equals("")) {
            // If section is just a comment line, have no label
            int lines = songsection.split("\n").length;
            if (lines < 2 || songsection.startsWith(";")) {
                label = "";
            } else {
                label = StaticVariables.songSection_holder;
            }
        }
        return label;
    }

    String[] matchPresentationOrder(Context c, Preferences preferences, String[] currentSections) {

        // mPresentation probably looks like "Intro V1 V2 C V3 C C Guitar Solo C Outro"
        // We need to identify the sections in the song that are in here
        // What if sections aren't in the song (e.g. Intro V2 and Outro)
        // The other issue is that custom tags (e.g. Guitar Solo) can have spaces in them

        StringBuilder tempPresentationOrder = new StringBuilder(StaticVariables.mPresentation + " ");

        // Get the currentSectionLabels - these will change after we reorder the song
        // IV - We look for extra information header and footer and add into presentation order to ensure display
        String[] currentSectionLabels = new String[currentSections.length];
        for (int sl = 0; sl < currentSections.length; sl++) {
            currentSectionLabels[sl] = getSectionHeadings(currentSections[sl]);
            if (currentSectionLabels[sl].equals("H__1")) { tempPresentationOrder.insert(0, "H__1 "); }
            if (currentSectionLabels[sl].equals("F__1")) { tempPresentationOrder.append("F__1 "); }

        }

        StringBuilder errors = new StringBuilder();

        // Go through each tag in the song
        for (String tag : currentSectionLabels) {
            if (tag.equals("") || tag.equals(" ")) {
                Log.d(TAG, "Empty search");
            } else if (tempPresentationOrder.toString().contains(tag)) {
                tempPresentationOrder = new StringBuilder(tempPresentationOrder.toString().replace(tag + " ", "<__" + tag + "__>"));
            } else {
                // IV - this logic avoids a trailing new line
                if (errors.length() > 0) {
                    errors.append(("\n"));
                }
                errors.append(tag).append(" - not found in presentation order");
            }
        }

        // tempPresentationOrder now looks like "Intro <__V1__>V2 <__C__><__V3__><__C__><__C__><__Guitar Solo__><__C__>Outro "
        // Assuming V2 and Outro aren't in the song anymore
        // Split the string by <__
        String[] tempPresOrderArray = tempPresentationOrder.toString().split("<__");
        // tempPresOrderArray now looks like "Intro ", "V1__>V2 ", "C__>", "V3__>", "C__>", "C__>", "Guitar Solo__>", "C__>Outro "
        // So, if entry doesn't contain __> it isn't in the song
        // Also, anything after __> isn't in the song
        for (int d = 0; d < tempPresOrderArray.length; d++) {
            if (!tempPresOrderArray[d].contains("__>")) {
                if (!tempPresOrderArray[d].equals("") && !tempPresOrderArray[d].equals(" ")) {
                    if (errors.length() > 0) {
                        errors.append(("\n"));
                    }
                    errors.append(tempPresOrderArray[d]).append(" - not found in song");
                }
                tempPresOrderArray[d] = "";
                // tempPresOrderArray now looks like "", "V1__>V2 ", "C__>", "V3__>", "C__>", "C__>", "Guitar Solo__>", "C__>Outro "
            } else {
                String goodbit = tempPresOrderArray[d].substring(0, tempPresOrderArray[d].indexOf("__>"));
                String badbit = tempPresOrderArray[d].replace(goodbit + "__>", "");
                tempPresOrderArray[d] = goodbit;
                if (!badbit.equals("") && !badbit.equals(" ")) {
                    if (errors.length() > 0) {
                        errors.append(("\n"));
                    }
                    errors.append(badbit).append(" - not found in song");
                }
                // tempPresOrderArray now looks like "", "V1", "C", "V3", "C", "C", "Guitar Solo", "C"
            }
        }

        // Go through the tempPresOrderArray and add the sections back together as a string
        StringBuilder newSongText = new StringBuilder();

        for (String aTempPresOrderArray : tempPresOrderArray) {
            if (!aTempPresOrderArray.equals("")) {
                for (int a = 0; a < currentSectionLabels.length; a++) {
                    if (currentSectionLabels[a].trim().equals(aTempPresOrderArray.trim())) {
                        newSongText.append(currentSections[a]).append("\n");
                    }
                }
            }
        }

        // Display any errors
        StaticVariables.myToastMessage = errors.toString();
        return splitSongIntoSections(c, preferences, newSongText.toString());
    }

    String getSongTitle() {
        return StaticVariables.mTitle;
    }

    String getSongAuthor() {
        return StaticVariables.mAuthor;
    }

    String getSongKey() {
        // If key is set
        String keytext = "";
        if (!StaticVariables.mKey.isEmpty()) {
            keytext = " (" + StaticVariables.mKey + ")";
        }
        return keytext;
    }

    String getCapoInfo(Context c, Preferences preferences) {
        String s = "";
        // If we are using a capo, add the capo display
        if (!StaticVariables.mCapo.equals("")) {
            int mcapo;
            try {
                mcapo = Integer.parseInt(StaticVariables.mCapo);
            } catch (Exception e) {
                mcapo = -1;
            }
            if (mcapo > 0) {
                if (preferences.getMyPreferenceBoolean(c, "capoInfoAsNumerals", false)) {
                    s = numberToNumeral(mcapo);
                } else {
                    s = "" + mcapo;
                }
            }
        }
        return s;
    }

    String getCapoNewKey() {
        String s = "";
        // If we are using a capo, add the capo display
        if (!StaticVariables.mCapo.equals("") && !StaticVariables.mKey.equals("") &&
                !FullscreenActivity.capokey.equals("")) {
            s = FullscreenActivity.capokey;
        }
        return s;
    }

    private String numberToNumeral(int num) {
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

    String songSectionChordPro(Context c, int x, boolean onsong) {
        StringBuilder chopro = new StringBuilder();
        String[] heading = beautifyHeadings(StaticVariables.songSectionsLabels[x], c);
        if (onsong) {
            // IV Done in three places to handle sections without heading
            if (heading[0].trim().equals("")) {
                chopro.append("Section:\n");
            } else {
                chopro.append(heading[0].trim()).append(":\n");
            }
        } else {
            if (heading[1].equals("chorus")) {
                chopro.append("{soc}\n");
            } else {
                if (heading[0].trim().equals("")) {
                    chopro.append("{c:Section}");
                } else {
                    chopro.append("{c:").append(heading[0].trim()).append("}\n");
                }
            }
        }

        int linenums = StaticVariables.sectionContents[x].length;

        // Go through each line and add the appropriate lyrics with chords in them
        for (int y = 0; y < linenums; y++) {
            // Go through the section a line at a time
            String nextlinetype = "";
            String previouslinetype = "";
            if (y < linenums - 1) {
                nextlinetype = StaticVariables.sectionLineTypes[x][y + 1];
            }
            if (y > 0) {
                previouslinetype = StaticVariables.sectionLineTypes[x][y - 1];
            }

            String[] positions_returned;
            String[] chords_returned;
            String[] lyrics_returned;

            String thisLine;
            String nextLine;
            thisLine = StaticVariables.sectionContents[x][y].replaceAll("\\s+$", "");

            switch (howToProcessLines(y, linenums, StaticVariables.sectionLineTypes[x][y], nextlinetype, previouslinetype)) {
                // If this is a chord line followed by a lyric line.
                case "chord_then_lyric":
                    // IV - We have a next line - now make lines the same length.
                    nextLine = StaticVariables.sectionContents[x][y + 1].replaceAll("\\s+$", "");
                    if (thisLine.length() < nextLine.length()) {
                        thisLine = fixLineLength(thisLine, nextLine.length());
                    } else {
                        nextLine = fixLineLength(nextLine, thisLine.length());
                    }

                    // IV - Chord positioning now uses the lyric line
                    positions_returned = getChordPositions(thisLine, nextLine);
                    chords_returned = getChordSections(thisLine, positions_returned);
                    lyrics_returned = getLyricSections(nextLine, positions_returned);
                    for (int w = 0; w < lyrics_returned.length; w++) {
                        String chord_to_add = "";
                        if (w < chords_returned.length) {
                            if (chords_returned[w] != null && !chords_returned[w].trim().equals("")) {
                                chord_to_add = "[" + chords_returned[w].trim() + "]";
                            }
                        }
                        chopro.append(chord_to_add).append(lyrics_returned[w]);
                    }
                    break;

                case "chord_only":
                    // Use same logic as chord_then_lyric to guarantee consistency
                    String tempString = fixLineLength("", thisLine.length());
                    // IV - Chord positioning now uses the lyric line
                    positions_returned = getChordPositions(thisLine, tempString);
                    chords_returned = getChordSections(thisLine, positions_returned);
                    lyrics_returned = getLyricSections(tempString, positions_returned);
                    for (int w = 0; w < lyrics_returned.length; w++) {
                        String chord_to_add = "";
                        if (w < chords_returned.length) {
                            if (chords_returned[w] != null && !chords_returned[w].trim().equals("")) {
                                chord_to_add = "[" + chords_returned[w].trim() + "]";
                            }
                        }
                        chopro.append(chord_to_add).append(lyrics_returned[w]);
                    }
                    break;

                case "lyric_no_chord":
                    chopro.append(thisLine.trim());

                    break;

                case "comment_no_chord":
                    chopro.append("{c:").append(thisLine.trim()).append("}");
                    break;
            }
            chopro.append("\n");
            chopro = new StringBuilder(chopro.toString().replace("][", "]  ["));
            chopro = new StringBuilder(chopro.toString().replace("\n\n", "\n"));
        }

        if (heading[1].equals("chorus")) {
            chopro.append("{eoc}\n");
        }
        chopro.append("\n");
        return chopro.toString();
    }

    String songSectionText(Context c, Preferences preferences, int x) {
        StringBuilder text = new StringBuilder();
        String[] heading = beautifyHeadings(StaticVariables.songSectionsLabels[x], c);
        if (heading[0].trim().equals("")) {
            text.append("Section:\n");
        } else {
            text.append(heading[0].trim()).append(":");
        }

        int linenums = StaticVariables.sectionContents[x].length;

        // Go through each line and add the appropriate lyrics with chords in them
        for (int y = 0; y < linenums; y++) {
            if (StaticVariables.sectionContents[x][y].length() > 1 &&
                    StaticVariables.sectionContents[x][y].startsWith("[")) {
                text.append("");
            } else if (StaticVariables.sectionContents[x][y].length() > 1 &&
                    StaticVariables.sectionContents[x][y].startsWith(" ") ||
                    StaticVariables.sectionContents[x][y].startsWith(".") ||
                    StaticVariables.sectionContents[x][y].startsWith(";")) {
                // IV - Replace leading character with space to keep correct txt alignment
                text.append(" ").append(StaticVariables.sectionContents[x][y].substring(1));
            } else {
                text.append(StaticVariables.sectionContents[x][y]);
            }
            text.append("\n");

        }
        text.append("\n");

        if (preferences.getMyPreferenceBoolean(c, "trimSections", true)) {
            text = new StringBuilder(text.toString().trim());
        }
        return text.toString();
    }

    LinearLayout songSectionView(Context c, int x, float fontsize, boolean projected,
                                 StorageAccess storageAccess, Preferences preferences,
                                 int lyricsTextColor, int lyricsBackgroundColor, int lyricsChordsColor,
                                 int lyricsCommentColor, int lyricsCustomColor,
                                 int lyricsCapoColor, int presoFontColor) {

        final LinearLayout ll = new LinearLayout(c);

        ll.setLayoutParams(linearlayout_params());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 0, 0, 0);
        ll.setClipChildren(false);
        ll.setClipToPadding(false);

        String[] returnvals = beautifyHeadings(StaticVariables.songSectionsLabels[x], c);
        // Identify the section type
        if (x < StaticVariables.songSectionsTypes.length) {
            StaticVariables.songSectionsTypes[x] = returnvals[1];
        }

        if (checkForFilter(c,preferences,ll,x)) {
            // IV - If first title is empty then do not do add to view and mark section as comment.  This helps the song details block.
            if (x == 0 & returnvals[0].equals("")) {
                returnvals[1] = "comment";
            } else {
                ll.addView(titletoTextView(c, preferences, lyricsTextColor, returnvals[0], fontsize));
            }


            int linenums = StaticVariables.sectionContents[x].length;

            String mCapo = StaticVariables.mCapo;
            if (mCapo == null || mCapo.isEmpty()) {
                mCapo = "0";
            }
            int mcapo = Integer.parseInt(mCapo);
            boolean showchords;
            if (projected) {
                showchords = preferences.getMyPreferenceBoolean(c, "presoShowChords", false);
            } else {
                showchords = preferences.getMyPreferenceBoolean(c, "displayChords", true);
            }
            boolean showcapochords = preferences.getMyPreferenceBoolean(c, "displayCapoChords", true);
            boolean shownativeandcapochords = preferences.getMyPreferenceBoolean(c, "displayCapoAndNativeChords", false);
            boolean transposablechordformat = StaticVariables.detectedChordFormat != 4 && StaticVariables.detectedChordFormat != 5;

            // Decide if capo chords are valid and should be shown
            boolean docapochords = showchords && showcapochords && mcapo > 0 && mcapo < 12 && transposablechordformat;

            // Decide if normal chords should be shown
            // They can't be shown if showchords is true but shownativeandcapochords is false;
            boolean justcapo = docapochords && !shownativeandcapochords;
            boolean donativechords = showchords && !justcapo;

            for (int y = 0; y < linenums; y++) {
                // Go through the section a line at a time
                String nextlinetype = "";
                String previouslinetype = "";
                if (y < linenums - 1) {
                    nextlinetype = StaticVariables.sectionLineTypes[x][y + 1];
                }
                if (y > 0) {
                    previouslinetype = StaticVariables.sectionLineTypes[x][y - 1];
                }

                String[] positions_returned;
                String[] chords_returned;
                String[] lyrics_returned;
                TableLayout tl = createTableLayout(c);

                // IV - Using variables to contain end trimmed lines
                String thisLine;
                String nextLine;
                // IV - 2 spaces added to reduce occurance of right edge overrun
                thisLine = StaticVariables.sectionContents[x][y].replaceAll("\\s+$", "") + "  ";

                switch (howToProcessLines(y, linenums, StaticVariables.sectionLineTypes[x][y], nextlinetype, previouslinetype)) {
                    // If this is a chord line followed by a lyric line.
                    case "chord_then_lyric":
                        // IV - We have a next line - now make lines the same length.
                        nextLine = StaticVariables.sectionContents[x][y + 1].replaceAll("\\s+$", "") + "  ";
                        if (thisLine.length() < nextLine.length()) {
                            thisLine = fixLineLength(thisLine, nextLine.length());
                        } else {
                            nextLine = fixLineLength(nextLine, thisLine.length());
                        }
                        // IV - Chord positioning now uses the lyric line
                        positions_returned = getChordPositions(thisLine, nextLine);
                        chords_returned = getChordSections(thisLine, positions_returned);
                        if (docapochords) {
                            tl.addView(capolinetoTableRow(c, preferences, lyricsCapoColor, chords_returned, fontsize));
                        }
                        if (!justcapo && donativechords) {
                            tl.addView(chordlinetoTableRow(c, preferences, lyricsChordsColor, chords_returned, fontsize));
                        }
                        if (preferences.getMyPreferenceBoolean(c, "displayLyrics", true)) {
                            // IV - Lyric processing moved here to be done only when required
                            lyrics_returned = getLyricSections(nextLine, positions_returned);
                            tl.addView(lyriclinetoTableRow(c, lyricsTextColor, presoFontColor,
                                    lyrics_returned, fontsize, storageAccess, preferences, false));
                        }
                        break;

                    case "chord_only":
                        chords_returned = new String[1];
                        chords_returned[0] = thisLine;
                        if (docapochords) {
                            tl.addView(capolinetoTableRow(c, preferences, lyricsCapoColor, chords_returned, fontsize));
                        }
                        if (!justcapo && donativechords) {
                            tl.addView(chordlinetoTableRow(c, preferences, lyricsChordsColor, chords_returned, fontsize));
                        }
                        break;

                    case "lyric_no_chord":
                        lyrics_returned = new String[1];

                        lyrics_returned[0] = thisLine;
                        if (preferences.getMyPreferenceBoolean(c, "displayLyrics", true)) {
                            tl.addView(lyriclinetoTableRow(c, lyricsTextColor, presoFontColor,
                                    lyrics_returned, fontsize, storageAccess, preferences, false));
                        }
                        break;

                    case "comment_no_chord":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine;
                        tl.addView(commentlinetoTableRow(c, preferences, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, false));
                        tl.setBackgroundColor(lyricsCommentColor);
                        break;

                    case "extra_info":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine;
                        TableRow tr = commentlinetoTableRow(c, preferences, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, false);
                        tr.setGravity(Gravity.END);
                        tl.addView(tr);
                        tl.setGravity(Gravity.END);
                        tl.setBackgroundColor(lyricsCustomColor);
                        break;

                    case "capo_info":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine;
                        TableRow trc = commentlinetoTableRow(c, preferences, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, false);
                        if (trc.getChildAt(0) != null) {
                            TextView tvcapo = (TextView) trc.getChildAt(0);
                            tvcapo.setTextColor(lyricsCapoColor);
                        }
                        trc.setGravity(Gravity.START);
                        tl.addView(trc);
                        tl.setGravity(Gravity.START);
                        tl.setBackgroundColor(lyricsBackgroundColor);
                        break;

                    case "guitar_tab":
                    case "tab":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine;
                        tl.addView(commentlinetoTableRow(c, preferences, presoFontColor, lyricsTextColor,
                                lyrics_returned, fontsize, true));
                        tl.setBackgroundColor(lyricsCommentColor);
                        break;

                /*case "abc_notation":
                    WebView wv = ProcessSong.abcnotationtoWebView(c, FullscreenActivity.mNotation);
                    if (wv!=null) {
                        tl.addView(wv);
                    }
                    break;*/
                }
                try {
                    ll.addView(tl);
                } catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                }

                // IV - 'Section space' moved within loop to support change of colour for empty line when after an extra info lines
                if (y == linenums - 1) {
                    if (preferences.getMyPreferenceBoolean(c, "addSectionSpace", true)) {
                        TextView emptyline = new TextView(c);
                        emptyline.setLayoutParams(linearlayout_params());
                        emptyline.setText(" ");
                        emptyline.setTextSize(fontsize * 0.5f);
                        if (thisLine.startsWith("__")) {
                            emptyline.setBackgroundColor(lyricsCustomColor);
                        }
                        ll.addView(emptyline);
                    }
                }
            }

        }
        return ll;
    }

    private boolean checkForFilter(Context c, Preferences preferences, LinearLayout ll, int x) {
        boolean show = true;
        if (StaticVariables.songSectionsLabels[x].startsWith("*") &&
                StaticVariables.songSectionsLabels[x].contains(":") &&
                preferences.getMyPreferenceBoolean(c,"commentFiltering",false)) {
            // Check if it should be filtered out
            boolean showOnlyFilter = preferences.getMyPreferenceBoolean(c,"commentFilterOnlyShow",false);
            String myFilter = preferences.getMyPreferenceString(c,"commentFilters","X__XX__X");
            if (!myFilter.equals("X__XX__X")) {
                String checkFilter = StaticVariables.songSectionsLabels[x].substring(1,StaticVariables.songSectionsLabels[x].indexOf(":"));
                // Sanity check
                if (checkFilter!=null && !checkFilter.isEmpty()) {
                    // Only show if not in filter
                    if ((showOnlyFilter && !myFilter.contains("X__X"+checkFilter+"X__X")) ||
                            (!showOnlyFilter && myFilter.contains("X__X"+checkFilter+"X__X"))) {
                        // User either wants to show matching filters, so hide others or
                        // Hide matching filters, so hide this
                        ll.setVisibility(View.GONE);
                        show = false;
                    }
                }
            }
        }
        return show;
    }

    LinearLayout projectedSectionView(Context c, int x, float fontsize, StorageAccess storageAccess,
                                      Preferences preferences,
                                      int lyricsTextColor, int lyricsChordsColor,
                                      int lyricsCapoColor, int presoFontColor, int presoShadowColor) {

        final LinearLayout ll = new LinearLayout(c);

        boolean showchordspreso = preferences.getMyPreferenceBoolean(c, "presoShowChords", false);
        boolean showcapochords = preferences.getMyPreferenceBoolean(c, "displayCapoChords", true);
        boolean shownativeandcapochords = preferences.getMyPreferenceBoolean(c, "displayCapoAndNativeChords", false);
        boolean transposablechordformat = StaticVariables.detectedChordFormat != 4 && StaticVariables.detectedChordFormat != 5;
        boolean stagelyricsonly = StaticVariables.whichMode.equals("Stage") && !showchordspreso;

        if (StaticVariables.whichMode.equals("Presentation") || stagelyricsonly) {
            ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            ll.setGravity(preferences.getMyPreferenceInt(c, "presoLyricsAlign", Gravity.CENTER));
        } else {
            ll.setLayoutParams(linearlayout_params());
        }
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 0, 0, 0);
        ll.setClipChildren(false);
        ll.setClipToPadding(false);

        String[] whattoprocess;
        String[] linetypes;
        int linenums;

        if (!StaticVariables.whichMode.equals("Presentation")) {
            // Identify the section type
            String[] returnvals = beautifyHeadings(StaticVariables.songSectionsLabels[x], c);
            if (x < StaticVariables.songSectionsTypes.length) {
                StaticVariables.songSectionsTypes[x] = returnvals[1];
            }
            if (!stagelyricsonly) {
                ll.addView(titletoTextView(c, preferences, lyricsTextColor, returnvals[0], fontsize));
            }
            whattoprocess = StaticVariables.sectionContents[x];
            linetypes = StaticVariables.sectionLineTypes[x];
            linenums = StaticVariables.sectionContents[x].length;

        } else {
            whattoprocess = StaticVariables.projectedContents[x];
            linetypes = StaticVariables.projectedLineTypes[x];
            linenums = whattoprocess.length;
        }

        String mCapo = StaticVariables.mCapo;
        if (mCapo == null || mCapo.isEmpty()) {
            mCapo = "0";
        }
        int mcapo = Integer.parseInt(mCapo);

        // Decide if capo chords are valid and should be shown
        boolean docapochords = showchordspreso && showcapochords && mcapo > 0 && mcapo < 12 && transposablechordformat;

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
                nextlinetype = linetypes[y + 1];
            }
            if (y > 0) {
                //previouslinetype = FullscreenActivity.sectionLineTypes[x][y - 1];
                previouslinetype = linetypes[y - 1];
            }

            String[] positions_returned;
            String[] chords_returned;
            String[] lyrics_returned;
            TableLayout tl = createTableLayout(c);

            if (StaticVariables.whichMode.equals("Presentation") || stagelyricsonly) {
                tl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }

            String what = howToProcessLines(y, linenums, linetypes[y], nextlinetype, previouslinetype);
            if (what == null) {
                what = "";
            }

            // IV - Using variables to contain end trimmed lines
            String thisLine;
            String nextLine;

            // IV - 2 spaces added to reduce occurance of right edge overrun
            thisLine = whattoprocess[y].replaceAll("\\s+$", "") + "  ";
            if (thisLine.startsWith(".")) { thisLine = thisLine.replaceFirst("."," "); }

            switch (what) {
                // If this is a chord line followed by a lyric line.

                case "chord_then_lyric":
                    // IV - We have a next line - make lines the same length.
                    nextLine = whattoprocess[y + 1].replaceAll("\\s+$", "") + "  ";
                    if (thisLine.length() < nextLine.length()) {
                        thisLine = fixLineLength(thisLine, nextLine.length());
                    } else {
                        nextLine = fixLineLength(nextLine, thisLine.length());
                    }
                    // IV - Chord positioning now uses the lyric line
                    positions_returned = getChordPositions(thisLine, nextLine);
                    chords_returned = getChordSections(thisLine, positions_returned);
                    if (docapochords) {
                        tl.addView(capolinetoTableRow(c, preferences, lyricsCapoColor, chords_returned, fontsize));
                    }
                    if (!justcapo && donativechords) {
                        tl.addView(chordlinetoTableRow(c, preferences, lyricsChordsColor, chords_returned, fontsize));
                    }
                    if (preferences.getMyPreferenceBoolean(c, "displayLyrics", true)) {
                        // IV - Lyric processing moved here to be done when required
                        lyrics_returned = getLyricSections(nextLine, positions_returned);
                        // IV - For stage lyrics only mode, ignore the lyric line if it is commented out
                        // IV - Some songs have alternatives for lines, they can be commented out
                        if (!(stagelyricsonly && (nextlinetype.equals("comment")))) {
                            tl.addView(lyriclinetoTableRow(c, lyricsTextColor, presoFontColor,
                                    lyrics_returned, fontsize, storageAccess, preferences, true));
                        }
                    }
                    break;

                case "chord_only":
                    chords_returned = new String[1];
                    chords_returned[0] = thisLine;
                    if (docapochords) {
                        tl.addView(capolinetoTableRow(c, preferences, lyricsCapoColor, chords_returned, fontsize));
                    }
                    if (!justcapo && donativechords) {
                        tl.addView(chordlinetoTableRow(c, preferences, lyricsChordsColor, chords_returned, fontsize));
                    }
                    break;

                case "lyric_no_chord":
                case "lyric":
                    lyrics_returned = new String[1];
                    lyrics_returned[0] = thisLine;
                    if (preferences.getMyPreferenceBoolean(c, "displayLyrics", true)) {
                        tl.addView(lyriclinetoTableRow(c, lyricsTextColor, presoFontColor,
                                lyrics_returned, fontsize, storageAccess, preferences, true));
                    }
                    break;

                case "comment_no_chord":
                    lyrics_returned = new String[1];
                    lyrics_returned[0] = thisLine;
                    if (!stagelyricsonly) tl.addView(commentlinetoTableRow(c, preferences, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, false));
                    break;

                case "extra_info":
                    lyrics_returned = new String[1];
                    lyrics_returned[0] = thisLine;
                    TableRow tr = commentlinetoTableRow(c, preferences, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, false);
                    tr.setGravity(Gravity.END);
                    if (!stagelyricsonly) {
                        tl.addView(tr);
                        tl.setGravity(Gravity.END);
                    }
                    break;

                case "guitar_tab":
                    lyrics_returned = new String[1];
                    lyrics_returned[0] = thisLine;
                    if (!stagelyricsonly) tl.addView(commentlinetoTableRow(c, preferences, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, true));
                    break;

            }
            if (preferences.getMyPreferenceBoolean(c, "blockShadow", false)) {
                tl.setBackgroundColor(getColorWithAlpha(presoShadowColor, preferences.getMyPreferenceFloat(c, "blockShadowAlpha", 0.7f)));
            }
            ll.addView(tl);
        }
        // IV - Add empty line only for multi section performance mode
        if (StaticVariables.whichMode.equals("Performance")) {
            TextView emptyline = new TextView(c);
            emptyline.setLayoutParams(linearlayout_params());
            emptyline.setText(" ");
            emptyline.setTextSize(fontsize * 0.5f);
            ll.addView(emptyline);
        }
        return ll;
    }

    private int presotextAlignFromGravity(int gravity) {
        int align = 1000;
        switch (gravity) {
            case Gravity.START:
                align = View.TEXT_ALIGNMENT_VIEW_START;
                break;
            case Gravity.END:
                align = TextView.TEXT_ALIGNMENT_VIEW_END;
                break;
            case Gravity.CENTER:
            case Gravity.CENTER_HORIZONTAL:
                align = TextView.TEXT_ALIGNMENT_CENTER;
                break;
        }
        return align;
    }
    public static int getColorWithAlpha(int color, float ratio) {
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(alpha, r, g, b);
    }

    LinearLayout createLinearLayout(Context c) {
        final LinearLayout ll = new LinearLayout(c);
        ll.setLayoutParams(linearlayout_params());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setClipChildren(false);
        ll.setClipToPadding(false);
        return ll;
    }

    private TableLayout createTableLayout(Context c) {
        TableLayout tl = new TableLayout(c);
        tl.setLayoutParams(linearlayout_params());
        tl.setClipChildren(false);
        tl.setClipToPadding(false);
        return tl;
    }

    Bitmap createPDFPage(Context c, Preferences preferences, StorageAccess storageAccess, int pagewidth, int pageheight, String scale) {
        String tempsongtitle = StaticVariables.songfilename.replace(".pdf", "");
        tempsongtitle = tempsongtitle.replace(".PDF", "");
        StaticVariables.mTitle = tempsongtitle;
        StaticVariables.mAuthor = "";

        // This only works for post Lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Uri uri = storageAccess.getUriForItem(c, preferences, "Songs", StaticVariables.whichSongFolder, StaticVariables.songfilename);

            // FileDescriptor for file, it allows you to close file when you are done with it
            ParcelFileDescriptor mFileDescriptor = null;
            PdfRenderer mPdfRenderer = null;
            if (uri != null) {
                try {
                    mFileDescriptor = c.getContentResolver().openFileDescriptor(uri, "r");
                    if (mFileDescriptor != null) {
                        mPdfRenderer = new PdfRenderer(mFileDescriptor);
                        FullscreenActivity.pdfPageCount = mPdfRenderer.getPageCount();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    FullscreenActivity.pdfPageCount = 0;
                }

                if (FullscreenActivity.pdfPageCurrent >= FullscreenActivity.pdfPageCount) {
                    FullscreenActivity.pdfPageCurrent = 0;
                }
            }

            // Open page 0
            PdfRenderer.Page mCurrentPage = null;
            if (mPdfRenderer != null) {
                // If we have used the move back option from a previous set item (page button, foot pedal, etc.), we should show the last page
                if (!StaticVariables.showstartofpdf) {
                    FullscreenActivity.pdfPageCurrent = FullscreenActivity.pdfPageCount - 1;
                    StaticVariables.showstartofpdf = true;
                }
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
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(0xffffffff);
            Paint paint = new Paint();
            paint.setColor(0xffffffff);
            canvas.drawRect(0F, 0F, (float) pdfwidth, (float) pdfheight, paint);
            // Be aware this pdf might have transparency.  For now, I've just set the background
            // of the image view to white.  This is fine for most PDF files.

            // Pdf page is rendered on Bitmap
            if (mCurrentPage != null) {
                try {
                    mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
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
            StaticVariables.myToastMessage = c.getResources().getString(R.string.nothighenoughapi);
            ShowToast.showToast(c);

            return null;
        }
    }

    float getScaleValue(Context c, Preferences preferences, float x, float y, float fontsize) {
        float scale;
        if (StaticVariables.thisSongScale == null) {
            StaticVariables.thisSongScale = preferences.getMyPreferenceString(c, "songAutoScale", "W");
        }
        float maxscale = preferences.getMyPreferenceFloat(c, "fontSizeMax", 50) / fontsize;
        switch (StaticVariables.thisSongScale) {
            case "Y":
                scale = Math.min(x, y);
                if (scale > maxscale) {
                    scale = maxscale;
                }
                break;
            case "W":
                scale = x;
                if (scale > maxscale) {
                    scale = maxscale;
                }
                break;
            default:
                scale = preferences.getMyPreferenceFloat(c, "fontSize", 42.0f) / fontsize;
                break;
        }
        return scale;
    }

    float getStageScaleValue(float x, float y) {
        return Math.min(x, y);
    }

    RelativeLayout preparePerformanceBoxView(Context c, Preferences preferences, int lyricsTextColor, int lyricsBackgroundColor, int padding) {
        RelativeLayout boxbit = new RelativeLayout(c);
        LinearLayout.LayoutParams llp = linearlayout_params();
        llp.setMargins(0, 0, 0, 0);
        boxbit.setLayoutParams(llp);
        boxbit.setBackgroundResource(R.drawable.lyrics_box);
        GradientDrawable drawable = (GradientDrawable) boxbit.getBackground();
        drawable.setColor(StaticVariables.transparent); // Makes the box transparent
        if (preferences.getMyPreferenceBoolean(c, "hideLyricsBox", false)) {
            drawable.setStroke(1, StaticVariables.transparent); // set stroke width and transparent stroke
        } else {
            drawable.setStroke(1, lyricsTextColor); // set stroke width and stroke color
        }
        drawable.setCornerRadius(padding);
        int linewidth = (int) (padding - ((float) padding / 6.0f)) / 2;
        boxbit.setPadding(padding - linewidth, padding - linewidth, padding - linewidth, padding - linewidth);
        return boxbit;
    }

    LinearLayout prepareProjectedBoxView(Context c, Preferences preferences, int lyricsTextColor, int lyricsBackgroundColor, int padding) {
        LinearLayout boxbit = createLinearLayout(c);
        LinearLayout.LayoutParams llp = linearlayout_params();
        llp.setMargins(0, 0, 0, 0);
        boxbit.setLayoutParams(llp);
        if (StaticVariables.whichMode.equals("Presentation") || StaticVariables.whichMode.equals("Stage")) {
            boxbit.setGravity(Gravity.CENTER_VERTICAL);
        }
        if (StaticVariables.whichMode.equals("Presentation") || (StaticVariables.whichMode.equals("Stage") && !preferences.getMyPreferenceBoolean(c, "presoShowChords", false))) {
            boxbit.setBackground(null);
            boxbit.setHorizontalGravity(preferences.getMyPreferenceInt(c, "presoLyricsAlign", Gravity.CENTER_HORIZONTAL));
            boxbit.setVerticalGravity(preferences.getMyPreferenceInt(c, "presoLyricsVAlign", Gravity.CENTER_VERTICAL));
        } else {
            boxbit.setBackgroundResource(R.drawable.lyrics_box);
            GradientDrawable drawable = (GradientDrawable) boxbit.getBackground();
            drawable.setColor(StaticVariables.transparent);  // Makes the box transparent
            if (preferences.getMyPreferenceBoolean(c, "hideLyricsBox", false)) {
                drawable.setStroke(1, StaticVariables.transparent); // set stroke width and transparent stroke
            } else {
                drawable.setStroke(1, lyricsTextColor); // set stroke width and stroke color
            }
            drawable.setCornerRadius(padding);
            boxbit.setPadding(padding, padding, padding, padding);
        }
        return boxbit;
    }

    LinearLayout prepareStageBoxView(Context c, Preferences preferences, int lyricsTextColor, int lyricsBackgroundColor, int m, int padding) {
        LinearLayout boxbit = new LinearLayout(c);
        LinearLayout.LayoutParams llp = linearlayout_params();
        llp.setMargins(m, m, m, padding);
        boxbit.setLayoutParams(llp);
        boxbit.setBackgroundResource(R.drawable.lyrics_box);
        GradientDrawable drawable = (GradientDrawable) boxbit.getBackground();
        drawable.setColor(StaticVariables.transparent);  // Makes the box transparent
        if (preferences.getMyPreferenceBoolean(c, "hideLyricsBox", false)) {
            drawable.setStroke(1, StaticVariables.transparent); // set stroke width and transparent stroke
        } else {
            drawable.setStroke(1, lyricsTextColor); // set stroke width and stroke color
        }
        drawable.setCornerRadius(padding);
        int linewidth = (int) (padding - ((float) padding / 6.0f)) / 2;
        boxbit.setPadding(padding - linewidth, padding - linewidth, padding - linewidth, padding - linewidth);
        return boxbit;
    }

    LinearLayout preparePerformanceColumnView(Context c) {
        LinearLayout column = new LinearLayout(c);
        column.setLayoutParams(linearlayout_params());
        column.setOrientation(LinearLayout.VERTICAL);
        column.setClipChildren(false);
        column.setClipToPadding(false);
        return column;
    }

    LinearLayout preparePerformanceSongBitView(Context c, boolean horizontal) {
        LinearLayout songbit = new LinearLayout(c);
        if (horizontal) {
            songbit.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            songbit.setOrientation(LinearLayout.VERTICAL);
        }
        songbit.setLayoutParams(linearlayout_params());
        songbit.setClipChildren(false);
        songbit.setClipToPadding(false);
        songbit.setFocusable(true);
        songbit.setFocusableInTouchMode(true);
        return songbit;
    }

    LinearLayout prepareStageSongBitView(Context c) {
        LinearLayout songbit = new LinearLayout(c);
        songbit.setOrientation(LinearLayout.VERTICAL);
        songbit.setLayoutParams(linearlayout_params());
        songbit.setClipChildren(false);
        songbit.setClipToPadding(false);
        return songbit;
    }

    float setScaledFontSize(int s) {
        float tempfontsize = 12.0f * StaticVariables.sectionScaleValue[s];

        int start = (int) tempfontsize;
        float end = tempfontsize - start;
        if (end < 0.5) {
            return (float) start - 0.1f;
        } else {
            return (float) start + 0.4f;
        }
    }

    float getProjectedFontSize(float scale) {
        float tempfontsize = 12.0f * scale;
        int start = (int) tempfontsize;
        float end = tempfontsize - start;
        if (end < 0.5) {
            return (float) start - 0.4f;
        } else {
            return (float) start;
        }
    }

    void addExtraInfo(Context c, StorageAccess storageAccess, Preferences preferences) {
        String nextinset = "";
        String displayNextInSet = preferences.getMyPreferenceString(c, "displayNextInSet", "B");

        if (StaticVariables.setView) {
            // Get the index in the set
            try {
                if (!StaticVariables.nextSongInSet.equals("")) {
                    StaticVariables.nextSongKeyInSet = LoadXML.grabNextSongInSetKey(c, preferences, storageAccess, StaticVariables.nextSongInSet);
                    nextinset = ";__" + c.getString(R.string.next) + ": " + StaticVariables.nextSongInSet;
                    if (!StaticVariables.nextSongKeyInSet.equals("")) {
                        nextinset = nextinset + " (" + StaticVariables.nextSongKeyInSet + ")";
                    }
                } else {
                    nextinset = ";__" + c.getResources().getString(R.string.lastsong);
                }
            } catch (Exception e) {
                Log.d(TAG, "Problem getting next song info");
            }
        }

        StringBuilder stickyNotes = new StringBuilder();
        String sad = preferences.getMyPreferenceString(c, "stickyAutoDisplay", "F");

        // Change Ts to Bs when not in Performance mode
        if (!(StaticVariables.whichMode.equals("Performance"))) {
            if (sad.equals("T")) sad = "B";
            if (displayNextInSet.equals("T")) displayNextInSet = "B";
        }

        if (((sad.equals("T") || sad.equals("B")) && !StaticVariables.mNotes.equals(""))) {
            String[] notes = StaticVariables.mNotes.split("\n");
            stickyNotes.append(";__").append(c.getString(R.string.note)).append(": ");
            for (String line : notes) {
                stickyNotes.append(";__").append(line).append("\n");
            }
        }

        // IV - New extra info section for song details - performance mode only
        // GE new variable given (set in PopUpExtraInfoFragment.java)

        StringBuilder songInformation = new StringBuilder();

        if (StaticVariables.whichMode.equals("Performance") && preferences.getMyPreferenceBoolean(c, "stickyBlockInfo", false)) {
            // IV - We handle long fields by splitting up lines.  This is based on the longest line length
            String[] lines = StaticVariables.mLyrics.split("\n");
            // We do not split 30 or less characters
            int longestLine = 30;
            for (String line : lines) {
                if (line.length() > longestLine) {
                    longestLine = line.length();
                }
            }
            // IV - Go multiline if needed, to avoid causing small text for multi-column songs
            songInformation.append(" B_").append(multiLine(StaticVariables.mTitle, longestLine).replaceAll("\n", "\n B_")).append("  \n");

            if (!StaticVariables.mAuthor.equals("")) {
                songInformation.append(";").append(multiLine(StaticVariables.mAuthor, longestLine).replaceAll("\n", "\n;")).append("  \n");
            }

            if (!StaticVariables.mCopyright.equals("")) {
                songInformation.append(";© ").append(multiLine(StaticVariables.mCopyright, longestLine).replaceAll("\n", "\n;")).append("  \n");
            }

            // IV - Try to generate a copo/key/tempo/time line
            String sprefix = ";";

            if (preferences.getMyPreferenceBoolean(c, "displayCapoChords", true)) {
                if (!StaticVariables.mCapo.equals("") && !StaticVariables.mCapo.equals("0")) {
                    // If we are using a capo, add the capo display
                    songInformation.append(sprefix).append("Capo: ");
                    sprefix = " ||| ";
                    int mcapo;
                    try {
                        mcapo = Integer.parseInt(StaticVariables.mCapo);
                    } catch (Exception e) {
                        mcapo = -1;
                    }
                    if ((mcapo > 0) && (preferences.getMyPreferenceBoolean(c, "capoInfoAsNumerals", false))) {
                        songInformation.append(numberToNumeral(mcapo));
                    } else {
                        songInformation.append("").append(mcapo);
                    }

                    Transpose transpose = new Transpose();

                    StaticVariables.temptranspChords = StaticVariables.mKey;
                    try {
                        transpose.capoTranspose(c, preferences);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!StaticVariables.temptranspChords.equals("")) {
                        songInformation.append(" (").append(StaticVariables.temptranspChords).append(")");
                    }
                }
            }

            if (!StaticVariables.mKey.equals("")) {
                songInformation.append(sprefix).append(c.getResources().getString(R.string.edit_song_key)).append(": ").append(StaticVariables.mKey);
                sprefix = " ||| ";
            }
            if (!StaticVariables.mTempo.equals("")) {
                songInformation.append(sprefix).append(c.getResources().getString(R.string.edit_song_tempo)).append(": ").append(StaticVariables.mTempo);
                sprefix = " ||| ";
            }
            if (!StaticVariables.mTimeSig.equals("")) {
                songInformation.append(sprefix).append(c.getResources().getString(R.string.edit_song_timesig)).append(": ").append(StaticVariables.mTimeSig);
                sprefix = " ||| ";
            }

            // If we have added elements finish off the line
            if (!sprefix.equals(";")) {
                songInformation.append("  \n");
            }
        }

        // If we have song details and a top sticky note, combine and empty stckyNotes variable
        if (sad.equals("T") && (songInformation.length() > 0) && (stickyNotes.length() > 0)) {
                songInformation.append(stickyNotes.toString().replace(";__" + c.getString(R.string.note) + ": " + ";__", ";" + c.getString(R.string.note) + ": ").replaceAll(";__", ";"));
                stickyNotes = new StringBuilder();
        }

        // Build an [H__1] header section
        StringBuilder headerInformation = new StringBuilder();

        // If top 'next in set'
        if (displayNextInSet.equals("T") && (nextinset.length() > 0)) {
            headerInformation.append(nextinset).append("\n");
        }

        // If top (still) 'sticky note'
        if (sad.equals("T") && (stickyNotes.length() > 0)) {
            headerInformation.append(stickyNotes.toString().replace(";__" + c.getString(R.string.note) + ": " + ";__", ";__" + c.getString(R.string.note) + ": ")).append("\n");
        }

        // If (always top) song details
        if (songInformation.length() > 0) {
            // If we have song details with information above, add a separator first
            if (headerInformation.length() > 0) {
                headerInformation = headerInformation.append(";__\n");
            }
            headerInformation.append(songInformation);
        }

        // If we have header information add as [H__1] section
        if (headerInformation.length() > 0) {
            FullscreenActivity.myLyrics = "[H__1]\n" + headerInformation.toString() + FullscreenActivity.myLyrics;
        }

        // Build an [F__1] footer section
        StringBuilder footerInformation = new StringBuilder();

        // If bottom 'sticky note'
        if (sad.equals("B") && (stickyNotes.length() > 0)) {
            footerInformation.append(stickyNotes.toString().replace(";__" + c.getString(R.string.note) + ": " + ";__", ";__" + c.getString(R.string.note) + ": "));
        }

        // If bottom 'next in set'
        if (displayNextInSet.equals("B") && (nextinset.length() > 0)) {
            footerInformation.append(nextinset).append("\n");
        }

        // If we have footer information add as [F__1] section
        if (footerInformation.length() > 0) {
            FullscreenActivity.myLyrics = FullscreenActivity.myLyrics + "[F__1]\n" + footerInformation.toString();
        }
    }

    private String multiLine(String longString, int targetLength) {
        if (longString.length() > targetLength) {
            try {
                StringBuilder outLongString = new StringBuilder();
                // IV - Work out word positions using the get chord position logic
                String[] positions = getChordPositions(longString, longString);
                int startpos = 0;
                int endpos = 0;

                for (int i = 0; i < positions.length; i++) {
                    // Split if this word starts with '(' and there is a word before
                    if ((longString.substring(Integer.parseInt(positions[i])).startsWith("(")) && (endpos > startpos) ) {
                        endpos = Integer.parseInt(positions[i]);
                        outLongString.append(longString.substring(startpos, endpos)).append("\n");
                        startpos = endpos;
                    } else {
                        // Split if this section is greater than target length
                        if (Integer.parseInt(positions[i]) > (targetLength + startpos)) {
                            // If there is no word before, split at this word (more than target length)
                            if (endpos == startpos) {
                                endpos = Integer.parseInt(positions[i]);
                            } else {
                                // Otherwise split at the previous word (target length or less)
                                endpos = Integer.parseInt(positions[i - 1]);
                                // Go back to previous word on next pass
                                i = i - 2;
                            }
                            outLongString.append(longString.substring(startpos, endpos)).append("\n");
                            startpos = endpos;
                        } else {
                            endpos = Integer.parseInt(positions[i]);
                            // A word has been considered so endpos > starpos
                        }
                    }
                }
                outLongString.append(longString.substring(startpos));
                return outLongString.toString();
            } catch (Exception e) {
                // Just in case, if there is a fail return full line
                return longString;
            }
        }
        return longString;
    }

    // The stuff for PresenterMode
    Button makePresenterSetButton(int x, Context c) {
        Button newButton = new Button(c);
        String buttonText = StaticVariables.mSetList[x];
        newButton.setText(buttonText);
        newButton.setBackgroundResource(R.drawable.present_section_setbutton);
        newButton.setTextSize(10.0f);
        newButton.setTextColor(StaticVariables.white);
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
    void highlightPresenterSetButton(Button b) {
        b.setBackgroundResource(R.drawable.present_section_setbutton_active);
        b.setTextSize(10.0f);
        b.setTextColor(StaticVariables.black);
        b.setPadding(10, 10, 10, 10);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
    }
    void unhighlightPresenterSetButton(Button b) {
        b.setBackgroundResource(R.drawable.present_section_setbutton);
        b.setTextSize(10.0f);
        b.setTextColor(StaticVariables.white);
        b.setPadding(10, 10, 10, 10);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
    }
    LinearLayout makePresenterSongButtonLayout(Context c) {
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
    TextView makePresenterSongButtonSection(Context c, String s) {
        TextView tv = new TextView(c);
        tv.setText(s);
        tv.setTextColor(StaticVariables.white);
        tv.setTextSize(10.0f);
        tv.setPadding(5, 5, 10, 5);
        return tv;
    }
    Button makePresenterSongButtonContent(Context c, String s) {
        Button b = new Button(c);
        b.setText(s.trim());
        b.setTransformationMethod(null);
        b.setBackgroundResource(R.drawable.present_section_button);
        b.setTextSize(10.0f);
        b.setTextColor(StaticVariables.white);
        b.setPadding(10, 10, 10, 10);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
        return b;
    }
    void highlightPresenterSongButton(Button b) {
        b.setBackgroundResource(R.drawable.present_section_button_active);
        b.setTextSize(10.0f);
        b.setTextColor(StaticVariables.black);
        b.setPadding(10, 10, 10, 10);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
    }
    void unhighlightPresenterSongButton(Button b) {

        b.setBackgroundResource(R.drawable.present_section_button);
        b.setTextSize(10.0f);
        b.setTextColor(StaticVariables.white);
        b.setPadding(10, 10, 10, 10);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
    }

    // The stuff for the highlighter notes
    String getHighlighterName(Context c) {
        String layout;
        String highlighterfile;

        if (StaticVariables.whichSongFolder.equals(c.getString(R.string.mainfoldername)) || StaticVariables.whichSongFolder.equals("MAIN") ||
                StaticVariables.whichSongFolder.equals("")) {
            highlighterfile = c.getString(R.string.mainfoldername) + "_" + StaticVariables.songfilename;
        } else if (StaticVariables.whichSongFolder.startsWith("../")) {
            highlighterfile = StaticVariables.whichSongFolder.replace("../","").replace("/","_") + "_" + StaticVariables.songfilename;
        } else {
            highlighterfile = StaticVariables.whichSongFolder.replace("/","_") + "_" + StaticVariables.songfilename;
        }

        if (c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layout = "_p";
        } else {
            layout = "_l";
        }

        String page = "";
        if (FullscreenActivity.isPDF) {
            // Because pdf files can have multiple pages, this allows different notes.
            page = "_" + FullscreenActivity.pdfPageCurrent;
        }
        return highlighterfile + layout + page + ".png";

    }


    // The stuff for the Nearby API connections (replaced Salut)
    public ArrayList<String> getNearbyIncoming(String incoming) {
        String[] bits = incoming.split("_xx____xx_");
        ArrayList<String> received = new ArrayList<>();
        Collections.addAll(received, bits);
        // Fix bits in the song xml
        if (received.size()>=4) {
            // 4th bit (index of 3 though!) is the xml
            String fixed = received.get(3);
            fixed = fixed.replace("\\n", "$$__$$");
            fixed = fixed.replace("\\", "");
            fixed = fixed.replace("$$__$$", "\n");
            received.add(3,fixed);
        }
        return received;
    }
    public int getNearbySection(String incoming) {
        int i=-1;
        if (incoming!=null && incoming.length()>0 && incoming.contains("___section___")) {
            incoming = incoming.replace("{\"description\":\"","");
            incoming = incoming.replace("\"}","");
            incoming = incoming.replace("___section___","");
            try {
                i = Integer.parseInt(incoming);
            } catch (Exception e) {
                i = -1;
            }
        }
        return i;
    }











    // Stuff I might eventually use...
    /*
    @SuppressWarnings("unused")
    public static WebView abcnotationtoWebView(Context c, final String s) {
        *//*
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
                        Log.d(TAG,"Error encoding");
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
        *//*
        return new WebView(c);
    }
    @SuppressWarnings("unused")
    public static String lyriclinetoHTML(String[] lyrics) {
        StringBuilder lyrichtml = new StringBuilder();
        for (String bit:lyrics) {
            lyrichtml.append("<td class=\"lyric\">").append(bit.replace(" ", "&nbsp;")).append("</td>");
        }
        return lyrichtml.toString();
    }
    @SuppressWarnings("unused")
    String songHTML (Context c, StorageAccess storageAccess, Preferences preferences,
                     int lyricsBackgroundColor, int lyricsTextColor, int lyricsChordColor) {
        return  "" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                getHTMLFontImports(c,preferences,lyricsTextColor,lyricsChordColor) +
                ".page       {background-color:" + String.format("#%06X", (StaticVariables.white & lyricsBackgroundColor)) + ";}\n" +
                ".lyrictable {border-spacing:0; border-collapse: collapse; border:0px;}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body class=\"page\"\">\n" +
                "<table id=\"mysection\">\n" +
                processHTMLLyrics(c,preferences,lyricsTextColor,lyricsChordColor) +
                "</table>\n" +
                "</body>\n" +
                "</html>";
    }
    String getHTMLFontImports(Context c, Preferences preferences,
                              int lyricsTextColor, int lyricsChordColor) {
        // This prepares the import code for the top of the html file that locates the fonts from Google
        // If they've been downloaded already, they are cached on the device, so no need to redownload.
        String base1 = "@import url('https://fonts.googleapis.com/css?family=";
        String base2 = "&swap=true');\n";
        String fontLyric = preferences.getMyPreferenceString(c,"fontLyric","Lato");
        String fontChord = preferences.getMyPreferenceString(c,"fontChord","Lato");
        String fontPreso = preferences.getMyPreferenceString(c,"fontPreso","Lato");
        String fontPresoInfo = preferences.getMyPreferenceString(c,"fontPresoInfo","Lato");
        float scaleChords = preferences.getMyPreferenceFloat(c,"scaleChords",1.0f);
        float scaleHeadings = preferences.getMyPreferenceFloat(c,"scaleHeadings",0.6f);
        String importString = base1+fontLyric+base2;
        importString += base1+fontChord+base2;
        importString += base1+fontPreso+base2;
        importString += base1+fontPresoInfo+base2;
        importString += ".lyric {font-family:"+fontLyric+"; color:" +
                String.format("#%06X", (0xFFFFFF & lyricsTextColor)) + "; " +
                "padding: 0px; text-size:12.0pt;}\n";
        importString += ".chord {font-family:"+fontChord+"; color:" +
                String.format("#%06X", (0xFFFFFF & lyricsChordColor)) + "; " +
                "padding: 0px; text-size:"+(12.0f*scaleChords)+"pt;}\n";
        importString += ".heading {font-family:"+fontLyric+"; color:" +
                String.format("#%06X", (0xFFFFFF & lyricsTextColor)) + "; " +
                "padding: 0px; text-size:"+(12.0f*scaleHeadings)+"pt; "+
                "text-decoration:underline;}\n";
        return importString;
    }
    String processHTMLLyrics(Context c, Preferences preferences,
                             int lyricsTextColor, int lyricsChordColor) {
        // This goes through the song a section at a time and prepares the table contents
        String[] lines = StaticVariables.mLyrics.split("\n");
        String previousline, previouslineType;
        String thisline, thislineType;
        String nextline, nextlineType;
        String

        StringBuilder htmltext = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                previousline = lines[i - 1];
            } else {
                previousline = "";
            }
            previouslineType = getLineType(previousline);
            thisline = lines[i];
            thislineType = getLineType(thisline);
            if (i < lines.length - 1) {
                nextline = lines[i + 1];
            } else {
                nextline = "";
            }
            nextlineType = getLineType(nextline);

            if (thislineType.equals("heading")) {
                htmltext.append(beautifyHeadings(thisline,c)[0]);
            }

        }
    }

    String getLineType(String line) {
        if (line==null) {
            return "null";
        } else if (line.startsWith("[")) {
            return "heading";
        } else if (line.startsWith(".")) {
            return "chord";
        } else if (line.startsWith(";") && line.contains(":")) {
            return "tab";
        } else if (line.startsWith(";")) {
            return "comment";
        } else {
            return "lyric";
        }
    }*/
}
