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
import android.graphics.Typeface;
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
    // IV - To improve performance relevant preferences are loaded into variables
    private String nextLine;
    private String nextLineType;
    private String previousLineType;
    private String thisLine;
    private String thisLineType;
    private String capoLine;
    private String[] chords_returned;
    private String[] lyrics_returned;
    private String[] positions_returned;
    private boolean addSectionSpace;
    private boolean blockShadow;
    private boolean displayBoldChordsHeadings;
    private boolean displayCapoAndNativeChords;
    private boolean displayCapoChords;
    private boolean displayChords;
    private boolean displayLyrics;
    private boolean hideLyricsBox;
    private boolean presoAutoScale;
    private boolean presoLyricsBold;
    private boolean presoShowChords;
    private boolean trimLines;
    private float blockShadowAlpha;
    private float fontSizePreso;
    private float lineSpacing;
    private float scaleChords;
    private float scaleComments;
    private int presoLyricsAlign;
    private Typeface typefaceLyrics;
    private Typeface typefaceChords;
    private Typeface typefacePreso;
    private Typeface typefaceMono;

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

    public static int getColorWithAlpha(int color, float ratio) {
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(alpha, r, g, b);
    }

    String parseLyrics(String myLyrics, Context c) {
        // To replace [<Verse>] with [V] and [<Verse> 1] with [V1]
        String languageverseV = c.getResources().getString(R.string.tag_verse);
        String languageverse_lowercaseV = languageverseV.toLowerCase(StaticVariables.locale);
        String languageverse_uppercaseV = languageverseV.toUpperCase(StaticVariables.locale);

        // To replace [<Chorus>] with [C] and [<Chorus> 1] with [C1]
        String languagechorusC = c.getResources().getString(R.string.tag_chorus);
        String languagechorus_lowercaseC = languagechorusC.toLowerCase(StaticVariables.locale);
        String languagechorus_uppercaseC = languagechorusC.toUpperCase(StaticVariables.locale);

        return myLyrics
                .replace("Slide 1", "[V1]")
                .replace("Slide 2", "[V2]")
                .replace("Slide 3", "[V3]")
                .replace("Slide 4", "[V4]")
                .replace("Slide 5", "[V5]")
                .replace("]\n\n", "]\n")
                .replaceAll("\r\n", "\n")
                .replaceAll("\r", "\n")
                .replaceAll("\\t", "    ")
                .replaceAll("\f", "    ")
                .replace("\r", "")
                .replace("\t", "    ")
                .replace("\b", "    ")
                .replace("\f", "    ")
                .replace("&#27;", "'")
                .replace("&#x27;", "'")
                .replace("&#027;", "'")
                .replace("&#39;", "'")
                .replace("&#34;", "'")
                .replace("&#039;", "'")
                .replace("&ndash;", "-")
                .replace("&mdash;", "-")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&rdquo;", "'")
                .replace("&rdquor;", "'")
                .replace("&rsquo;", "'")
                .replace("&amp;rsquo;", "'")
                .replace("&rdquor;", "'")
                .replaceAll("\u0092", "'")
                .replaceAll("\u0093", "'")
                .replaceAll("\u2018", "'")
                .replaceAll("\u2019", "'")
                // If UG has been bad, replace these bits:
                .replace("pre class=\"\"", "")
                // Make double tags into single ones
                .replace("[[", "[")
                .replace("]]", "]")
                // Make lowercase start tags into caps
                .replace("[v", "[V")
                .replace("[b", "[B")
                .replace("[c", "[C")
                .replace("[t", "[T")
                .replace("[p", "[P")
                // Replace [Verse] with [V] and [Verse 1] with [V1]
                .replace("[" + languageverse_lowercaseV, "[" + languageverseV)
                .replace("[" + languageverse_uppercaseV, "[" + languageverseV)
                .replace("[" + languageverseV + "]", "[V]")
                .replace("[" + languageverseV + " 1]", "[V1]")
                .replace("[" + languageverseV + " 2]", "[V2]")
                .replace("[" + languageverseV + " 3]", "[V3]")
                .replace("[" + languageverseV + " 4]", "[V4]")
                .replace("[" + languageverseV + " 5]", "[V5]")
                .replace("[" + languageverseV + " 6]", "[V6]")
                .replace("[" + languageverseV + " 7]", "[V7]")
                .replace("[" + languageverseV + " 8]", "[V8]")
                .replace("[" + languageverseV + " 9]", "[V9]")
                // Replace [<Chorus>] with [C] and [<Chorus> 1] with [C1]
                .replace("[" + languagechorus_lowercaseC, "[" + languagechorusC)
                .replace("[" + languagechorus_uppercaseC, "[" + languagechorusC)
                .replace("[" + languagechorusC + "]", "[C]")
                .replace("[" + languagechorusC + " 1]", "[C1]")
                .replace("[" + languagechorusC + " 2]", "[C2]")
                .replace("[" + languagechorusC + " 3]", "[C3]")
                .replace("[" + languagechorusC + " 4]", "[C4]")
                .replace("[" + languagechorusC + " 5]", "[C5]")
                .replace("[" + languagechorusC + " 6]", "[C6]")
                .replace("[" + languagechorusC + " 7]", "[C7]")
                .replace("[" + languagechorusC + " 8]", "[C8]")
                .replace("[" + languagechorusC + " 9]", "[C9]")
                // Try to convert ISO / Windows
                .replace("\0x91", "'")
                // Get rid of BOMs and stuff
                .replace("\uFEFF", "")
                .replace("\uFEFF", "")
                .replace("[&#x27;]", "")
                .replace("[\\xEF]", "")
                .replace("[\\xBB]", "")
                .replace("[\\xFF]", "")
                .replace("\\xEF", "")
                .replace("\\xBB", "")
                .replace("\\xFF", "");
    }

    String fixStartOfLines(String lyrics) {
        StringBuilder fixedlyrics = new StringBuilder();
        String[] lines = lyrics.split("\n");

        for (String line : lines) {
            if (line.length()==0 || !("[;. 123456789-".contains(line.substring(0,1)))) {
               line = " " + line;
            }
            fixedlyrics.append(line).append("\n");
        }
        return fixedlyrics.toString();
    }

    String fixlinebreaks(String string) {
        string = string
                .replace("\r\n", "\n")
                .replace("\n\r", "\n")
                .replace("\r", "\n")
                .replace("<br>", "\n")
                .replace("<p>", "\n\n");
        return string;
    }

    String removeUnderScores(Context c, Preferences preferences, String myLyrics) {
        // Go through the lines and remove underscores if the line isn't an image location
        // Split the lyrics into a line by line array so we can fix individual lines
        StringBuilder myLyricsBuilder = new StringBuilder();

        // IV - Remove an underscore OR replace with a space if showing chords
        String underscoreReplace = "";

        // Stage or Performance
        if ("Presentation".equals(StaticVariables.whichMode)) {
            if (preferences.getMyPreferenceBoolean(c, "presoShowChords", false))
                underscoreReplace = " ";
        } else {
            if (!preferences.getMyPreferenceBoolean(c, "displayChords", true))
                underscoreReplace = " ";
        }

        String imageHeader = "[" + c.getResources().getString(R.string.image) + "_";
        boolean thisIsImage = true;
        boolean prevIsImage;
        boolean thisandprevIsImage;

        for (String bit : myLyrics.split("\n")) {
            prevIsImage = thisIsImage;
            thisIsImage = bit.contains(imageHeader);
            thisandprevIsImage = prevIsImage && thisIsImage;
            if (bit.contains("_") &&
                    (!thisandprevIsImage) &&
                    !((bit.endsWith(".png") || bit.endsWith(".jpg") || bit.endsWith(".gif")) ||
                            (bit.contains("content://") || bit.contains("file://")))) {
                bit = bit.replace("_", underscoreReplace);
            }
            myLyricsBuilder.append(bit).append("\n");
        }
        myLyrics = myLyricsBuilder.toString();
        return myLyrics;
    }

    String removeUnwantedSymbolsAndSpaces(Context c, Preferences preferences, String string) {
        // Replace unwanted symbols
        // Split into lines
        //string = string.replace("|", "\n");
        String underscoreReplace = "";

        if ("Presentation".equals(StaticVariables.whichMode)) {
            if (preferences.getMyPreferenceBoolean(c, "presoShowChords", false))
                underscoreReplace = " ";
        } else {
            if (!preferences.getMyPreferenceBoolean(c, "displayChords", true))
                underscoreReplace = " ";
        }
        string = string.replace("_", underscoreReplace)
                .replace(",", " ")
                .replace(".", " ")
                .replace(":", " ")
                .replace(";", " ")
                .replace("!", " ")
                .replace("'", "")
                .replace("(", " ")
                .replace(")", " ")
                //string = string.replace("-", " ");
                // Now reduce any white spaces to a single space
                .replaceAll("\\s{2,}", " ");
        return string;
    }

    String[] beautifyHeadings(String heading, Context c) {
        String section = "";
        String[] vals = new String[2];
        boolean annotated = heading.contains("-]");

        if (heading.equals("")) {
            vals[0] = "";
            vals[1] = "custom";
            return vals;
        }

        String string;
        if (annotated) {
            string = heading
                    .replace("[","")
                    .replace(" -]", "")
                    .replace("-]", "")
                    .replace ("]","")
                    .trim();
        } else {
            string = heading
                    .replace("[","")
                    .replace("]", "")
                    .trim();
        }

        // Fix for filtered section labels. ':' is a 'quick exit for non filtered headings
        if (string.contains(":") &&
                (string.contains(":V") || string.contains(":C") || string.contains(":B") ||
                 string.contains(":T") || string.contains(":P"))) {
            string = string.substring(string.indexOf(":") + 1);
        }

        if (!FullscreenActivity.foundSongSections_heading.contains(string)) {
            FullscreenActivity.foundSongSections_heading.add(string);
        }

        // IV - Test 1 char or 2 chars ending 0-9 or 3 chars ending 10
        if (string.length() == 1 ||
           (string.length() == 2 && "123456789".contains(string.substring(1))) ||
           (string.length() == 3 && string.substring(1).equals("10"))) {
            switch (string.substring(0, 1)) {
                case "V":
                    string = string.replace("V", c.getResources().getString(R.string.tag_verse) + " ");
                    section = "verse";
                    break;

                case "T":
                    string = string.replace("T", c.getResources().getString(R.string.tag_tag) + " ");
                    section = "tag";
                    break;

                case "C":
                    string = string.replace("C", c.getResources().getString(R.string.tag_chorus) + " ");
                    section = "chorus";
                    break;

                case "B":
                    string = string.replace("B", c.getResources().getString(R.string.tag_bridge) + " ");
                    section = "bridge";
                    break;

                case "P":
                    string = string.replace("P", c.getResources().getString(R.string.tag_prechorus) + " ");
                    section = "prechorus";
                    break;

                case "I":
                    section = "custom";
                    break;

                default:
                    break;
            }
        }

        vals[0] = string.trim();

        if (section.equals("")) {
            vals[0] = heading
                    .replace("[","")
                    .replace("]","")
                    .trim();
            // Also look for English tags for non-English app users
            string = string.toLowerCase(StaticVariables.locale);
            if (string.contains("verse")) {
                section = "verse";
            } else if (string.contains("chorus")) {
                section = "chorus";
            } else if (string.contains("bridge")) {
                section = "bridge";
            } else if (string.contains("prechorus") || string.contains("pre-chorus")) {
                section = "prechorus";
            } else if (string.contains("tag")) {
                section = "tag";
            } else {
                section = "custom";
            }
        } else if (annotated) {
            vals[0] = "";
        }

        vals[1] = section;
        return vals;
    }

    String howToProcessLines(int linenum, int totallines, String thislinetype, String nextlinetype, String previouslinetype) {
        String what = "null";

        switch (thislinetype) {
            case "chord":
                if (linenum < totallines - 1 && (nextlinetype.equals("lyric") || nextlinetype.equals("comment"))) {
                    what = "chord_then_lyric";
                } else if (nextlinetype.equals("") || nextlinetype.equals("chord")) {
                    what = "chord_only";
                }
                break;

            case "lyric":
                if (!previouslinetype.equals("chord"))
                    what = "lyric_no_chord";
                break;

            case "comment":
                if (!previouslinetype.equals("chord"))
                    what = "comment_no_chord";
                break;

            case "capoinfo":
                what = "capo_info";
                break;

            case "extra":
                what = "extra_info";
                break;

            case "tab":
                what = "guitar_tab";
                break;

            case "heading":
                what = "heading";
                break;

        }

        return what;
    }

    void rebuildParsedLyrics(int length) {
        StringBuilder tempLyrics = new StringBuilder();
        for (int x = 0; x < length; x++) {
            // First line of section should be the label, so replace it with label.
            if (StaticVariables.songSections[x].startsWith("[" + StaticVariables.songSectionsLabels[x] + "]")) {
                tempLyrics.append(StaticVariables.songSections[x]).append("\n");
            } else if (StaticVariables.songSectionsLabels[x] != null) {
                tempLyrics.append("[").append(StaticVariables.songSectionsLabels[x]).append("]\n");
                tempLyrics.append(StaticVariables.songSections[x]).append("\n");
            }
        }
        FullscreenActivity.myParsedLyrics = tempLyrics.toString().split("\n");
    }

    void lookForSplitPoints() {
        // Determine column split points
        String[] myParsedLyrics = FullscreenActivity.myParsedLyrics;
        int halfwaypoint = Math.round((float) FullscreenActivity.numrowstowrite / 2.0f);
        int thirdwaypoint = Math.round((float) FullscreenActivity.numrowstowrite / 3.0f);
        int twothirdwaypoint = thirdwaypoint * 2;

        // In the split tests, set a not found default and then try to update to nearest split point

        // If there is only one section, the splitpoint is at the end
        if (StaticVariables.songSections.length == 1) {
            FullscreenActivity.splitpoint = FullscreenActivity.numrowstowrite;
        } else {
            // Which is the best split point closest to halfway for 2 columns
            int splitpoint_beforeHalf = 0;
            for (int scan = halfwaypoint; scan > 0; scan--) {
                if (myParsedLyrics[scan] != null &&
                        (myParsedLyrics[scan].startsWith("[") || myParsedLyrics[scan].length() == 0)) {
                    splitpoint_beforeHalf = scan;
                    break;
                }
            }

            int splitpoint_afterHalf = FullscreenActivity.numrowstowrite;
            for (int scan = halfwaypoint; scan < myParsedLyrics.length; scan++) {
                if (myParsedLyrics[scan] != null) {
                    if (myParsedLyrics[scan].startsWith("[")) {
                        splitpoint_afterHalf = scan;
                        break;
                    } else if (myParsedLyrics[scan].length() == 0) {
                        splitpoint_afterHalf = scan + 1;
                        break;
                    }
                }
            }

            // Uses a '- n' to weight for filling the left columns
            if (Math.abs(halfwaypoint - splitpoint_beforeHalf) <= Math.abs(halfwaypoint - splitpoint_afterHalf) - 12) {
                FullscreenActivity.splitpoint = splitpoint_beforeHalf;
            } else {
                FullscreenActivity.splitpoint = splitpoint_afterHalf;
            }
        }

        // Which is the best split point closest to 1st third for 3 columns
        int splitpoint_before1stThird = 0;
        for (int scan = thirdwaypoint; scan > 0; scan--) {
            if (myParsedLyrics[scan] != null) {
                if (myParsedLyrics[scan].startsWith("[")) {
                    splitpoint_before1stThird = scan;
                    break;
                } else if (myParsedLyrics[scan].length() == 0) {
                    splitpoint_before1stThird = scan + 1;
                    break;
                }
            }
        }

        int splitpoint_after1stThird = 0;
        for (int scan = thirdwaypoint; scan < myParsedLyrics.length; scan++) {
            if (myParsedLyrics[scan] != null) {
                if (myParsedLyrics[scan].startsWith("[")) {
                    splitpoint_after1stThird = scan;
                    break;
                } else if (myParsedLyrics[scan].length() == 0) {
                    splitpoint_after1stThird = scan + 1;
                    break;
                }
            }
        }

        // Uses a '- n' to weight for filling the left column
        if (Math.abs(thirdwaypoint - splitpoint_before1stThird) <= Math.abs(thirdwaypoint - splitpoint_after1stThird) - 12) {
            FullscreenActivity.thirdsplitpoint = splitpoint_before1stThird;
        } else {
            FullscreenActivity.thirdsplitpoint = splitpoint_after1stThird;
        }

        // Which is the best split point closest to 2nd third for 3 columns
        int splitpoint_before2ndThird = splitpoint_before1stThird;
        for (int scan = twothirdwaypoint; scan > 0; scan--) {
            if (myParsedLyrics[scan] != null) {
                if (myParsedLyrics[scan].startsWith("[")) {
                    splitpoint_before2ndThird = scan;
                    break;
                } else if (myParsedLyrics[scan].length() == 0) {
                    splitpoint_before2ndThird = scan + 1;
                    break;
                }
            }
        }

        int splitpoint_after2ndThird = FullscreenActivity.numrowstowrite;
        for (int scan = twothirdwaypoint; scan < myParsedLyrics.length; scan++) {
            if (myParsedLyrics[scan] != null) {
                if (myParsedLyrics[scan].startsWith("[")) {
                    splitpoint_after2ndThird = scan;
                    break;
                } else if (myParsedLyrics[scan].length() == 0) {
                    splitpoint_after2ndThird = scan + 1;
                    break;
                }
            }
        }

        // IV - Use a '- n' to weight for filling the left columns
        if (Math.abs(twothirdwaypoint - splitpoint_before2ndThird) <= Math.abs(twothirdwaypoint - splitpoint_after2ndThird) - 12) {
            FullscreenActivity.twothirdsplitpoint = splitpoint_before2ndThird;
        } else {
            FullscreenActivity.twothirdsplitpoint = splitpoint_after2ndThird;
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
        return String.format("%1$-" + newlength + "s", string);
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

    boolean isAutoScrollValid(Context c, Preferences preferences) {
        // Get the autoScrollDuration;
        if (StaticVariables.mDuration.isEmpty()) {
            if (preferences.getMyPreferenceBoolean(c, "autoscrollUseDefaultTime", false)) {
                StaticVariables.autoScrollDuration = preferences.getMyPreferenceInt(c, "autoscrollDefaultSongLength", 180);
            } else {
                StaticVariables.autoScrollDuration = -1;
            }
        } else {
            try {
                StaticVariables.autoScrollDuration = Integer.parseInt(StaticVariables.mDuration.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                StaticVariables.autoScrollDuration = 0;
            }
        }

        // Get the autoScrollDelay;
        if (StaticVariables.mPreDelay.isEmpty()) {
            if (preferences.getMyPreferenceBoolean(c, "autoscrollUseDefaultTime", false)) {
                StaticVariables.autoScrollDelay = preferences.getMyPreferenceInt(c, "autoscrollDefaultSongPreDelay", 10);
            } else {
                StaticVariables.autoScrollDelay = 0;
            }
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
        if (string.startsWith("[")) {
            type = "heading";
        } else if (string.startsWith(".")) {
            type = "chord";
        } else if (string.startsWith(";")) {
            // Simple test for ; above means that the complex tests below are done only if a comment line
            if (string.startsWith(";__" + c.getResources().getString(R.string.edit_song_capo))) {
                type = "capoinfo";
            } else if (string.startsWith(";__")) {
                type = "extra";
                //} else if (string.startsWith(";"+c.getString(R.string.music_score))) {
                //    type = "abcnotation";
            } else if (string.length() > 4 && (string.indexOf("|") == 2 || string.indexOf("|") == 3)) {
                // Used to do this by identifying type of string start or drum start
                // Now just look for ;*| or ;**| where * is anything such as ;e | or ;BD|
                type = "tab";
            } else if ((string.contains("+") && string.contains("1") && string.contains("2"))) {
                // Drum tab count line
                type = "tab";
            } else {
                type = "comment";
            }
        } else {
            type = "lyric";
        }
        return type;
    }

    String[] getChordPositions(String chord, String lyric) {
        ArrayList<String> chordpositions = new ArrayList<>();

        // IV - Set ready for the loop
        boolean thischordcharempty;
        boolean prevchordcharempty = false;
        boolean prevlyriccharempty;
        boolean prevlyricempty = true;

        for (int x = 1; x < (chord.length()); x++) {
            thischordcharempty = chord.startsWith(" ", x);
            prevlyriccharempty = lyric.startsWith(" ", x - 1);
            prevlyricempty = prevlyricempty & prevlyriccharempty;

            // Add the start position of a chord
            if (!thischordcharempty && prevchordcharempty) {
                // Remove the previous chord end position when in a run of chords over lyric spaces
                if (prevlyricempty && lyric.startsWith(" ", x)) {
                    chordpositions.remove(chordpositions.size() - 1);
                }
                chordpositions.add(String.valueOf(x));
                prevlyricempty = true;
            // Add the end position of a chord if it ends over a lyric space
            } else if (thischordcharempty && !prevchordcharempty && prevlyriccharempty) {
                chordpositions.add(String.valueOf(x));
                prevlyricempty = true;
            }
            prevchordcharempty = thischordcharempty;
        }

        String[] chordpos = new String[chordpositions.size()];
        chordpos = chordpositions.toArray(chordpos);
        return chordpos;
    }

    String[] getSections(String string, String[] pos_string) {
        // Go through the line identifying sections
        ArrayList<String> workingsections = new ArrayList<>();

        int startpos = 0;
        int endpos;
        for (int x = 0; x < pos_string.length; x++) {
            endpos = Integer.parseInt(pos_string[x]);
            // We have a position for the end of a section, add the section
            workingsections.add(string.substring(startpos, endpos));
            startpos = endpos;
            if (x == pos_string.length - 1) {
                // For the last position we also add the end of line section
                endpos = string.length();
                workingsections.add(string.substring(startpos, endpos));
            }
        }
        if (startpos == 0) {
            // This is just a line, so add the whole line
            workingsections.add(string);
        }
        String[] sections = new String[workingsections.size()];
        sections = workingsections.toArray(sections);

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

    private TableRow capolinetoTableRow(Context c, int lyricsCapoColor, String[] chords, float fontsize) {
        int trimval = 0;
        TableRow caporow = new TableRow(c);
        caporow.setClipChildren(false);
        caporow.setClipToPadding(false);
        caporow.setPadding(0, 0, 0, 0);

        if (trimLines) {
            trimval = (int) (fontsize * scaleChords * lineSpacing);
            caporow.setPadding(0, -trimval, 0, -trimval);
            caporow.setGravity(Gravity.CENTER_VERTICAL);
        }

        for (String bit : chords) {
            TextView capobit = new TextView(c);
            capobit.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            capobit.setText(bit);
            capobit.setTextSize(fontsize * scaleChords);
            capobit.setTextColor(lyricsCapoColor);
            capobit.setTypeface(typefaceChords);

            if (displayBoldChordsHeadings) {
                capobit.setPaintFlags(capobit.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            }

            if (trimLines) {
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

    private TableRow chordlinetoTableRow(Context c, int lyricsChordsColor, String[] chords, float fontsize) {
        int trimval = 0;
        TableRow chordrow = new TableRow(c);
        chordrow.setClipChildren(false);
        chordrow.setClipToPadding(false);
        chordrow.setPadding(0, 0, 0, 0);

        if (trimLines) {
            trimval = (int) (fontsize * scaleChords * lineSpacing);
            chordrow.setPadding(0, -trimval, 0, -trimval);
            chordrow.setGravity(Gravity.CENTER_VERTICAL);
        }

        if (StaticVariables.whichMode.equals("Presentation")) {
            TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams();
            layoutParams.width = TableLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = presoLyricsAlign;
            chordrow.setLayoutParams(layoutParams);
        } else {
            chordrow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }

        for (String bit : chords) {
            TextView chordbit = new TextView(c);
            chordbit.setText(bit);
            chordbit.setTextSize(fontsize * scaleChords);
            chordbit.setTextColor(lyricsChordsColor);
            chordbit.setTypeface(typefaceChords);

            if (displayBoldChordsHeadings) {
                chordbit.setPaintFlags(chordbit.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            }

            if (StaticVariables.whichMode.equals("Presentation")) {
                chordbit.setSingleLine(true);
                if (!presoAutoScale && !presoShowChords) {
                    chordbit.setTextSize(fontSizePreso);
                }
            }

            if (trimLines) {
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
                                         StorageAccess storageAccess, Boolean presentation) {
        int trimval = 0;
        boolean projectlyricsonly = (!presoShowChords && (StaticVariables.whichMode.equals("Presentation") || presentation)) &&
                (!StaticVariables.whichMode.equals("Performance"));

        TableRow lyricrow = new TableRow(c);
        lyricrow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        lyricrow.setClipChildren(false);
        lyricrow.setClipToPadding(false);

        if (trimLines) {
            trimval = (int) (fontsize * lineSpacing);
            lyricrow.setPadding(0, -trimval, 0, -trimval);
            lyricrow.setGravity(Gravity.CENTER_VERTICAL);
        }

        // IV - Used when a lyrics only song display is processed
        boolean lyricsOnly = ((presentation && !presoShowChords) || (!presentation && !displayChords));

        // IV - Used to bold lines, ' B_' is an individual line bold
        boolean fakeBold = presentation && presoLyricsBold;
        if (lyrics[0].startsWith("B_")) {
            fakeBold = true;
            lyrics[0] = lyrics[0].replaceFirst("B_", "");
        }

        boolean doImage = false;
        if (lyrics.length == 1) {
            String bit0 = lyrics[0].toLowerCase(Locale.ROOT);
            doImage = ((bit0.endsWith(".png") || bit0.endsWith(".jpg") || bit0.endsWith(".gif")) ||
                    (bit0.contains("content://") || bit0.contains("file://")));
        }

        for (String bit : lyrics) {
            // Image
            if (doImage) {
                ImageView img = new ImageView(c);

                // By default, the image should be the not found one
                Drawable drw = ResourcesCompat.getDrawable(c.getResources(), R.drawable.notfound, null);

                int maxwidth = 320;
                if (FullscreenActivity.myWidthAvail > 0) {
                    // IV - Bigger for Stage mode presentation view
                    if (StaticVariables.whichMode.equals("Stage") && presentation) {
                        maxwidth = (int) (0.75f * (float) FullscreenActivity.myWidthAvail);
                    } else {
                        maxwidth = (int) (0.25f * (float) FullscreenActivity.myWidthAvail);
                    }
                }

                img.setMaxWidth(maxwidth);
                img.setMaxHeight(maxwidth);

                Uri uri = Uri.parse(bit.trim());
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
                // IV - If lyric line only, assemble and do the line in one go
                if (lyricsOnly) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(lyrics[0]);
                    for (int i = 1; i < lyrics.length; i++) {
                        sb.append(lyrics[i]);
                    }
                    bit = sb.toString();

                    // IV - Remove (....) comments when Stage or Presentation mode
                    if (!StaticVariables.whichMode.equals("Performance")) {
                        bit = bit.replaceAll("\\(.*?\\)", "");
                    }

                    // IV - Remove typical word splits, white space and trim - beautify!
                    bit = bit.replaceAll("_", "")
                            .replaceAll("\\s+-\\s+", "")
                            .replaceAll("\\s{2,}", " ")
                            .trim();

                    // IV - Add 2 spaces padding when presentation
                    if (presentation) {
                        // Before and after so that block text shadow has spaces on both sides
                        if (presoAutoScale && projectlyricsonly) {
                            bit = "  " + bit + "  ";
                        } else {
                            bit = bit + "  ";
                        }
                    }
                } else {
                    bit = bit.replace("_", " ");
                }

                TextView lyricbit = new TextView(c);
                lyricbit.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                lyricbit.setSingleLine(false);
                lyricbit.setTextSize(fontsize);

                // IV - Overrides for layoutparams, gravity and more - depending on mode
                if (projectlyricsonly) {
                    lyricbit.setTextColor(presoFontColor);
                    lyricbit.setTypeface(typefacePreso);
                    lyricbit.setTextAlignment(presotextAlignFromGravity(presoLyricsAlign));
                    lyricbit.setGravity(presoLyricsAlign);
                    if (presoAutoScale || presoShowChords) {
                        lyricbit.setSingleLine(true);
                    } else {
                        // IV - If we have turned off autoscale and aren't showing the chords, support wrapping
                        // IV - The line is trimmed set to wrap full width
                        // IV - Block text use will also (intentionally) display full width - split lines work as a full width block
                        lyricbit.setTextSize(fontSizePreso);
                        lyricbit.setText(bit.trim());
                        int w = StaticVariables.cast_availableWidth_1col;
                        lyricbit.setLayoutParams(new TableRow.LayoutParams(w, TableRow.LayoutParams.WRAP_CONTENT));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            lyricbit.setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED);
                        }
                    }
                } else {
                    lyricbit.setTextColor(lyricsTextColor);
                    lyricbit.setTypeface(typefaceLyrics);
                }

                // IV - Support bold lyrics when presentation
                if (fakeBold) {
                    lyricbit.setPaintFlags(lyricbit.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                }

                // IV - Only use if the bit is not 'empty'.  This results in the chord line spacing being used
                if (!bit.trim().isEmpty()) {
                    lyricbit.setText(bit);
                }

                if (trimLines) {
                    lyricbit.setIncludeFontPadding(false);
                    lyricbit.setGravity(Gravity.CENTER_VERTICAL);
                    lyricbit.setPadding(0, -trimval, 0, -trimval);
                    // IV - Not when wrapping presentation lyrics (or wrapping breaks)
                    if (!presentation || presoAutoScale || presoShowChords) {
                        lyricbit.setLineSpacing(0f, 0f);
                    }
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

    private TableRow commentlinetoTableRow(Context c, int presoFontColor, int lyricsTextColor, String[] comment, float fontsize, boolean tab) {
        int trimval = 0;

        TableRow commentrow = new TableRow(c);
        commentrow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        commentrow.setClipChildren(false);
        commentrow.setClipToPadding(false);
        commentrow.setPadding(0, 0, 0, 0);

        if (trimLines) {
            trimval = (int) (fontsize * lineSpacing);
            commentrow.setPadding(0, -trimval, 0, -trimval);
            commentrow.setGravity(Gravity.CENTER_VERTICAL);
        }

        // IV - Remove an underscore OR replace with a space if showing chords
        String underscoreReplace = "";

        if ("Presentation".equals(StaticVariables.whichMode)) {
            if (presoShowChords) underscoreReplace = " ";
        } else {
            if (displayChords) underscoreReplace = " ";
        }

        for (String bit : comment) {
            if (bit.startsWith("__")) {
                bit = bit.replace("__", "");
            }

            bit = bit.replace("_", underscoreReplace);

            TextView lyricbit = new TextView(c);

            lyricbit.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            lyricbit.setText(bit);
            lyricbit.setTextSize(fontsize * scaleComments);
            if (StaticVariables.whichMode.equals("Presentation")) {
                lyricbit.setTextColor(presoFontColor);
                lyricbit.setTypeface(typefacePreso);
                if (presoLyricsBold) {
                    lyricbit.setPaintFlags(lyricbit.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                }

            } else {
                lyricbit.setTextColor(lyricsTextColor);
                lyricbit.setTypeface(typefaceLyrics);

            }
            if (tab) {
                // Set the comment text as monospaced to make it fit
                lyricbit.setTypeface(typefaceMono);
            }
            if (trimLines) {
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
        titleview.setTypeface(typefaceLyrics);
        titleview.setTextSize(fontsize * preferences.getMyPreferenceFloat(c, "scaleHeadings", 0.6f));
        if (displayBoldChordsHeadings) {
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

            // Quick exit if not multiline
            if (!string.contains("\n1") || !string.contains("\n2")) return string;

            try {
                boolean has_multiline_vtag = false;
                boolean has_multiline_ctag = false;

                for (String line: string.split("\n")) {
                    line= line.toLowerCase(StaticVariables.locale);
                    if (line.startsWith("[v]")) {
                        has_multiline_vtag = true;
                    } else if (line.startsWith("[c]")) {
                        has_multiline_ctag = true;
                    }
                }

                if (has_multiline_vtag || has_multiline_ctag) {

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

    String removeChordLines(String song) {
        StringBuilder newsong = new StringBuilder();

        // Split the song into separate lines
        for (String thisline : song.split("\n")) {
            if (!thisline.startsWith(".")) {
                newsong.append(thisline).append("\n");
            }
        }
        return newsong.toString();
    }

    String getAllChords(String song) {
        StringBuilder newsong = new StringBuilder();

        // Split the song into separate lines
        for (String thisline : song.split("\n")) {
            if (thisline.startsWith(".")) {
                newsong.append(thisline).append(" ");
            }
        }
        return newsong.toString().replace(".", " ");
    }

    String removeCommentLines(String song) {
        StringBuilder newsong = new StringBuilder();

        // Split the song into separate lines
        for (String thisline : song.split("\n")) {
            if (!thisline.startsWith(";")) {
                newsong.append(thisline).append("\n");
            }
        }
        return newsong.toString();
    }

    void prepareSongSections(Context c, Preferences preferences, StorageAccess storageAccess) {
        // IV - Song section preparation for all modes is done here

        boolean isPresentation = StaticVariables.whichMode.equals("Presentation");

        // 1. Sort multiline verse/chord formats
        FullscreenActivity.myLyrics = fixMultiLineFormat(c, preferences, FullscreenActivity.myLyrics);

        // 2. Add any extra info
        if (!isPresentation) addExtraInfo(c, storageAccess, preferences);

        // 3. Deal with line splits
        // Line splits | are relevant to presentation
        String lineSplit = " ";
        if (isPresentation && !presoShowChords) lineSplit = "\n";

        // Sections splits || are relevant to presentation and Stage mode. If sectionSplit is ║ is used to test for further processing later.
        String sectionSplit = "";
        if (isPresentation || StaticVariables.whichMode.equals("Stage")) sectionSplit = "║";

        // We split at \n\n but not for scripture
        String doubleNewlineSplit = "\n\n";
        if (!StaticVariables.whichSongFolder.contains(c.getResources().getString(R.string.scripture))) {
            doubleNewlineSplit = "§";
        }

        StringBuilder sb = new StringBuilder();

        // Process || and | split markers on lyric lines
        // Add a trailing ¶ to force a split behaviour that copes with a trailing new line!
        for (String line : (FullscreenActivity.myLyrics+"¶").split("\n")) {
            // IV - Use leading \n as we can be certain it is safe to remove later
            sb.append("\n");
            if (line.startsWith(" ")) {
                line = line
                        .replace("||", sectionSplit)
                        .replace("|", lineSplit);
            }
            // Add it back up
            sb.append(line);
        }

        String[] currsections = sb.toString()
                .replace("-!!", "")
                // --- Process new section markers
                .replace("\n ---","\n[]")
                .replace("\n---","\n[]")
                .substring(1).replace("¶", "")
                // Prevent empty lines
                .replace("\n\n", doubleNewlineSplit)
                .replace("\n[", "§[")
                .replace("§\n", "§")
                .replace("\n§", "§")
                .replace("§ §", "§")
                .replace("§§", "§")
                .split("§");

        // 4. Put into presentation order when required
        if (preferences.getMyPreferenceBoolean(c, "usePresentationOrder", false) &&
                !StaticVariables.mPresentation.equals("")) {
            currsections = matchPresentationOrder(currsections);
        }

        // 5. Deal with null sections (ignore) and section splits (handle chords when split by a section split)
        ArrayList<String> newbits = new ArrayList<>();
        String [] thissection;

        for (int x = 0; x < currsections.length; x++) {
            if (currsections[x] != null) {
                if (sectionSplit.equals("║") && currsections[x].contains("║")) {
                    thissection = currsections[x].split("\n");
                    // Start from line 1 as line 0 cannot have a chord line above
                    for (int line = 1; line < thissection.length; line++) {
                        // If || splits chords we need to move chords after the split into their correct section
                        if (thissection[line].contains("║") && thissection[line - 1].startsWith(".")) {
                            int chordssplitpoint = thissection[line].indexOf("║");
                            if ((chordssplitpoint + 2) < thissection[line - 1].length()) {
                                thissection[line] = thissection[line].replace("║", "║." + thissection[line - 1].substring(chordssplitpoint + 2) + "\n");
                                thissection[line - 1] = thissection[line - 1].substring(0, chordssplitpoint);
                            }
                        }
                    }

                    // Put the section back together
                    currsections[x] = "";
                    for (String line : thissection) {
                        currsections[x] += line + "\n";
                    }
                    // Add to the array split using the section splits
                    Collections.addAll(newbits, currsections[x].split("║"));

                } else {
                    // Add to the array
                    newbits.add(currsections[x]);
                }
            }
        }

        if (newbits.size() < 1) {
            newbits.add("");
        }

        // 6. Get the section labels/types and section line contents/types (we are after any presentation order)
        int songSectionsLength = newbits.size();
        StaticVariables.songSections       = new String[songSectionsLength];
        StaticVariables.songSectionsLabels = new String[songSectionsLength];
        StaticVariables.songSectionsTypes  = new String[songSectionsLength];
        StaticVariables.sectionContents    = new String[songSectionsLength][];
        StaticVariables.sectionLineTypes   = new String[songSectionsLength][];

        if (isPresentation) {
            StaticVariables.projectedContents  = new String[songSectionsLength][];
            StaticVariables.projectedLineTypes = new String[songSectionsLength][];
        }

        // Initialise working variable for previous song section name used by getSectionHeadings
        StaticVariables.songSection_holder = "";

        for (int x = 0; x < songSectionsLength; x++) {

            StaticVariables.songSections[x] = newbits.get(x);

            if (preferences.getMyPreferenceBoolean(c, "trimSections", true)) {
                StaticVariables.songSections[x] = StaticVariables.songSections[x].trim();
            }

            if (isPresentation) {
                StaticVariables.songSectionsLabels[x] = getSectionHeadings(StaticVariables.songSections[x]);

                // Now that we have generated the song, decide if we should remove lines etc.
                // Remove tag/heading lines
                StaticVariables.songSections[x] = StaticVariables.songSections[x].replaceAll("\\[.*?]\n", "").replaceAll("\\[.*?]", "");
                // Remove chord lines
                if (!preferences.getMyPreferenceBoolean(c, "presoShowChords", false)) {
                    StaticVariables.songSections[x] = removeChordLines(StaticVariables.songSections[x]);
                }
                // Remove comment lines
                if (!StaticVariables.isHost || !StaticVariables.isConnected || !StaticVariables.usingNearby) {
                    StaticVariables.songSections[x] = removeCommentLines(StaticVariables.songSections[x]);
                }
                // Remove underscores
                StaticVariables.songSections[x] = removeUnderScores(c, preferences, StaticVariables.songSections[x]);

                // Split each section into lines in a string array, determine each line type and get sectionContents lines end trimmed
                StaticVariables.sectionContents[x] = StaticVariables.songSections[x].split("\n");
                StaticVariables.projectedContents[x] = StaticVariables.songSections[x].split("\n");

                int scl = StaticVariables.sectionContents[x].length;
                StaticVariables.sectionLineTypes[x] = new String[scl];
                StaticVariables.projectedLineTypes[x] = new String[scl];
                for (int y = 0; y < scl; y++) {
                    StaticVariables.sectionLineTypes[x][y] = determineLineTypes(StaticVariables.sectionContents[x][y], c);
                    StaticVariables.projectedLineTypes[x][y] = StaticVariables.sectionLineTypes[x][y];
                    if (StaticVariables.sectionContents[x][y].length() > 0 && " .;".contains(StaticVariables.sectionContents[x][y].substring(0, 1))) {
                        StaticVariables.sectionContents[x][y] = StaticVariables.sectionContents[x][y].substring(1).replaceAll("\\s+$", "");
                    }
                }
            } else {
                // Remove heading of any extra information header and footer
                StaticVariables.songSections[x] = StaticVariables.songSections[x].replace("[H__1]\n", "").replace("[F__1]\n", "");

                StaticVariables.songSectionsLabels[x] = getSectionHeadings(StaticVariables.songSections[x]);

                // Split each section into lines in a string array, determine each line type and get sectionContents lines end trimmed
                StaticVariables.sectionContents[x] = StaticVariables.songSections[x].split("\n");
                int scl = StaticVariables.sectionContents[x].length;

                StaticVariables.sectionLineTypes[x] = new String[scl];
                for (int y = 0; y < scl; y++) {
                    StaticVariables.sectionLineTypes[x][y] = determineLineTypes(StaticVariables.sectionContents[x][y], c);
                    if (StaticVariables.sectionContents[x][y].length() > 0 && " .;".contains(StaticVariables.sectionContents[x][y].substring(0, 1))) {
                        StaticVariables.sectionContents[x][y] = StaticVariables.sectionContents[x][y].substring(1).replaceAll("\\s+$", "");
                    }
                }
            }
        }
    }

    String getSectionHeadings(String songsection) {
        String label = "";
        int startoftag = songsection.indexOf("[");
        if (startoftag > -1) {
            int endoftag = songsection.indexOf("]");
            if (endoftag < startoftag) {
                label = songsection.substring(startoftag + 1);
            } else if (endoftag > startoftag) {
                label = songsection.substring(startoftag + 1, endoftag);
            } else {
                label = songsection.replace("[", "").replace("]", "");
            }
            StaticVariables.songSection_holder = label;
        }
        if (label.equals("")) {
            // If section is just a comment line, have no label
            if (songsection.startsWith((";"))) {
                label = "";
            } else if (songsection.split("\n").length < 2) {
                label = "";
            } else {
                // IV - Use the last found label
                label = StaticVariables.songSection_holder;
            }
        }
        return label;
    }

    String[] matchPresentationOrder(String[] currentSections) {

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
            if (currentSectionLabels[sl].equals("H__1")) {
                tempPresentationOrder.insert(0, "H__1 ");
            }
            if (currentSectionLabels[sl].equals("F__1")) {
                tempPresentationOrder.append("F__1 ");
            }
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

        // Go through the tempPresOrderArray and add the sections back together
        ArrayList<String> presentationSectionsList = new ArrayList<>();
        for (String aTempPresOrderArray : tempPresOrderArray) {
            if (!aTempPresOrderArray.equals("")) {
                for (int a = 0; a < currentSectionLabels.length; a++) {
                    if (currentSectionLabels[a].trim().equals(aTempPresOrderArray.trim())) {
                        presentationSectionsList.add(currentSections[a]);
                    }
                }
            }
        }
        String[] presentationSections = new String[presentationSectionsList.size()];
        presentationSectionsList.toArray(presentationSections);

        // Display any errors
        StaticVariables.myToastMessage = errors.toString();
        return presentationSections;
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
            // IV - Get the capokey here to support later getCapoNewKey call
            if (StaticVariables.mKey != null) {
                Transpose transpose = new Transpose();
                transpose.capoKeyTranspose(c, preferences);
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
            // IV - Done in three places to handle sections without heading
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

        // IV - songSectionChordPro
        nextLine = StaticVariables.sectionContents[x][0];
        thisLineType = "";

        for (int y = 0; y < linenums; y++) {
            // Go through the section a line at a time
            thisLine = nextLine;
            previousLineType = thisLineType;
            thisLineType = StaticVariables.sectionLineTypes[x][y];

            if (y < linenums - 1) {
                nextLine = StaticVariables.sectionContents[x][y + 1];
                nextLineType = StaticVariables.sectionLineTypes[x][y + 1];
            } else {
                nextLine = "";
                nextLineType = "";
            }

            switch (howToProcessLines(y, linenums, thisLineType, nextLineType, previousLineType)) {
                // If this is a chord line followed by a lyric line.
                case "chord_then_lyric":
                    // IV - We have a next line - now make lines the same length.
                    if (thisLine.length() < nextLine.length()) {
                        thisLine = fixLineLength(thisLine, nextLine.length());
                    } else {
                        nextLine = fixLineLength(nextLine, thisLine.length());
                    }

                    // IV - Chord positioning now uses the lyric line
                    positions_returned = getChordPositions(thisLine, nextLine);
                    chords_returned = getSections(thisLine, positions_returned);
                    lyrics_returned = getSections(nextLine, positions_returned);
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
                    chords_returned = getSections(thisLine, positions_returned);
                    lyrics_returned = getSections(tempString, positions_returned);
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
        boolean displayChords = preferences.getMyPreferenceBoolean(c, "displayChords", true);
        boolean trimSections = preferences.getMyPreferenceBoolean(c, "trimSections", true);
        StringBuilder text = new StringBuilder();
        String[] heading = beautifyHeadings(StaticVariables.songSectionsLabels[x], c);
        if (heading[0].trim().equals("")) {
            text.append("Section:\n");
        } else {
            text.append(heading[0].trim()).append(":\n");
        }

        int linenums = StaticVariables.sectionContents[x].length;

        // Go through each line and add the appropriate lyrics with chords in them
        for (int y = 0; y < linenums; y++) {
            if (StaticVariables.sectionContents[x][y].length() > 1) {
                switch (StaticVariables.sectionLineTypes[x][y]) {
                    case "heading":
                        // Already added above
                        break;
                    case "comment":
                        text.append(StaticVariables.sectionContents[x][y]).append("\n");
                        break;
                    case "chord":
                        if (displayChords) {
                            text.append(StaticVariables.sectionContents[x][y]).append("\n");
                        }
                        break;
                    default:
                        // If none of the above, it's a lyrics line without leading space
                        // IV - Same logic as in ProcessSong
                        if (!displayChords) {
                            // IV - Remove (....) comments when Stage or Presentation mode
                            if (!StaticVariables.whichMode.equals("Performance")) {
                                StaticVariables.sectionContents[x][y] = StaticVariables.sectionContents[x][y].replaceAll("\\(.*?\\)", "");
                            }
                            // IV - Remove typical word splits, white space and trim - beautify!
                            StaticVariables.sectionContents[x][y] = StaticVariables.sectionContents[x][y].replaceAll("_", "")
                                    .replaceAll("\\s+-\\s+", "")
                                    .replaceAll("\\s{2,}", " ")
                                    .trim();
                        }
                        text.append(StaticVariables.sectionContents[x][y]).append("\n");
                        break;
                }
            }
        }

        if (trimSections) {
            text = new StringBuilder(text.toString().trim());
        }
        return text.toString();
    }

    LinearLayout songSectionView(Context c, int x, float fontsize,
                                 StorageAccess storageAccess, Preferences preferences,
                                 int lyricsTextColor, int lyricsBackgroundColor, int lyricsChordsColor,
                                 int lyricsCommentColor, int lyricsCustomColor,
                                 int lyricsCapoColor, int presoFontColor,
                                 int lyricsVerseColor, int lyricsChorusColor,
                                 int lyricsPreChorusColor, int lyricsBridgeColor, int lyricsTagColor) {

        Transpose transpose = new Transpose();
        // Added in chord format check otherwise app gets stuck with detected format
        Transpose.checkChordFormat();
        getPreferences(c, preferences);

        // Decide if chords are valid to be shown

        int mcapo = Integer.parseInt("0" + StaticVariables.mCapo);

        boolean doCapoChords = displayChords &&
                ((displayCapoChords && mcapo > 0 && mcapo < 12) ||
                        (preferences.getMyPreferenceInt(c, "chordFormat", 0) > 0 &&
                                StaticVariables.detectedChordFormat != StaticVariables.newChordFormat));

        boolean doNativeChords = displayChords && (!doCapoChords || displayCapoAndNativeChords);

        boolean doCapoChordsOnly = doCapoChords && !doNativeChords;

        final LinearLayout ll = new LinearLayout(c);
        ll.setLayoutParams(linearlayout_params());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 0, 0, 0);
        ll.setClipChildren(false);
        ll.setClipToPadding(false);

        String[] returnvals = beautifyHeadings(StaticVariables.songSectionsLabels[x], c);

        // Identify the section type
        if (x < StaticVariables.songSectionsTypes.length) StaticVariables.songSectionsTypes[x] = returnvals[1];

        if (checkForFilter(c, preferences, x)) {
            // IV - If first title is empty then do not do add to view and mark section as comment.  This helps the song details block.
            if (x == 0 & returnvals[0].equals("")) {
                returnvals[1] = "comment";
            } else {
                ll.addView(titletoTextView(c, preferences, lyricsTextColor, returnvals[0], fontsize));
            }

            String[] whattoprocess;
            String[] linetypes;
            int linenums;
            int linelength;

            whattoprocess = StaticVariables.sectionContents[x];
            linetypes = StaticVariables.sectionLineTypes[x];
            linenums = whattoprocess.length;

            // IV - songSectionView
            nextLine = whattoprocess[0];
            thisLineType = "";

            for (int y = 0; y < linenums; y++) {
                // Go through the section a line at a time
                thisLine = nextLine;
                previousLineType = thisLineType;
                thisLineType = linetypes[y];

                if (y < linenums - 1) {
                    nextLine = whattoprocess[y + 1];
                    nextLineType = linetypes[y + 1];
                }

                TableLayout tl = createTableLayout(c);

                // IV - songSectionView
                // IV - 2 spaces added to output lines below to reduce occurance of right edge overrun
                switch (howToProcessLines(y, linenums, thisLineType, nextLineType, previousLineType)) {
                    case "chord_then_lyric":
                        if (doCapoChordsOnly) {
                            capoLine = transpose.capoTranspose(c, preferences, thisLine);

                            // IV - We need lines to be the same length.
                            linelength = Math.max(capoLine.length(),nextLine.length());
                            if (capoLine.length() != linelength) capoLine = fixLineLength(capoLine, linelength);
                            if (nextLine.length() != linelength) nextLine = fixLineLength(nextLine, linelength);

                            // IV - Use capo chord positions
                            positions_returned = getChordPositions(capoLine, nextLine);

                            chords_returned = getSections(capoLine + "  ", positions_returned);
                            tl.addView(capolinetoTableRow(c, lyricsCapoColor, chords_returned, fontsize));
                        } else {
                            if (doCapoChords) {
                                capoLine = transpose.capoTranspose(c, preferences, thisLine);

                                // IV - We need lines to be the same length.
                                linelength = Math.max(Math.max(thisLine.length(),nextLine.length()),capoLine.length());
                                if (capoLine.length() != linelength) capoLine = fixLineLength(capoLine, linelength);
                                if (thisLine.length() != linelength) thisLine = fixLineLength(thisLine, linelength);
                                if (nextLine.length() != linelength) nextLine = fixLineLength(nextLine, linelength);

                                // IV - Use native chord positions
                                positions_returned = getChordPositions(thisLine, nextLine);

                                try {
                                    // IV - Keep only those positions that do not split a capo or native chord
                                    ArrayList<String> workingpositions = new ArrayList<>();
                                    int position;
                                    for (String s : positions_returned) {
                                        position = Integer.parseInt(s);
                                        if ((capoLine).substring(position - 1, position + 1).contains(" ") &&
                                                (thisLine).substring(position - 1, position + 1).contains(" ")) {
                                            workingpositions.add(s);
                                        }
                                    }
                                    positions_returned = new String[workingpositions.size()];
                                    positions_returned = workingpositions.toArray(positions_returned);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                chords_returned = getSections(capoLine + "  ", positions_returned);
                                tl.addView(capolinetoTableRow(c, lyricsCapoColor, chords_returned, fontsize));
                            } else {
                                // IV - We need lines to be the same length.
                                linelength = Math.max(thisLine.length(),nextLine.length());
                                if (thisLine.length() != linelength) thisLine = fixLineLength(thisLine, linelength);
                                if (nextLine.length() != linelength) nextLine = fixLineLength(nextLine, linelength);

                                // IV - Use native chord positions
                                positions_returned = getChordPositions(thisLine, nextLine);
                            }
                            if (doNativeChords) {
                                // IV - Positions returned determined above
                                chords_returned = getSections(thisLine + "  ", positions_returned);
                                tl.addView(chordlinetoTableRow(c, lyricsChordsColor, chords_returned, fontsize));
                            }
                        }
                        if (displayLyrics) {
                            lyrics_returned = getSections(nextLine + "  ", positions_returned);
                            tl.addView(lyriclinetoTableRow(c, lyricsTextColor, presoFontColor,
                                    lyrics_returned, fontsize, storageAccess, false));
                        }
                        break;

                    case "chord_only":
                        chords_returned = new String[1];
                        if (doCapoChords) {
                            chords_returned[0] = transpose.capoTranspose(c, preferences, thisLine) + "  ";
                            tl.addView(capolinetoTableRow(c, lyricsCapoColor, chords_returned, fontsize));
                        }
                        if (doNativeChords) {
                            chords_returned[0] = thisLine + "  ";
                            tl.addView(chordlinetoTableRow(c, lyricsChordsColor, chords_returned, fontsize));
                        }
                        break;

                    case "lyric_no_chord":
                        if (displayLyrics) {
                            lyrics_returned = new String[1];
                            lyrics_returned[0] = thisLine + "  ";
                            tl.addView(lyriclinetoTableRow(c, lyricsTextColor, presoFontColor,
                                    lyrics_returned, fontsize, storageAccess, false));
                        }
                        break;

                    case "comment_no_chord":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine + "  ";
                        tl.addView(commentlinetoTableRow(c, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, false));
                        tl.setBackgroundColor(lyricsCommentColor);
                        break;

                    case "extra_info":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine + "  ";
                        TableRow tr = commentlinetoTableRow(c, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, false);
                        tr.setGravity(Gravity.END);
                        tl.addView(tr);
                        tl.setGravity(Gravity.END);
                        tl.setBackgroundColor(lyricsCustomColor);
                        break;

                    case "capo_info":
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine + "  ";
                        TableRow trc = commentlinetoTableRow(c, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, false);
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
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine + "  ";
                        tl.addView(commentlinetoTableRow(c, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, true));
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
                    if (addSectionSpace) {
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
        } else {
            // Filtered out so GONE
            ll.setVisibility(View.GONE);
        }

        // IV - Set the sections background color depending on section type
        ll.setBackgroundColor(getSectionColors(returnvals[1], lyricsVerseColor, lyricsChorusColor, lyricsPreChorusColor,
                lyricsBridgeColor, lyricsTagColor, lyricsCommentColor, lyricsCustomColor));
        return ll;
    }

    private boolean checkForFilter(Context c, Preferences preferences, int x) {
        if (preferences.getMyPreferenceBoolean(c, "commentFiltering", false) &&
                StaticVariables.songSectionsLabels[x].startsWith("*") &&
                StaticVariables.songSectionsLabels[x].contains(":")) {
            // Check if it should be filtered out
            boolean showOnlyFilter = preferences.getMyPreferenceBoolean(c, "commentFilterOnlyShow", false);
            String myFilter = preferences.getMyPreferenceString(c, "commentFilters", "X__XX__X");
            if (!myFilter.equals("X__XX__X")) {
                String checkFilter = StaticVariables.songSectionsLabels[x].substring(1, StaticVariables.songSectionsLabels[x].indexOf(":"));
                // Sanity check
                if (!checkFilter.isEmpty()) {
                    // Only show if not in filter
                    // User either wants to show matching filters, so hide others or
                    // Hide matching filters, so hide this
                    return (!showOnlyFilter || myFilter.contains("X__X" + checkFilter + "X__X")) &&
                            (showOnlyFilter || !myFilter.contains("X__X" + checkFilter + "X__X"));
                }
            }
        }
        return true;
    }

    LinearLayout projectedSectionView(Context c, int x, float fontsize, StorageAccess storageAccess,
                                      Preferences preferences,
                                      int lyricsTextColor, int lyricsChordsColor,
                                      int lyricsCapoColor, int presoFontColor, int presoShadowColor) {
        Transpose transpose = new Transpose();

        getPreferences(c, preferences);

        boolean isPresentation = StaticVariables.whichMode.equals("Presentation");
        boolean presentationChordsandlyrics = isPresentation && presoShowChords;
        boolean stagelyricsonly = StaticVariables.whichMode.equals("Stage") && !presoShowChords;

        // Decide if chords are valid to be shown
        int mcapo = Integer.parseInt("0" + StaticVariables.mCapo);

        boolean doCapoChords = presoShowChords &&
                ((displayCapoChords && mcapo > 0 && mcapo < 12) ||
                (preferences.getMyPreferenceInt(c, "chordFormat", 0) > 0 &&
                 StaticVariables.detectedChordFormat != StaticVariables.newChordFormat));

        boolean doNativeChords = presoShowChords && (!doCapoChords || displayCapoAndNativeChords);

        boolean doCapoChordsOnly = doCapoChords && !doNativeChords;

        final LinearLayout ll = new LinearLayout(c);

        if (isPresentation || stagelyricsonly) {
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
        int linelength;

        if (isPresentation) {
            // projectedContents have a leading linetype character that is removed later
            whattoprocess = StaticVariables.projectedContents[x];
            linetypes = StaticVariables.projectedLineTypes[x];
        } else {
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
        }

        linenums = whattoprocess.length;

        // projectedSectionView
        // 2 spaces added to output lines in the loop to reduce occurance of right edge overrun
        // IV - projectedContent lines need the line type character removing
        if (presentationChordsandlyrics & whattoprocess[0].length() > 0) {
            nextLine = whattoprocess[0].substring(1);
        } else {
            nextLine = whattoprocess[0];
        }
        thisLineType = "";

        for (int y = 0; y < linenums; y++) {
            // Go through the section a line at a time
            thisLine = nextLine;
            previousLineType = thisLineType;
            thisLineType = linetypes[y];

            if (y < linenums - 1) {
                if (presentationChordsandlyrics & whattoprocess[y + 1].length() > 0) {
                    nextLine = whattoprocess[y + 1].substring(1);
                } else {
                    nextLine = whattoprocess[y + 1];
                }
                nextLineType = linetypes[y + 1];
            }

            TableLayout tl = createTableLayout(c);

            if (isPresentation || stagelyricsonly) {
                tl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }

            switch (howToProcessLines(y, linenums, thisLineType, nextLineType, previousLineType)) {
                // If this is a chord line followed by a lyric line.
                case "chord_then_lyric":
                    if (doCapoChordsOnly) {
                        capoLine = transpose.capoTranspose(c, preferences, thisLine);

                        // IV - We need lines to be the same length.
                        linelength = Math.max(capoLine.length(),nextLine.length());
                        if (capoLine.length() != linelength) capoLine = fixLineLength(capoLine, linelength);
                        if (nextLine.length() != linelength) nextLine = fixLineLength(nextLine, linelength);

                        // IV - Use capo chord positions
                        positions_returned = getChordPositions(capoLine, nextLine);

                        chords_returned = getSections(capoLine + "  ", positions_returned);
                        tl.addView(capolinetoTableRow(c, lyricsCapoColor, chords_returned, fontsize));
                    } else {
                        if (doCapoChords) {
                            capoLine = transpose.capoTranspose(c, preferences, thisLine);

                            // IV - We need lines to be the same length.
                            linelength = Math.max(Math.max(thisLine.length(),nextLine.length()),capoLine.length());
                            if (capoLine.length() != linelength) capoLine = fixLineLength(capoLine, linelength);
                            if (thisLine.length() != linelength) thisLine = fixLineLength(thisLine, linelength);
                            if (nextLine.length() != linelength) nextLine = fixLineLength(nextLine, linelength);

                            // IV - Start wih native chord positions
                            positions_returned = getChordPositions(thisLine, nextLine);

                            try {
                                // IV - Keep only those positions that do not split a capo or native chord
                                ArrayList<String> workingpositions = new ArrayList<>();
                                int position;
                                for (String s : positions_returned) {
                                    position = Integer.parseInt(s);
                                    if ((capoLine).substring(position - 1, position + 1).contains(" ") &&
                                            (thisLine).substring(position - 1, position + 1).contains(" ")) {
                                        workingpositions.add(s);
                                    }
                                }
                                positions_returned = new String[workingpositions.size()];
                                positions_returned = workingpositions.toArray(positions_returned);

                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                            chords_returned = getSections(capoLine + "  ", positions_returned);
                            tl.addView(capolinetoTableRow(c, lyricsCapoColor, chords_returned, fontsize));
                        } else {
                            // IV - We need lines to be the same length.
                            linelength = Math.max(thisLine.length(),nextLine.length());
                            if (thisLine.length() != linelength) thisLine = fixLineLength(thisLine, linelength);
                            if (nextLine.length() != linelength) nextLine = fixLineLength(nextLine, linelength);

                            // IV - Use native chord positions
                            positions_returned = getChordPositions(thisLine, nextLine);
                        }
                        if (doNativeChords) {
                            // IV - Positions returned determined above
                            chords_returned = getSections(thisLine + "  ", positions_returned);
                            tl.addView(chordlinetoTableRow(c, lyricsChordsColor, chords_returned, fontsize));
                        }
                    }
                    if (displayLyrics) {
                        // IV - For stage mode when showing lyrics only, songs can have commented out alternative lines which should not be projected
                        if (!(stagelyricsonly && nextLineType.equals("comment"))) {
                            lyrics_returned = getSections(nextLine + "  ", positions_returned);
                            tl.addView(lyriclinetoTableRow(c, lyricsTextColor, presoFontColor, lyrics_returned, fontsize, storageAccess, true));
                        }
                    }
                    break;

                case "chord_only":
                    chords_returned = new String[1];
                    if (doCapoChords) {
                        chords_returned[0] = transpose.capoTranspose(c, preferences, thisLine) + "  ";
                        tl.addView(capolinetoTableRow(c, lyricsCapoColor, chords_returned, fontsize));
                    }
                    if (doNativeChords) {
                        chords_returned[0] = thisLine + "  ";
                        tl.addView(chordlinetoTableRow(c, lyricsChordsColor, chords_returned, fontsize));
                    }
                    break;

                case "lyric_no_chord":
                    if (displayLyrics) {
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine + "  ";
                        tl.addView(lyriclinetoTableRow(c, lyricsTextColor, presoFontColor, lyrics_returned, fontsize, storageAccess, true));
                    }
                    break;

                case "comment_no_chord":
                    if (!stagelyricsonly) {
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine + "  ";
                        tl.addView(commentlinetoTableRow(c, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, false));
                    }
                    break;

                case "extra_info":
                    if (!stagelyricsonly) {
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine + "  ";
                        TableRow tr = commentlinetoTableRow(c, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, false);
                        tr.setGravity(Gravity.END);
                        tl.addView(tr);
                        tl.setGravity(Gravity.END);
                    }
                    break;

                case "guitar_tab":
                    if (!stagelyricsonly) {
                        lyrics_returned = new String[1];
                        lyrics_returned[0] = thisLine + "  ";
                        tl.addView(commentlinetoTableRow(c, presoFontColor, lyricsTextColor, lyrics_returned, fontsize, true));
                    }
                    break;
            }
            if (blockShadow) {
                tl.setBackgroundColor(getColorWithAlpha(presoShadowColor, blockShadowAlpha));
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
        if (StaticVariables.mTitle.equals("")) {
            StaticVariables.mTitle = StaticVariables.songfilename.replaceAll("\\.[^.]*$", "");
        }

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

            if (mFileDescriptor == null) {
                // IV - No file
                return null;
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

            StaticVariables.currentSection = FullscreenActivity.pdfPageCurrent;

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

            try {
                mFileDescriptor.close();
            } catch (Exception e) {
                // Problem closing the file descriptor, but not critical
            }

            return bitmap;

        } else {
            // Make the image to be displayed on the screen a pdf icon
            StaticVariables.myToastMessage = c.getResources().getString(R.string.nothighenoughapi);
            ShowToast.showToast(c);

            return null;
        }
    }

    float getScaleValue(Context c, Preferences preferences, float x, float y) {
        float scale;
        if (StaticVariables.thisSongScale == null) {
            StaticVariables.thisSongScale = preferences.getMyPreferenceString(c, "songAutoScale", "W");
        }
        float maxscale = preferences.getMyPreferenceFloat(c, "fontSizeMax", 50) / (float) 12.0;
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
                scale = preferences.getMyPreferenceFloat(c, "fontSize", 42.0f) / (float) 12.0;
                break;
        }
        return scale;
    }

    float getStageScaleValue(float x, float y) {
        return Math.min(x, y);
    }

    RelativeLayout preparePerformanceBoxView(Context c, int lyricsTextColor, int padding) {
        RelativeLayout boxbit = new RelativeLayout(c);
        LinearLayout.LayoutParams llp = linearlayout_params();
        llp.setMargins(0, 0, 0, 0);
        boxbit.setLayoutParams(llp);
        boxbit.setBackgroundResource(R.drawable.lyrics_box);
        GradientDrawable drawable = (GradientDrawable) boxbit.getBackground();
        drawable.setColor(StaticVariables.transparent); // Makes the box transparent
        if (hideLyricsBox) {
            drawable.setStroke(1, StaticVariables.transparent); // set stroke width and transparent stroke
        } else {
            drawable.setStroke(1, lyricsTextColor); // set stroke width and stroke color
        }
        drawable.setCornerRadius(padding);
        padding = padding - ((int) (padding - ((float) padding / 6.0f)) / 2);
        boxbit.setPadding(padding, padding, padding, padding);
        return boxbit;
    }

    LinearLayout prepareProjectedBoxView(Context c, Preferences preferences, int lyricsTextColor, int padding) {
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

    LinearLayout prepareStageBoxView(Context c, int lyricsTextColor, int padding) {
        LinearLayout boxbit = new LinearLayout(c);
        LinearLayout.LayoutParams llp = linearlayout_params();
        llp.setMargins(0, 0, 0, padding);
        boxbit.setLayoutParams(llp);
        boxbit.setBackgroundResource(R.drawable.lyrics_box);
        GradientDrawable drawable = (GradientDrawable) boxbit.getBackground();
        drawable.setColor(StaticVariables.transparent);  // Makes the box transparent
        if (hideLyricsBox) {
            drawable.setStroke(1, StaticVariables.transparent); // set stroke width and transparent stroke
        } else {
            drawable.setStroke(1, lyricsTextColor); // set stroke width and stroke color
        }
        drawable.setCornerRadius(padding);
        padding = padding - ((int) (padding - ((float) padding / 6.0f)) / 2);
        boxbit.setPadding(padding, padding, padding, padding);
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

            // IV - Try to generate a capo/key/tempo/time line
            String sprefix = ";";

            if (displayCapoChords) {
                if (!StaticVariables.mCapo.equals("") && !StaticVariables.mCapo.equals("0")) {
                    // If we are using a capo, add the capo display
                    songInformation.append(sprefix).append("Capo: ");
                    sprefix = " | ";
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
                    if (!StaticVariables.mKey.equals("")) {
                        songInformation.append(" (").append(transpose.capoTranspose(c, preferences, StaticVariables.mKey)).append(")");
                    }
                }
            }

            if (!StaticVariables.mKey.equals("")) {
                songInformation.append(sprefix).append(c.getResources().getString(R.string.edit_song_key)).append(": ").append(StaticVariables.mKey);
                sprefix = " | ";
            }
            if (!StaticVariables.mTempo.equals("")) {
                songInformation.append(sprefix).append(c.getResources().getString(R.string.edit_song_tempo)).append(": ").append(StaticVariables.mTempo);
                sprefix = " | ";
            }
            if (!StaticVariables.mTimeSig.equals("")) {
                songInformation.append(sprefix).append(c.getResources().getString(R.string.edit_song_timesig)).append(": ").append(StaticVariables.mTimeSig);
                sprefix = " | ";
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
            FullscreenActivity.myLyrics = FullscreenActivity.myLyrics + "\n[F__1]\n" + footerInformation.toString();
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
                    if ((longString.substring(Integer.parseInt(positions[i])).startsWith("(")) && (endpos > startpos)) {
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
            highlighterfile = StaticVariables.whichSongFolder.replace("../", "").replace("/", "_") + "_" + StaticVariables.songfilename;
        } else {
            highlighterfile = StaticVariables.whichSongFolder.replace("/", "_") + "_" + StaticVariables.songfilename;
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
        if (received.size() >= 4) {
            // 4th bit (index of 3 though!) is the xml
            String fixed = received.get(3);
            fixed = fixed
                    .replace("\\n", "$$__$$")
                    .replace("\\", "")
                    .replace("$$__$$", "\n");
            received.add(3, fixed);
        }
        return received;
    }

    public int getNearbySection(String incoming) {
        int i = -1;
        if (incoming != null && incoming.length() > 0 && incoming.contains("___section___")) {
            incoming = incoming
                    .replace("{\"description\":\"", "")
                    .replace("\"}", "")
                    .replace("___section___", "");
            try {
                i = Integer.parseInt(incoming);
            } catch (Exception e) {
                i = -1;
            }
        }
        return i;
    }

    void getPreferences(Context c, Preferences preferences) {
        // IV - To avoid repeated preference loads during section display load into local variables
        addSectionSpace = preferences.getMyPreferenceBoolean(c, "addSectionSpace", true);
        blockShadow = preferences.getMyPreferenceBoolean(c, "blockShadow", false);
        blockShadowAlpha = preferences.getMyPreferenceFloat(c, "blockShadowAlpha", 0.7f);
        displayBoldChordsHeadings = preferences.getMyPreferenceBoolean(c, "displayBoldChordsHeadings", false);
        displayCapoAndNativeChords = preferences.getMyPreferenceBoolean(c, "displayCapoAndNativeChords", false);
        displayCapoChords = preferences.getMyPreferenceBoolean(c, "displayCapoChords", true);
        displayChords = preferences.getMyPreferenceBoolean(c, "displayChords", true);
        displayLyrics = preferences.getMyPreferenceBoolean(c, "displayLyrics", true);
        fontSizePreso = preferences.getMyPreferenceFloat(c, "fontSizePreso", 14.0f);
        hideLyricsBox = preferences.getMyPreferenceBoolean(c, "hideLyricsBox", false);
        lineSpacing = preferences.getMyPreferenceFloat(c, "lineSpacing", 0.1f);
        presoAutoScale = preferences.getMyPreferenceBoolean(c, "presoAutoScale", true);
        presoLyricsAlign = preferences.getMyPreferenceInt(c, "presoLyricsAlign", Gravity.CENTER);
        presoLyricsBold = preferences.getMyPreferenceBoolean(c, "presoLyricsBold", false);
        presoShowChords = preferences.getMyPreferenceBoolean(c, "presoShowChords", true);
        scaleChords = preferences.getMyPreferenceFloat(c, "scaleChords", 1.0f);
        scaleComments = preferences.getMyPreferenceFloat(c, "scaleComments", 0.8f);
        trimLines = Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && preferences.getMyPreferenceBoolean(c, "trimLines", false);
        typefaceLyrics = StaticVariables.typefaceLyrics;
        typefaceChords = StaticVariables.typefaceChords;
        typefacePreso = StaticVariables.typefacePreso;
        typefaceMono = StaticVariables.typefaceMono;
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
