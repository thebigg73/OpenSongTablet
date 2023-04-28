package com.garethevans.church.opensongtablet.songprocessing;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialEditText;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class ProcessSong {

    public ProcessSong(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    // The variables used for repeated song processing
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "ProcessSong", groupline_string="____groupline____",
        newline_string="___NEWLINE___", columnbreak_string="::CBr::";
    private final float defFontSize = 8.0f;
    private boolean addSectionSpace;
    //private boolean addSectionBox;
    private boolean blockShadow;
    private boolean displayBoldChordsHeadings;
    private boolean displayBoldChorus;
    private boolean displayChords;
    private boolean displayLyrics;
    private boolean displayCapoChords;
    private boolean displayCapoAndNativeChords;
    private boolean trimWordSpacing;
    private boolean songAutoScaleColumnMaximise;
    private boolean songAutoScaleOverrideFull;
    private boolean songAutoScaleOverrideWidth;
    private boolean trimLines;
    private boolean trimSections;
    private boolean multiLineVerseKeepCompact;
    private boolean multilineSong;
    private boolean forceColumns;
    private boolean makingScaledScreenShot;
    private float fontSize, fontSizeMax, fontSizeMin, blockShadowAlpha,
            lineSpacing, scaleHeadings, scaleChords, scaleComments;
    private String songAutoScale;
    // Stuff for resizing/scaling
    private int padding = 8, primaryScreenColumns=1;
    private boolean bracketsOpen = false;
    private int bracketsStyle = Typeface.NORMAL;
    private boolean curlyBrackets = true;


    public static int getColorWithAlpha(int color, float ratio) {
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(alpha, r, g, b);
    }

    // Update the processing preferences
    public void updateProcessingPreferences() {
        addSectionSpace = mainActivityInterface.getPreferences().getMyPreferenceBoolean("addSectionSpace", true);
        blockShadow = mainActivityInterface.getPreferences().getMyPreferenceBoolean("blockShadow", false);
        blockShadowAlpha = mainActivityInterface.getPreferences().getMyPreferenceFloat("blockShadowAlpha", 0.7f);
        displayCapoChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayCapoChords", true);
        displayCapoAndNativeChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayCapoAndNativeChords", false);
        displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayChords", true);
        displayLyrics = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayLyrics", true);
        displayBoldChordsHeadings = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayBoldChordsHeadings", false);
        displayBoldChorus = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayBoldChorus",false);
        songAutoScale = mainActivityInterface.getPreferences().getMyPreferenceString("songAutoScale", "W");
        songAutoScaleColumnMaximise = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songAutoScaleColumnMaximise", true);
        songAutoScaleOverrideFull = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songAutoScaleOverrideFull", true);
        songAutoScaleOverrideWidth = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songAutoScaleOverrideWidth", false);
        trimLines = mainActivityInterface.getPreferences().getMyPreferenceBoolean("trimLines", true);
        trimSections = mainActivityInterface.getPreferences().getMyPreferenceBoolean("trimSections", true);
        trimWordSpacing = mainActivityInterface.getPreferences().getMyPreferenceBoolean("trimWordSpacing", true);
        //addSectionBox = mainActivityInterface.getPreferences().getMyPreferenceBoolean("addSectionBox", false);
        fontSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("fontSize", 20f);
        fontSizeMax = mainActivityInterface.getPreferences().getMyPreferenceFloat("fontSizeMax", 50f);
        fontSizeMin = mainActivityInterface.getPreferences().getMyPreferenceFloat("fontSizeMin", 8f);
        lineSpacing = mainActivityInterface.getPreferences().getMyPreferenceFloat("lineSpacing", 0.1f);
        scaleHeadings = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleHeadings", 0.6f);
        scaleChords = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleChords", 0.8f);
        scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleComments", 0.8f);
        multiLineVerseKeepCompact = mainActivityInterface.getPreferences().getMyPreferenceBoolean("multiLineVerseKeepCompact", false);
        bracketsStyle = mainActivityInterface.getPreferences().getMyPreferenceInt("bracketsStyle",Typeface.NORMAL);
        curlyBrackets = mainActivityInterface.getPreferences().getMyPreferenceBoolean("curlyBrackets",true);
        forceColumns = mainActivityInterface.getPreferences().getMyPreferenceBoolean("forceColumns",true);
    }

    public boolean showingCapo(String capo) {
        return (displayCapoChords || displayCapoAndNativeChords) && capo!=null && !capo.isEmpty();
    }

    // Get some preferences back
    public float getScaleComments() {
        return scaleComments;
    }

    public Song initialiseSong(String newFolder, String newFilename) {
        Song song = new Song();
        song.setFilename(newFilename);
        song.setFolder(newFolder);
        song.setSongid(mainActivityInterface.getCommonSQL().getAnySongId(newFolder, newFilename));
        return song;
    }

    // This deals with creating the song XML file
    public String getXML(Song thisSong) {
        if (thisSong.getEncoding() == null || thisSong.getEncoding().equals("")) {
            thisSong.setEncoding("UTF-8");
        }
        String myNEWXML = "<?xml version=\"1.0\" encoding=\"" + thisSong.getEncoding() + "\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + parseToHTMLEntities(thisSong.getTitle()) + "</title>\n";
        myNEWXML += "  <author>" + parseToHTMLEntities(thisSong.getAuthor()) + "</author>\n";
        myNEWXML += "  <copyright>" + parseToHTMLEntities(thisSong.getCopyright()) + "</copyright>\n";
        myNEWXML += "  <presentation>" + parseToHTMLEntities(thisSong.getPresentationorder()) + "</presentation>\n";
        myNEWXML += "  <hymn_number>" + parseToHTMLEntities(thisSong.getHymnnum()) + "</hymn_number>\n";
        myNEWXML += "  <capo print=\"" + parseToHTMLEntities(thisSong.getCapoprint()) + "\">" +
                parseToHTMLEntities(thisSong.getCapo()) + "</capo>\n";
        myNEWXML += "  <tempo>" + parseToHTMLEntities(thisSong.getTempo()) + "</tempo>\n";
        myNEWXML += "  <time_sig>" + parseToHTMLEntities(thisSong.getTimesig()) + "</time_sig>\n";
        myNEWXML += "  <duration>" + parseToHTMLEntities(thisSong.getAutoscrolllength()) + "</duration>\n";
        myNEWXML += "  <predelay>" + parseToHTMLEntities(thisSong.getAutoscrolldelay()) + "</predelay>\n";
        myNEWXML += "  <ccli>" + parseToHTMLEntities(thisSong.getCcli()) + "</ccli>\n";
        myNEWXML += "  <theme>" + parseToHTMLEntities(thisSong.getTheme()) + "</theme>\n";
        myNEWXML += "  <alttheme>" + parseToHTMLEntities(thisSong.getAlttheme()) + "</alttheme>\n";
        myNEWXML += "  <user1>" + parseToHTMLEntities(thisSong.getUser1()) + "</user1>\n";
        myNEWXML += "  <user2>" + parseToHTMLEntities(thisSong.getUser2()) + "</user2>\n";
        myNEWXML += "  <user3>" + parseToHTMLEntities(thisSong.getUser3()) + "</user3>\n";
        myNEWXML += "  <key>" + parseToHTMLEntities(thisSong.getKey()) + "</key>\n";
        myNEWXML += "  <keyoriginal>" + parseToHTMLEntities(thisSong.getKeyOriginal()) + "</keyoriginal>\n";
        myNEWXML += "  <aka>" + parseToHTMLEntities(thisSong.getAka()) + "</aka>\n";
        myNEWXML += "  <midi>" + parseToHTMLEntities(thisSong.getMidi()) + "</midi>\n";
        myNEWXML += "  <midi_index>" + parseToHTMLEntities(thisSong.getMidiindex()) + "</midi_index>\n";
        myNEWXML += "  <notes>" + parseToHTMLEntities(thisSong.getNotes()) + "</notes>\n";
        myNEWXML += "  <lyrics>" + parseToHTMLEntities(thisSong.getLyrics()) + "</lyrics>\n";
        myNEWXML += "  <pad_file>" + parseToHTMLEntities(thisSong.getPadfile()) + "</pad_file>\n";
        myNEWXML += "  <custom_chords>" + parseToHTMLEntities(thisSong.getCustomchords()) + "</custom_chords>\n";
        myNEWXML += "  <link_youtube>" + parseToHTMLEntities(thisSong.getLinkyoutube()) + "</link_youtube>\n";
        myNEWXML += "  <link_web>" + parseToHTMLEntities(thisSong.getLinkweb()) + "</link_web>\n";
        myNEWXML += "  <link_audio>" + parseToHTMLEntities(thisSong.getLinkaudio()) + "</link_audio>\n";
        myNEWXML += "  <loop_audio>" + parseToHTMLEntities(thisSong.getPadloop()) + "</loop_audio>\n";
        myNEWXML += "  <link_other>" + parseToHTMLEntities(thisSong.getLinkother()) + "</link_other>\n";
        myNEWXML += "  <abcnotation>" + parseToHTMLEntities(thisSong.getAbc()) + "</abcnotation>\n";
        myNEWXML += "  <abctranspose>" + parseToHTMLEntities(thisSong.getAbcTranspose()) + "</abctranspose>\n";

        if (thisSong.getHasExtraStuff()) {
            String extraStuff = mainActivityInterface.getLoadSong().getExtraStuff(thisSong);
            myNEWXML += "  " + extraStuff + "\n";
        }
        myNEWXML += "</song>";
        // Strip out any empty lines
        StringBuilder stringBuilder = new StringBuilder();
        String[] lines = myNEWXML.split("\n");
        for (String line:lines) {
            if (!line.trim().isEmpty()) {
                stringBuilder.append(line).append("\n");
            }
        }
        thisSong.setSongXML(stringBuilder.toString());
        return stringBuilder.toString();
    }

    // These is used when loading and converting songs (ChordPro, badly formatted XML, etc).
    public String parseHTML(String s) {
        try {
            if (s == null) {
                return "";
            }
            s = s.replace("&amp;apos;", "'").
                    replace("&amp;quote;", "\"").
                    replace("&amp;quot;", "\"").
                    replace("&amp;lt;", "<").
                    replace("&amp;gt;", ">").
                    replace("&amp;", "&").
                    replace("&lt;", "<").
                    replace("&gt;", ">").
                    replace("&apos;", "'").
                    replace("&quote;", "\"").
                    replace("&quot;", "\"").
                    replace("&iquest;", "¿").
                    replace("&Agrave;", "À").
                    replace("&agrave;", "à").
                    replace("&Aacute;", "Á").
                    replace("&aacute;", "á").
                    replace("&Acirc;;", "Â").
                    replace("&acirc;;", "â").
                    replace("&Atilde;", "Ã").
                    replace("&atilde;", "ã").
                    replace("&Aring;", "Å").
                    replace("&aring;", "å").
                    replace("&Auml;", "Ä").
                    replace("&auml;", "ä").
                    replace("&AElig;", "Æ").
                    replace("&aelig;", "æ").
                    replace("&Cacute;", "Ć").
                    replace("&cacute;", "ć").
                    replace("&Ccedil;", "Ç").
                    replace("&ccedil;", "ç").
                    replace("&Eacute;", "É").
                    replace("&eacute;", "é").
                    replace("&Ecirc;;", "Ê").
                    replace("&ecirc;;", "ê").
                    replace("&Egrave;", "È").
                    replace("&egrave;", "è").
                    replace("&Euml;", "Ë").
                    replace("&euml;", "ë").
                    replace("&Iacute;", "Í").
                    replace("&iacute;", "í").
                    replace("&Icirc;;", "Î").
                    replace("&icirc;;", "î").
                    replace("&Igrave;", "Ì").
                    replace("&igrave;", "ì").
                    replace("&Iuml;", "Ï").
                    replace("&iuml;", "ï").
                    replace("&Oacute;", "Ó").
                    replace("&oacute;", "ó").
                    replace("&Ocirc;;", "Ô").
                    replace("&ocirc;;", "ô").
                    replace("&Ograve;", "Ò").
                    replace("&ograve;", "ò").
                    replace("&Ouml;", "Ö").
                    replace("&ouml;", "ö").
                    replace("&szlig;", "ß").
                    replace("&Uacute;", "Ú").
                    replace("&uacute;", "ú").
                    replace("&Ucirc;;", "Û").
                    replace("&ucirc;;", "û").
                    replace("&Ugrave;", "Ù").
                    replace("&ugrave;", "ù").
                    replace("&Uuml;", "Ü").
                    replace("&uuml;", "ü").
                    replace("&#039;", "'").
                    replace("&#8217;", "'").
                    replace("�??", "'").
                    replace("�?", "'").
                    replace("�", "'").
                    replace("&nbsp;", " ");

            // If UG has been bad, replace these bits:
            s = s.replace("pre class=\"\"", "");

        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
            s = "";
        }
        return s;
    }

    public String parseToHTMLEntities(String s) {
        try {
            if (s == null) {
                s = "";
            }
            // Make sure all ss are unencoded to start with
            // Now HTML encode everything that needs encoded
            // Protected are < > &
            // Change < to __lt;  We'll later replace the __ with &.  Do this to deal with &amp; separately
            s = s.replace("<", "__lt;");
            s = s.replace("&lt;", "__lt;");

            // Change > to __gt;  We'll later replace the __ with &.  Do this to deal with &amp; separately
            s = s.replace(">", "__gt;");
            s = s.replace("&gt;", "__gt;");

            // Change &apos; to ' as they don't need encoding in this format - also makes it compatible with desktop
            s = s.replace("&apos;", "'");
            //s = s.replace("\'", "'");

            // Change " to __quot;  We'll later replace the __ with &.  Do this to deal with &amp; separately
            s = s.replace("\"", "__quot;");
            s = s.replace("&quot;", "__quot;");

            // Now deal with the remaining ampersands
            s = s.replace("&amp;", "&");  // Reset any that already encoded - all need encoded now
            s = s.replace("&&", "&");     // Just in case we have wrongly encoded old ones e.g. &amp;&quot;
            s = s.replace("&", "&amp;");  // Reencode all remaining ampersands

            // Now replace the other protected encoded entities back with their leading ampersands
            s = s.replace("__lt;", "&lt;");
            s = s.replace("__gt;", "&gt;");
            s = s.replace("__quot;", "&quot;");

        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
            s = "";
        }
        return s;
    }

    public String getSubstring(String startText, String laterStartText, String endText, String searchText) {
        int startPos = -1;
        int laterStartPos;
        int endPos = -1;
        if (searchText!=null) {
            if (startText != null) {
                startPos = searchText.indexOf(startText);
                if (startPos>-1) {
                    startPos = startPos + startText.length();
                }
            }
            if (laterStartText != null && startPos > -1) {
                laterStartPos = searchText.indexOf(laterStartText, startPos);
                if (laterStartPos>-1) {
                    startPos = laterStartPos + laterStartText.length();
                }
            }
            if (endText != null) {
                endPos = searchText.indexOf(endText,startPos);
            }
            if (startPos > 0 && endPos > startPos) {
                // Valid substring
                return searchText.substring(startPos,endPos);
            }
        }
        // Something wasn't right, so return an empty string
        return "";
    }

    public  String removeHTMLTags(String s) {
        s = s.replace("<!--", "");
        s = s.replace("-->", "");
        return s.replaceAll("<.*?>", "");
    }

    public String fixStartOfLines(String lyrics) {
        StringBuilder fixedlyrics = new StringBuilder();
        String[] lines = lyrics.split("\n");

        for (String line : lines) {
            if (line.length() == 0 || !("[;. 123456789-".contains(line.substring(0, 1)))) {
                line = " " + line;
                //} else if (line.matches("^[0-9].*$") && line.length() > 1 && !line.startsWith(".", 1)) {
                // Multiline verse
            }
            if (line.trim().isEmpty()) {
                line = "";
            }
            fixedlyrics.append(line).append("\n");
        }
        return fixedlyrics.toString();
    }

    public String fixLineBreaksAndSlashes(String s) {
        return s.replace("\r\n", "\n").
                replace("\r", "\n").
                replace("\n\n\n", "\n\n").
                replace("&quot;", "\"").
                replace("\\'", "'").
                replace("&quot;", "\"").
                replace("<", "(").
                replace(">", ")").
                replace("&#39;", "'").
                replace("\t", "    ").
                replace("\\'", "'");
    }

    private String fixWordStretch(String s) {
        // This replaces text like hal____low_______ed with hal  - low    -  ed
        // Deal with up to 2-8 underscores.  The rest are just replaced
        if (s.contains("_")) {
            String[] unders   = new String[]{"________","_______","______","_____", "____","___","__","_"};
            String[] replaces = new String[]{"   -    ","   -   ","   -  ","  -  ", " -  "," - ","- ","-"};
            for (int i = 0; i<8; i++) {
                s = s.replace(unders[i],replaces[i]);
            }
        }
        return s;
    }

    public String fixLyricsOnlySpace(String s) {
        // IV - Used for whole lines when displaying lyrics only
        // IV - Remove typical word splits, white space and trim - beautify!
        return s.replaceAll("_", "")
                .replaceAll("\\s+-\\s+", "")
                .replaceAll("\\s{2,}", " ")
                // Fix sentences
                .replace(". ", ".  ")
                .trim();
    }

    public String determineLineTypes(String string) {
        String type;
        if (string.startsWith("[")) {
            type = "heading";
        } else if (string.startsWith(".")) {
            type = "chord";
        } else if (string.startsWith("˄")) {
            type = "capoline";
        } else if (string.startsWith(";")) {
            // Simple test for ; above means that the complex tests below are done only if a comment line
            if (string.startsWith(";__" + c.getResources().getString(R.string.capo))) {
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

    public String howToProcessLines(int linenum, int totallines, String thislinetype, String nextlinetype, String previouslinetype) {
        String what = "null";
        switch (thislinetype) {
            case "chord":
                if (linenum < totallines - 1 && (nextlinetype.equals("lyric") || nextlinetype.equals("comment"))) {
                    what = "chord_then_lyric";
                } else if (totallines == 1 || nextlinetype.equals("") || nextlinetype.equals("chord") || nextlinetype.equals("heading")) {
                    what = "chord_only";
                }
                break;
            case "lyric":
                if (!previouslinetype.equals("chord")) {
                    what = "lyric_no_chord";
                }
                break;
            case "comment":
                if (!previouslinetype.equals("chord")) {
                    what = "comment_no_chord";
                }
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

    public String fixLineLength(String string, int newlength) {
        return String.format("%1$-" + newlength + "s", string);
    }

    public boolean looksLikeGuitarTab(String line) {
        return line.contains("|") && line.contains("--");
    }

    public String fixGuitarTabLine(String line) {
        // Guitar tab line should be like ;e |-1---3  etc.
        if (!line.startsWith(";")) {
            line = ";" + line;
        }
        // Look for position of first |
        if (line.indexOf("|") == 2 && line.length() > 3) {
            // We want this at position 3 (to allow for two character string tunings)
            line = line.substring(0, 2) + " |" + line.substring(3);
        } else if (line.indexOf("|") == 1 && line.length() > 2) {
            // No string tuning has been specified
            line = line.charAt(0) + "   |" + line.substring(2);
        }
        return line;
    }

    public boolean looksLikeHeadingLine(String line) {
        return line.length() < 15 && (line.contains("[") && line.contains("]")) ||
                line.toLowerCase().contains("verse") || line.toLowerCase().contains("chorus");
    }

    public String fixHeadingLine(String line) {
        if (!line.startsWith("[")) {
            line = "[" + line;
        }
        if (!line.contains("]") && !line.endsWith("]")) {
            line = line + "]";
        }
        return line;
    }

    public String[] getChordPositions(String chord, String lyric) {
        ArrayList<String> chordpositions = new ArrayList<>();

        // IV - Set ready for the loop
        boolean thischordcharempty;
        boolean prevchordcharempty = false;
        boolean prevlyriccharempty;
        boolean prevlyricempty = true;

        for (int x = 1; x < (chord.length()); x++) {
            // IV - The first chord char is considered empty
            thischordcharempty = chord.startsWith(" ", x);
            prevlyriccharempty = lyric.startsWith(" ", x - 1);
            prevlyricempty = prevlyricempty && prevlyriccharempty;

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

        return chordpositions.toArray(chordpos);
    }

    public String[] getSections(String string, String[] pos_string) {
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

    public String parseLyrics(Locale locale, Song song) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        String myLyrics = song.getLyrics();

        myLyrics = fixSquareBracketComments(myLyrics);

        // To replace [<Verse>] with [V] and [<Verse> 1] with [V1]
        String languageverseV = c.getResources().getString(R.string.verse);
        String languageverse_lowercaseV = languageverseV.toLowerCase(locale);
        String languageverse_uppercaseV = languageverseV.toUpperCase(locale);

        // To replace [<Chorus>] with [C] and [<Chorus> 1] with [C1]
        String languagechorusC = c.getResources().getString(R.string.chorus);
        String languagechorus_lowercaseC = languagechorusC.toLowerCase(locale);
        String languagechorus_uppercaseC = languagechorusC.toUpperCase(locale);

        myLyrics = myLyrics
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
                .replace("[" + languageverseV + "1]", "[V1]")
                .replace("[" + languageverseV + "2]", "[V2]")
                .replace("[" + languageverseV + "3]", "[V3]")
                .replace("[" + languageverseV + "4]", "[V4]")
                .replace("[" + languageverseV + "5]", "[V5]")
                .replace("[" + languageverseV + "6]", "[V6]")
                .replace("[" + languageverseV + "7]", "[V7]")
                .replace("[" + languageverseV + "8]", "[V8]")
                .replace("[" + languageverseV + "9]", "[V9]")
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

        song.setLyrics(myLyrics);
        return myLyrics;
    }

    private String getLineType(String string) {
        if (string.startsWith(".")) {
            return "chord";
        } else if (string.startsWith("˄")) {
            return "capoline";
        } else if (string.startsWith(";") && string.contains("|")) {
            return "tab";
        } else if (string.startsWith(";")) {
            return "comment";
        } else if (string.startsWith("[")) {
            return "heading";
        } else {
            return "lyric";
        }
    }

    // This is used for preparing the lyrics as views
    // When processing the lyrics, chords+lyrics or chords+comments or multiple chords+chords are processed
    // as groups of lines and returned as a TableLayout containing two or more rows to allow alignment

    private String trimOutLineIdentifiers(String linetype, String string) {
        switch (linetype) {
            case "heading":
                string = beautifyHeading(string);
                if (!mainActivityInterface.getSong().getSongSectionHeadings().contains(string)) {
                    mainActivityInterface.getSong().getSongSectionHeadings().add(string);
                }
                break;
            case "chord":
            case "capoline":
            case "comment":
            case "tab":
                if (string.length() > 0) {
                    string = string.substring(1);
                }
                break;
            case "lyric":
            default:
                if (string.startsWith(" ")) {
                    string = string.replaceFirst(" ", "");
                }
                break;
        }
        return string;
    }

    public String beautifyHeading(String line) {
        boolean annotated = line.contains("-]");

        if (line.equals("")) {
            return "";
        }

        if (annotated) {
            line = line
                    .replace("[", "")
                    .replace(" -]", "")
                    .replace("-]", "")
                    .replace("]", "")
                    .trim();
        } else {
            line = line
                    .replace("[", "")
                    .replace("]", "")
                    .trim();
        }

        // Fix for filtered section labels. ':' is a 'quick exit for non filtered headings
        if (line.contains(":") &&
                (line.contains(":V") || line.contains(":C") || line.contains(":B") ||
                        line.contains(":T") || line.contains(":P"))) {
            line = line.substring(line.indexOf(":") + 1);
        }

        // Look for caps or English tags for non-English app users
        line = replaceBadHeadings(line, "verse", "V");
        line = replaceBadHeadings(line, "prechorus", "P");
        line = replaceBadHeadings(line, "pre-chorus", "P");
        line = replaceBadHeadings(line, "chorus", "C");
        line = replaceBadHeadings(line, "tag", "T");
        line = replaceBadHeadings(line, "bridge", "B");

        // IV - Test 1 char or 2 chars ending 0-9 or 3 chars ending 10
        if (line.length() == 1 ||
                (line.length() == 2 && "123456789".contains(line.substring(1))) ||
                (line.length() == 3 && line.substring(1).equals("10"))) {
            switch (line.substring(0, 1)) {
                case "V":
                    line = removeAnnotatedSections(line).replace("V", c.getResources().getString(R.string.verse) + " ").replace("-", "");
                    break;
                case "T":
                    line = removeAnnotatedSections(line).replace("T", c.getResources().getString(R.string.tag) + " ");
                    break;
                case "C":
                    line = removeAnnotatedSections(line).replace("C", c.getResources().getString(R.string.chorus) + " ");
                    break;
                case "B":
                    line = removeAnnotatedSections(line).replace("B", c.getResources().getString(R.string.bridge) + " ");
                    break;
                case "P":
                    line = removeAnnotatedSections(line).replace("P", c.getResources().getString(R.string.prechorus) + " ");
                    break;
                case "I":
                default:
                    break;
            }
        }
        line = line.replace("[", "").replace("]", "");
        return line.trim();
    }

    private String replaceBadHeadings(String line, String fix, String replacement) {
        if (line.contains(fix) || line.contains(fix.toUpperCase(mainActivityInterface.getLocale()))) {
            line = line.replace(fix + " ", replacement).
                    replace(fix.toUpperCase(mainActivityInterface.getLocale()) + " ", replacement).
                    replace(fix, replacement).
                    replace(fix.toUpperCase(mainActivityInterface.getLocale()), replacement);
        }
        return line;
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

    // Keep only wanted lines and group lines that should be in a table for alignment purposes
    public String filterAndGroupLines(String string, boolean displayChords) {
        // IV - If no content then return an empty string
        if (string == null || string.trim().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String[] lines = string.split("\n");

        // Go through each line and when displaying chords add lines together as groups ('groupline_string' between bits, \n for new group)
        for (int i=0; i<lines.length; i++) {
            if (lines[i].startsWith(".") && displayChords) {
                // This is a chord line = this needs to be part of a group
                sb.append("\n").append(lines[i]).append(groupline_string);
                // If the next line is a lyric or comment add this to the group and stop there
                if (shouldNextLineBeAdded(i + 1, lines, true)) {
                    i++;
                    // IV - Support a useful quirk of v5 that renders a comment line immediately following a chord line as a lyric line
                    // IV - For example a line may have a chords version for repeat. The repeat variant lyrics can be changed to comment, it will not be projected
                    if (lines[i].startsWith(";")) {
                        lines[i] = lines[i].replaceFirst(";"," ");
                    }
                    if (lines[i].startsWith(" ")) {
                        if (displayLyrics) {
                            sb.append(fixWordStretch(lines[i])).append(groupline_string);
                        }
                    } else {
                        sb.append(lines[i]).append(groupline_string);
                    }
                    // Keep going for multiple lines to be added
                    while (shouldNextLineBeAdded(i + 1, lines, false)) {
                        i++;
                        sb.append(fixWordStretch(lines[i])).append(groupline_string);
                    }
                }
            } else if (lines[i].startsWith(" ") && !displayChords && displayLyrics) {
                // IV - When displaying lyrics only - remove typical word splits, white space and trim - beautify!
                sb.append("\n ").append(fixLyricsOnlySpace(lines[i]));
            } else if (lines[i].contains("§") || (displayLyrics && !lines[i].startsWith("."))) {
                // IV - Always add section breaks.  Add other non chord lines when displaying lyrics.
                sb.append("\n").append(fixWordStretch(lines[i]));
            }
        }
        String fixed = sb.toString();

        // IV - Lines are added with leading \n, the first needs to be removed.  We restore section breaks.
        return fixed.replaceFirst("\n","");
    }

    private boolean shouldNextLineBeAdded(int nl, String[] lines, boolean incnormallyricline) {
        if (incnormallyricline) {
            return (nl < lines.length && (lines[nl].startsWith(" ") && !lines[nl].trim().isEmpty() || lines[nl].startsWith(";") ||
                    lines[nl].matches("\\d.*$")));
        } else {
            return (nl < lines.length && (lines[nl].matches("\\d.*$")));
        }
    }

    private TableLayout groupTable(String string, int lyricColor, int chordColor, int capoColor,
                                   int highlightChordColor, boolean presentation, boolean boldText) {
        TableLayout tableLayout = newTableLayout();
        tableLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

        // If we have a capo and want to show capo chords, duplicate and transpose the chord line
        String capoText = mainActivityInterface.getSong().getCapo();
        boolean hasCapo = capoText!=null && !capoText.isEmpty();
        if (hasCapo && (displayCapoChords || displayCapoAndNativeChords)) {
            int capo = Integer.parseInt(capoText);
            String chordbit = string.substring(0,string.indexOf(groupline_string));
            chordbit = mainActivityInterface.getTranspose().transposeChordForCapo(capo,chordbit).replaceFirst(".","˄");
            // Add it back in with a capo identifying this part
            string = chordbit + groupline_string + string;
        }

        boolean applyFixExcessSpaces = (trimWordSpacing || presentation || !mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                (!multiLineVerseKeepCompact && !multilineSong));

        // Split the group into lines
        String[] lines = string.split(groupline_string);

        int minlength = 0;
        for (String line : lines) {
            minlength = Math.max(minlength, line.length());
        }
        // Make it 1 char bigger so the algorithm identifies end chords
        minlength = minlength + 1;

        for (int i = 0; i < lines.length; i++) {
            int length = lines[i].length();
            if (length < minlength) {
                for (int z = 0; z < (minlength - length); z++) {
                    lines[i] += " ";
                }
            }
        }

        // Get the positions of the chords.  Each will be the start of a new section
        // IV - Use getChordPosition logic which improves the layout of chords
        ArrayList<Integer> pos = new ArrayList<>();

        // IV - If we are not displaying chords, handle the line as a whole
        if (!displayChords) {
            pos.add(0);
        } else {
            if (lines.length > 1) {
                String[] chordPos;
                if (lines[0].startsWith("˄") && lines.length>2) {
                    // For capo chords.  lines[1] is the chords, lines[2] the lyrics
                    chordPos = getChordPositions(lines[0], lines[2]);
                } else if (lines[1].startsWith(".")) {
                    // IV - A chord line follows so position this line referring only to itself
                    chordPos = getChordPositions(lines[0], lines[0]);
                } else {
                    // Standard chord line followed by lyrics
                    chordPos = getChordPositions(lines[0], lines[1]);
                }
                for (String p : chordPos) {
                    // Convert to int arraylist
                    pos.add(Integer.valueOf(p));
                }
            }
        }
        // IV - Add the end of line to positions
        pos.add(minlength);

        String linetype;

        // Now we have the sizes, split into individual TextViews inside a TableRow for each line
        for (int t = 0; t < lines.length; t++) {
            TableRow tableRow = newTableRow();
            if (presentation) {
                tableRow.setGravity(mainActivityInterface.getPresenterSettings().getPresoLyricsAlign());
            }
            linetype = getLineType(lines[t]);

            // Headings with just a comment missed this out
            // Also comments followed by headings
            if (linetype.equals("heading") & lines[t].startsWith("[")) {
                lines[t] = beautifyHeading(lines[t]);
            }
            if (linetype.equals("comment") & lines[t].startsWith(";")) {
                lines[t] = trimOutLineIdentifiers(linetype, lines[t]);
            }

            Typeface typeface = getTypeface(presentation, linetype);
            float size = getFontSize(linetype);
            int color = getFontColor(linetype, lyricColor, chordColor, capoColor);
            int startpos = 0;
            for (int endpos : pos) {
                if (endpos != 0 && endpos>startpos && endpos<lines[t].length() + 1) {
                    TextView textView = newTextView(linetype, typeface, size, color);
                    String str = lines[t].substring(startpos, endpos);
                    if (startpos == 0) {
                        str = trimOutLineIdentifiers(linetype, str);
                    }
                    // If this is a chord line that either has highlighting, or needs to to include capo chords
                    // We process separately, otherwise it is handled in the last default 'else'
                    switch (linetype) {
                        case "chord":
                            // Only show this if we want chords and if there is a capo, we want both capo and native
                            if (displayChords && (!hasCapo || displayCapoAndNativeChords || !displayCapoChords)) {
                                if (highlightChordColor != 0x00000000) {
                                    textView.setText(new SpannableString(highlightChords(str,
                                            highlightChordColor)));
                                } else {
                                    textView.setText(str);
                                }
                            } else {
                                textView = null;
                            }
                            break;
                        case "capoline":
                            // Only show this if we want chords and if there is a capo and showcapo
                            if (displayChords && hasCapo && (displayCapoChords || displayCapoAndNativeChords)) {
                                if (highlightChordColor != 0x00000000) {
                                    textView.setText(new SpannableString(highlightChords(str,
                                            highlightChordColor)));
                                } else {
                                    textView.setText(str);
                                }
                            } else {
                                textView = null;
                            }
                            break;
                        case "lyric":
                            if (displayLyrics) {
                                str = str.replace("_","");
                                str = str.replaceAll("[|]"," ");
                                if (!displayChords) {
                                    // IV - Remove typical word splits, white space and beautify!
                                    str = fixLyricsOnlySpace(str);
                                } else {
                                    if (applyFixExcessSpaces) {
                                        str = fixExcessSpaces(str);
                                    }
                                }
                                SpannableStringBuilder spannableString = getSpannableBracketString(str);
                                if (boldText) {
                                    textView.setPaintFlags(textView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                                    textView.setTypeface(textView.getTypeface(),Typeface.BOLD);
                                }
                                textView.setText(spannableString);
                            } else {
                                textView = null;
                            }
                            break;
                        default:
                            // Just set the text
                            if (applyFixExcessSpaces) {
                                str = fixExcessSpaces(str);
                            }
                            SpannableStringBuilder spannableString = getSpannableBracketString(str);
                            textView.setText(spannableString);
                            break;
                    }
                    if (textView!=null) {
                        tableRow.addView(textView);
                    }
                    startpos = endpos;
                }
            }
            tableLayout.addView(tableRow);
        }
        return tableLayout;
    }

    private SpannableStringBuilder getSpannableBracketString(String str) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
        if (bracketsStyle!= Typeface.NORMAL) {
            try {
                if (bracketsOpen && !str.contains((")"))) {
                    // All spannable adjusted
                    spannableStringBuilder.setSpan(new android.text.style.StyleSpan(bracketsStyle), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (str.contains("(") && str.contains(")") && str.indexOf(")") > str.indexOf("(")) {
                    // Replace the text inside the brackets as spannable italics
                    spannableStringBuilder.setSpan(new android.text.style.StyleSpan(bracketsStyle), str.indexOf("("), str.indexOf(")") + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (str.contains("(") && !str.contains(")")) {
                    // Everything after ( is spannable adjusted and mark flag waiting for closing bracket
                    bracketsOpen = true;
                    spannableStringBuilder.setSpan(new android.text.style.StyleSpan(bracketsStyle), str.indexOf("("), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (str.contains(")") && !str.contains("(")) {
                    // Everything up to ) is spannable adjusted and close flag
                    bracketsOpen = false;
                    spannableStringBuilder.setSpan(new android.text.style.StyleSpan(bracketsStyle), 0, str.indexOf(")") + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return spannableStringBuilder;
    }

    private Spannable highlightChords(String str, int highlightChordColor) {
        // Draw the backgrounds to the chord(s)
        Spannable span = new SpannableString(str);
        str = str + " ";
        int start = 0;
        int end = str.indexOf(" ");
        while (end > -1) {
            span.setSpan(new BackgroundColorSpan(highlightChordColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // IV - Move startPos past spaces to the next non space
            start = end;
            while (str.substring(start + 1).startsWith(" ")) {
                start = start + 1;
            }
            start = start + 1;
            // IV - See if we have more work
            end = str.indexOf(" ", start);
        }
        return span;
    }

    private boolean isMultiLineFormatSong(String string) {
        // Best way to determine if the song is in multiline format is
        // Look for lines starting with 1 and 2 and lines starting [v] or [c] case insensitive
        if (string == null || string.isEmpty()) {
            return false;
        }
        try {
            String lString = "\n" + string.toLowerCase(mainActivityInterface.getLocale());
            return (lString.contains("\n1") && lString.contains("\n2")) &&
                    (lString.contains("\n[v]") || lString.contains("\n[c]"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean lineIsChordForMultiline(String[] lines) {
        return (lines[0].length() > 1 && lines.length > 1 && lines[1].matches("\\d.*$"));
    }

    private String fixMultiLineFormat(String string, boolean presentation) {
        multilineSong = isMultiLineFormatSong(string);
        if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) || presentation ||
                (!multiLineVerseKeepCompact && multilineSong)) {
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
                        verse[vnum] += " " + lines[z].substring(1) + "\n";
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
        } else if (multiLineVerseKeepCompact && multilineSong) {
            // Multiline format, but we want to keep it compact
            // Add ')  ' after the lines starting with numbers and spaces after the chord identifier
            StringBuilder fixedLines = new StringBuilder();
            String[] lines = string.split("\n");
            for (String line:lines) {
                if (line.startsWith(".")) {
                    line = line.replaceFirst(".",".   ");
                } else if (line.length()>0 && "123456789".contains(line.substring(0,1))) {
                    line = line.charAt(0) + ")  " + line.substring(1);
                }
                fixedLines.append(line).append("\n");
            }
            return fixedLines.toString();
        } else {
            // Not multiline format, or not wanting to expand it
            return string;
        }
    }

    private boolean isMultiLine(String l, String l_1, String l_2, String type) {
        boolean isit = false;
        l = l.toLowerCase(mainActivityInterface.getLocale());

        if (l.startsWith("[" + type + "]") &&
                (l_1.startsWith("1") || l_2.startsWith("1"))) {
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
                    if (!section[x+1].startsWith(".") && !section[x+1].startsWith("[") &&
                            !section[x+1].startsWith(";") && !section[x+1].startsWith(" ")) {
                        replacementtext.append(chordlines[x]).append("\n").append(" ").append(section[x + 1]).append("\n");
                    } else {
                        replacementtext.append(chordlines[x]).append("\n").append(section[x + 1]).append("\n");
                    }
                }
                replacementtext.append("\n");
            } else {
                replacementtext.append(sections).append("\n");
            }
        }
        return replacementtext.toString();
    }

    public void matchPresentationOrder(Song song) {
        // presentationOrder probably looks like "Intro V1 V2 C V3 C C Guitar Solo C Outro"
        // We need to identify the sections in the song that are in here
        // What if sections aren't in the song (e.g. Intro V2 and Outro)
        // The other issue is that custom tags (e.g. Guitar Solo) can have spaces in them

        if (mainActivityInterface.getPresenterSettings().getUsePresentationOrder() &&
                song.getPresentationorder() != null && !song.getPresentationorder().isEmpty() &&
            !(multiLineVerseKeepCompact && isMultiLineFormatSong(song.getLyrics()))) {
            try {
                // Update to match the presentation order
                ArrayList<String> newSections = new ArrayList<>();
                ArrayList<String> newHeaders = new ArrayList<>();

                StringBuilder tempPresentationOrder = new StringBuilder(song.getPresentationorder() + " ");
                StringBuilder errors = new StringBuilder();

                // Go through each tag in the song
                for (String tag : song.getSongSectionHeadings()) {
                    if (tag.equals("") || tag.equals(" ")) {
                        Log.d(TAG, "Empty search");
                    } else if (tempPresentationOrder.toString().contains(tag)) {
                        tempPresentationOrder = new StringBuilder(tempPresentationOrder.toString().
                                replace(tag + " ", "<__" + tag + "__>"));
                    } else {
                        // IV - this logic avoids a trailing new line
                        if (errors.length() > 0) {
                            errors.append(("\n"));
                        }
                        // We have sections in the song we haven't included
                        errors.append(tag).append(" - ").append(c.getString(R.string.section_not_used)).append("\n");
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
                            errors.append(tempPresOrderArray[d]).append(" - ").append(c.getString(R.string.section_not_found));
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
                            errors.append(badbit).append(" - ").append(c.getString(R.string.section_not_found));
                        }
                        // tempPresOrderArray now looks like "", "V1", "C", "V3", "C", "C", "Guitar Solo", "C"
                    }
                }

                // Go through the tempPresOrderArray and add the sections back together
                for (String aTempPresOrderArray : tempPresOrderArray) {
                    if (!aTempPresOrderArray.equals("")) {
                        for (int a = 0; a < song.getSongSectionHeadings().size(); a++) {
                            if (song.getSongSectionHeadings().get(a).trim().equals(aTempPresOrderArray.trim())) {
                                newSections.add(song.getGroupedSections().get(a));
                                newHeaders.add(song.getSongSectionHeadings().get(a));
                            }
                        }
                    }
                }

                // Display any errors as a bottom sheet (may need time to read)
                if (!errors.toString().trim().isEmpty()) {
                    // Use a toast which is less intrusive during live performance - Inform but do not demand a reponse. For example, not using a verse section may be valid.
                    mainActivityInterface.getShowToast().doIt(c.getString(R.string.presentation_order) + ": " + c.getString(R.string.error) + "?");
                    //InformationBottomSheet informationBottomSheet = new InformationBottomSheet(
                    //        c.getString(R.string.presentation_order), errors.toString().trim(),
                    //        c.getString(R.string.edit_song), c.getString(R.string.deeplink_edit));
                    //informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "InformationBottomSheet");
                }

                song.setPresoOrderSongSections(newSections);
                song.setPresoOrderSongHeadings(newHeaders);
            } catch (Exception e) {
                // IV - An error has occurred so return what we have
                song.setPresoOrderSongSections(song.getGroupedSections());
                song.setPresoOrderSongHeadings(song.getSongSectionHeadings());
            }
        } else {
            // Not using presentation order, so just return what we have
            song.setPresoOrderSongSections(song.getGroupedSections());
            song.setPresoOrderSongHeadings(song.getSongSectionHeadings());
        }
    }

    private TextView lineText(String linetype,
                              String string, Typeface typeface, float size, int color,
                              int highlightHeadingColor, int highlightChordColor,
                              boolean presentation, boolean boldText) {
        TextView textView = newTextView(linetype, typeface, size, color);

        boolean applyFixExcessSpaces = (trimWordSpacing || presentation || !mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                (!multiLineVerseKeepCompact && !multilineSong));

        if (presentation) {
            textView.setGravity(mainActivityInterface.getPresenterSettings().getPresoLyricsAlign());
        }
        String str = trimOutLineIdentifiers(linetype, string);
        if (linetype.equals("heading") && highlightHeadingColor != 0x00000000) {
            Spannable span = new SpannableString(str);
            int x = 0;
            int y = str.length();
            span.setSpan(new BackgroundColorSpan(highlightHeadingColor), x, y, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(span);
        } else {
            if (linetype.equals("chord") && highlightChordColor != 0x00000000) {
                textView.setText(highlightChords(str, highlightChordColor));
            } else if (linetype.equals("lyric")) {
                // Just set the text
                str = str.replaceAll("[|_]", " ");
                if (applyFixExcessSpaces) {
                    str = fixExcessSpaces(str);
                }
                SpannableStringBuilder spannableString = getSpannableBracketString(str);
                textView.setText(spannableString);
            } else {
                SpannableStringBuilder spannableString = getSpannableBracketString(str);
                textView.setText(spannableString);
            }
        }
        if (boldText) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            textView.setTypeface(textView.getTypeface(),Typeface.BOLD);
        }
        return textView;
    }

    public String fixExcessSpaces(String str) {
        if (trimWordSpacing) {
            // Encode new lines as something else first
            str = str.replace("\n",newline_string);
            // This removes multiple spaces and returns single spaces
            str = str.replaceAll("\\s+", " ");
            // Now fix sentences
            str = str.replace(". ", ".  ");
            // Return new lines
            str = str.replace(newline_string,"\n");
        }
        return str;
    }

    // Prepare the views
    private void clearAndResetLinearLayout(LinearLayout linearLayout, boolean removeViews) {
        if (linearLayout != null) {
            if (removeViews) {
                linearLayout.removeAllViews();
            }
            linearLayout.setScaleX(1.0f);
            linearLayout.setScaleY(1.0f);
        }
    }

    private void resetRelativeLayout(RelativeLayout relativeLayout) {
        if (relativeLayout != null) {
            relativeLayout.setScaleX(1.0f);
            relativeLayout.setScaleY(1.0f);
        }
    }

    private void columnVisibility(LinearLayout c1, LinearLayout c2, LinearLayout c3, boolean v1, boolean v2, boolean v3) {
        if (v1) {
            c1.setVisibility(View.VISIBLE);
        } else {
            c1.setVisibility(View.GONE);
        }
        if (v2) {
            c2.setVisibility(View.VISIBLE);
        } else {
            c2.setVisibility(View.GONE);
        }
        if (v3) {
            c3.setVisibility(View.VISIBLE);
        } else {
            c3.setVisibility(View.GONE);
        }
    }

    public void processSongIntoSections(Song song, boolean presentation) {
        // First we process the song (could be the loaded song, or a temp song - that's why we take a reference)
        // 1. Get a temporary version of the lyrics (as we are going to process them)
        String lyrics = song.getLyrics();

        // Display any encoded html entities properly
        lyrics = parseHTML(lyrics);

        // 2. Check for multiline verse formatting e.g. [V] 1. 2. etc.
        lyrics = fixMultiLineFormat(lyrics, presentation);

        // 3. Make sure new column/breaks aren't pulled into separate sections
        // Do this by trimming whitespace before them
        if (lyrics.contains("!--")) {
            lyrics = lyrics.replaceAll("\\s+!--","!--");
        }

        // 4. Prepare for line splits: | are relevant to Presenter mode only without chord display
        String lineSplit = " ";
        if (presentation && !mainActivityInterface.getPresenterSettings().getPresoShowChords()) {
            lineSplit = "\n";
        }

        // 5. Prepare for section splits: || are relevant to presentation and Stage mode.
        // If sectionSplit, ║ is used in further processing later.
        String sectionSplit = "";
        if (presentation || mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage))) {
            sectionSplit = "║";
        }

        // 6. Prepare for double new line section split - needed for all except scripture or performance primary (not presentation) screen
        String doubleNewlineSplit = "\n\n";
        if (!mainActivityInterface.getSong().getFolder().contains(c.getResources().getString(R.string.scripture)) &&
                !(mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) && !presentation )) {
            doubleNewlineSplit = "§";
        }

        // 7. Process ||, | and split markers on lyric lines
        // Add a trailing ¶ to force a split behaviour that copes with a trailing new line!
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : (lyrics + "¶").split("\n")) {
            // IV - Use leading \n as we can be certain it is safe to remove later
            stringBuilder.append("\n");
            if (forceColumns && line.contains("!--")) {
                line = line.replace("!--", columnbreak_string);
            }
            if (line.startsWith(" ")) {
                line = line
                        .replace("||", sectionSplit)
                        .replace("|", lineSplit);
            }
            // Add it back up
            stringBuilder.append(line);
        }

         // 8. Handle new sections
        lyrics = stringBuilder.toString()
                .replace("-!!", "")
                // --- Process section markers
                .replace("\n ---", "\n§")
                .replace("\n---", "\n§")
                .replace("\n [", "\n§[")
                .replace("\n[", "\n§[")
                .replace("\n\n", doubleNewlineSplit)
                .replaceAll("§+","§")
                // --- Remove the added leading \n
                .substring(1)
                // --- Remove the added trailing ¶
                .replace("¶","");

        // IV - Compress runs of empty sections created by doubleNewLineSplit into one
        if (doubleNewlineSplit.equals("§")) {
            lyrics = lyrics.replaceAll("§\\s+§","§")
                    .replaceAll("§+","§");
        }

        // 9. Handle || splits
        String[] sections = lyrics.split("§");
        ArrayList<String> songSections = new ArrayList<>();
        String[] thissection;

        for (int x = 0; x < sections.length; x++) {
            if (sections[x] != null) {
                if (sectionSplit.equals("║") && sections[x].contains("║")) {
                    thissection = sections[x].split("\n");
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
                    sections[x] = "";
                    for (String line : thissection) {
                        sections[x] += line + "\n";
                    }

                    // Add to the array, split by the section split
                    Collections.addAll(songSections, sections[x].split("║"));

                } else {
                    // Add to the array
                    songSections.add(sections[x]);
                }
            }
        }

        // 10. Pack up as lyric string.  Carry forward the section header to split sections for Stage and Presenter modes
        StringBuilder fixedlyrics = new StringBuilder();
        String sectionHeader = "";

        for (int x = 0; x < songSections.size(); x++) {
            fixedlyrics.append("\n§");
            if (songSections.get(x).startsWith("[")) {
                // IV - Store the header.  Use an empty header in performance mode.
                if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
                    sectionHeader = "¬";
                } else {
                    sectionHeader = songSections.get(x).substring(0,songSections.get(x).indexOf("]") + 1);
                }
                fixedlyrics.append(songSections.get(x));
            } else {
                fixedlyrics.append(sectionHeader).append("\n").append(("¬"+ songSections.get(x)).replace("¬\n","").replace("¬",""));
            }
        }
        lyrics = fixedlyrics.toString()
                // IV - Content is added with leading \n§, the first needs to be removed
                .replaceFirst("\n§","")
                // IV - Remove (when present) performance mode 'empty' sectionHeader
                .replace("\n§¬\n","\n");

        // 11. Handle section trimming
        // IV - Trim but not if performance primary screen and trimsections is off
        if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))  ||
                (!presentation && trimSections)) {
            lyrics = lyrics
                    // We protect the leading space of lyric lines
                    // --- Simplify empty lyric lines... the replace is needed twice
                    .replace("\n \n","\n\n")
                    .replace("\n \n","\n\n")
                    // --- Replace the leading spaces of lyric lines with ×
                    .replace("\n ","\n×")
                    // Trim to remove leading and trailing whitespace
                    .trim()
                    // Remove whitespace after section header - which will not remove ×
                    .replaceAll("]\\s+","]\n")
                    // --- Revert the protected spaces
                    .replace("×"," ")
                    // Remove whitespace before the section marker
                    .replaceAll("\\s+§","\n\n§");
        }

        // 12. Go through the lyrics and get section headers and add to the song object
        song.setSongSectionHeadings(getSectionHeadings(lyrics));

        // 13. Go through the lyrics, filter lines needed for this mode/display chords combination.
        // Returns wanted line types and group lines that should be in a table for alignment purposes
        if (presentation) {
            lyrics = filterAndGroupLines(lyrics, mainActivityInterface.getPresenterSettings().getPresoShowChords());
        } else {
            lyrics = filterAndGroupLines(lyrics, displayChords);
        }

        // 14. Build the songSections for later recall
        // The song sections are not the views (which can have sections repeated using presentationOrder)
        // The grouped sections are used for alignments

        // IV - Handle empty lyrics and fix a trimmed leading section marker
        if (lyrics.equals("")) {
            lyrics = "\n\n§[]";
        } else if (lyrics.startsWith("§")) {
            lyrics = "\n\n" + lyrics;
        } else if (lyrics.startsWith("\n§")) {
            lyrics = "\n" + lyrics;
        }

        songSections = new ArrayList<>();
        ArrayList<String> groupedSections = new ArrayList<>();

       // Remove a new line added by section processing which is not from the song
        lyrics = lyrics.replace("\n§","§");

        // IV - Ignore empty sections.  Sections which have a header only are needed.
        for (String thisSection : lyrics.split("\n§")) {
            if (thisSection != null && !thisSection.trim().isEmpty()) {
                groupedSections.add(thisSection);
                songSections.add(thisSection.replace(groupline_string, "\n"));
            }
        }

        song.setSongSections(songSections);
        song.setGroupedSections(groupedSections);

        // 15. Put into presentation order when required
        matchPresentationOrder(song);

        // Reset the spannable brackets here as we are just starting processing
        bracketsOpen = false;
    }

    private String fixSquareBracketComments(String lyrics) {
        // For IV - replace comments in lyrics from [..] to (..)
        // May cause issues if songs from UG, etc. have spaces before section names
        String[] lines = lyrics.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        for (String line:lines) {
            if (line.startsWith(" ")) {
                // Not a heading line hopefully...
                line = line.replace("[","(").replace("]",")");
            }
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    public ArrayList<View> setSongInLayout(Song song, boolean asPDF, boolean presentation) {
        ArrayList<View> sectionViews = new ArrayList<>();
        ArrayList<Integer> sectionColors = new ArrayList<>();

        // First we process the song (could be the loaded song, or a temp song - that's why we take a reference)
        processSongIntoSections(song, presentation);

        // IV - Initialise transpose capo key  - might be needed
        mainActivityInterface.getTranspose().capoKeyTranspose();

        if (asPDF && mainActivityInterface.getMakePDF().getIsSetListPrinting()) {
            // This is the set list PDF print.  Items are split by empty section headers []
            // We need to now remove those and trim the section content otherwise the PDF has gaps between lines
            for (int i=0; i<song.getSongSections().size(); i++) {
                song.getSongSections().set(i,song.getSongSections().get(i).replace("[]","").replace(".,",",").trim());
                song.getGroupedSections().set(i,song.getGroupedSections().get(i).replace("[]","").replace(".,",",").trim());
            }
            for (int i=0; i<song.getPresoOrderSongSections().size(); i++) {
                song.getPresoOrderSongSections().set(i,song.getPresoOrderSongSections().get(i).replace("[]","").replace(".,",",").trim());
            }
        }

        // Now we deal with creating the views from the available sections
        int backgroundColor;
        int overallBackgroundColor;
        int textColor;
        if (presentation) {
            backgroundColor = Color.TRANSPARENT;
            textColor = mainActivityInterface.getMyThemeColors().getPresoFontColor();
        } else if (asPDF) {
            backgroundColor = Color.WHITE;
            textColor = Color.BLACK;
        } else {
            backgroundColor = mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor();
            textColor = mainActivityInterface.getMyThemeColors().getLyricsTextColor();
        }
        overallBackgroundColor = backgroundColor;

        for (int sect = 0; sect < song.getPresoOrderSongSections().size(); sect++) {

            String section = song.getPresoOrderSongSections().get(sect);
            if (!section.isEmpty()) {
                boolean isChorusBold = false;
                section = section.replace(columnbreak_string,"");
                if (trimSections) {
                    // IV - End trim only as a section may start with a lyric line and have no header
                    section = ("¬" + section).trim().replace("¬","");
                }
                LinearLayout linearLayout = newLinearLayout(); // transparent color
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                if (presentation) {
                    linearLayout.setGravity(mainActivityInterface.getPresenterSettings().getPresoLyricsAlign());
                }

                if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) && blockShadow) {
                    linearLayout.setBackgroundColor(mainActivityInterface.getMyThemeColors().getPresoShadowColor());
                    linearLayout.setBackgroundColor(getColorWithAlpha(mainActivityInterface.
                            getMyThemeColors().getPresoShadowColor(), blockShadowAlpha));
                }

                // Add this section to the array (so it can be called later for presentation)
                if (!section.trim().isEmpty()) {
                    // Now split by line, but keeping empty ones
                    String[] lines = section.split("\n",-1);
                    for (int l=0; l<lines.length; l++) {
                        String line = lines[l];
                        // IV - Do not process an empty group line or empty header line
                        if (!line.equals(groupline_string) && !line.equals("[]")) {
                            // Get the text stylings
                            String linetype = getLineType(line);
                            boolean notLyricOrChord = linetype.equals("heading") || linetype.equals("comment") || linetype.equals("tab");
                            if (presentation && !mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
                                if (notLyricOrChord) {
                                    // Do not use these lines with the second screen
                                    continue;
                                } else if (curlyBrackets) {
                                    // Android Studio gets confused over escapes here - suggesting removing escapes that break the regex!  Keep lots of escapes to be sure it works!
                                    line = line.replaceAll("\\{.*?\\}", "");
                                }
                            }
                            backgroundColor = overallBackgroundColor;
                            if (!asPDF && !presentation && notLyricOrChord) {
                                int[] colors = getBGColor(line);
                                backgroundColor = colors[0];
                                if (l==0) {
                                    isChorusBold = colors[1]==1 && displayBoldChorus;
                                    Log.d(TAG,"isChorusBold:"+isChorusBold);
                                    overallBackgroundColor = backgroundColor;
                                }
                            }
                            Typeface typeface = getTypeface(presentation, linetype);
                            float size = getFontSize(linetype);
                            if (!asPDF && !presentation) {
                                textColor = getFontColor(linetype, mainActivityInterface.getMyThemeColors().
                                        getLyricsTextColor(), mainActivityInterface.getMyThemeColors().getLyricsChordsColor(),
                                        mainActivityInterface.getMyThemeColors().getLyricsCapoColor());
                            }

                            if (line.contains(groupline_string)) {
                                // Has lyrics and chords
                                if (asPDF) {
                                    linearLayout.addView(groupTable(line, Color.BLACK, Color.BLACK,
                                            Color.BLACK, Color.TRANSPARENT, false, isChorusBold));
                                } else if (presentation) {
                                    linearLayout.addView(groupTable(line,
                                            mainActivityInterface.getMyThemeColors().getPresoFontColor(),
                                            mainActivityInterface.getMyThemeColors().getPresoChordColor(),
                                            mainActivityInterface.getMyThemeColors().getPresoCapoColor(),
                                            mainActivityInterface.getMyThemeColors().getHighlightChordColor(),
                                            true, isChorusBold));
                                } else {
                                    TableLayout tl = groupTable(line,
                                            mainActivityInterface.getMyThemeColors().getLyricsTextColor(),
                                            mainActivityInterface.getMyThemeColors().getLyricsChordsColor(),
                                            mainActivityInterface.getMyThemeColors().getLyricsCapoColor(),
                                            mainActivityInterface.getMyThemeColors().getHighlightChordColor(),
                                            false,isChorusBold);
                                    tl.setBackgroundColor(backgroundColor);
                                    linearLayout.addView(tl);
                                }
                            } else {
                                if (!presentation && !asPDF && (!line.isEmpty() || mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)))) {
                                    // IV - Remove typical word splits, white space and trim - beautify!
                                    // IV - Similar logic is used in other places - if changed find and make changes to all
                                    if (!displayChords) {
                                        // IV - Remove typical word splits, white space and trim - beautify!
                                        line = fixLyricsOnlySpace(line);
                                    }
                                    TextView tv = lineText(linetype, line, typeface,
                                            size, textColor,
                                            mainActivityInterface.getMyThemeColors().getHighlightHeadingColor(),
                                            mainActivityInterface.getMyThemeColors().getHighlightChordColor(), false, isChorusBold);
                                    tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                    tv.setBackgroundColor(backgroundColor);
                                    linearLayout.addView(tv);
                                } else if (!line.isEmpty()) {
                                    // PDF or presentation
                                    // IV - Remove typical word splits, white space and trim - beautify!
                                    // IV - Similar logic is used in other places - if changed find and make changes to all
                                    if (!displayChords) {
                                        // IV - Remove typical word splits, white space and trim - beautify!
                                        line = fixLyricsOnlySpace(line);
                                    }
                                    linearLayout.addView(lineText(linetype, line, typeface,
                                            size, textColor, Color.TRANSPARENT, Color.TRANSPARENT,
                                            presentation, isChorusBold));
                                }
                            }
                        }
                    }

                    // IV - Support add section space feature for stage mode. This is done in column processing for performance mode.
                    if (addSectionSpace & !presentation && mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage)) &&
                            !mainActivityInterface.getMakePDF().getIsSetListPrinting() &&
                            sect != (song.getPresoOrderSongSections().size() - 1)) {
                        linearLayout.addView(lineText("lyric", "", getTypeface(false, "lyric"),
                                getFontSize("lyric") / 2, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT,
                                false, isChorusBold));
                    }

                    linearLayout.setBackgroundColor(overallBackgroundColor);
                    sectionColors.add(overallBackgroundColor);
                    sectionViews.add(linearLayout);
                }
            }
        }

        if (!presentation) {
            mainActivityInterface.setSectionColors(sectionColors);
        }

        return sectionViews;
    }

    // Get properties for creating the views
    private Typeface getTypeface(boolean presentation, String string) {
        if (string.equals("tab")) {
            return mainActivityInterface.getMyFonts().getMonoFont();
        } else if (presentation) {
            return mainActivityInterface.getMyFonts().getPresoFont();
        } else {
            if (string.equals("chord") || string.equals("capoline")) {
                return mainActivityInterface.getMyFonts().getChordFont();
            }
        }
        return mainActivityInterface.getMyFonts().getLyricFont();
    }

    private int getFontColor(String string, int lyricColor, int chordColor, int capoColor) {
        if (string.equals("chord")) {
            return chordColor;
        } else if (string.equals("capoline")) {
            return capoColor;
        } else {
            return lyricColor;
        }
    }

    public float getDefFontSize() {
        return defFontSize;
    }
    private float getFontSize(String string) {
        float f = defFontSize;
        switch (string) {
            case "chord":
            case "capoline":
                f = defFontSize * scaleChords;
                break;
            case "comment":
                f = defFontSize * scaleComments;
                break;
            case "heading":
                f = defFontSize * scaleHeadings;
                break;
        }
        return f;
    }

    private int[] getBGColor(String line) {
        if (line.startsWith(";")) {
            return new int[]{mainActivityInterface.getMyThemeColors().getLyricsCommentColor(), 0};
        } else if (beautifyHeading(line).contains(c.getString(R.string.verse))) {
            return new int[]{mainActivityInterface.getMyThemeColors().getLyricsVerseColor(),0};
        } else if (beautifyHeading(line).contains(c.getString(R.string.prechorus))) {
            return new int[]{mainActivityInterface.getMyThemeColors().getLyricsPreChorusColor(),0};
        } else if (beautifyHeading(line).contains(c.getString(R.string.chorus))) {
            return new int[]{mainActivityInterface.getMyThemeColors().getLyricsChorusColor(),1};
        } else if (beautifyHeading(line).contains(c.getString(R.string.bridge))) {
            return new int[]{mainActivityInterface.getMyThemeColors().getLyricsBridgeColor(),0};
        } else if (beautifyHeading(line).contains(c.getString(R.string.tag))) {
            return new int[]{mainActivityInterface.getMyThemeColors().getLyricsTagColor(),0};
        } else if (beautifyHeading(line).contains(c.getString(R.string.custom))) {
            return new int[]{mainActivityInterface.getMyThemeColors().getLyricsCustomColor(),0};
        } else if (line.contains("[") && line.contains("]")) {
            return new int[]{mainActivityInterface.getMyThemeColors().getLyricsCustomColor(),0};
        } else {
            return new int[]{mainActivityInterface.getMyThemeColors().getLyricsVerseColor(),0};
        }
    }

    // Creating new blank views
    private TableLayout newTableLayout() {
        TableLayout tableLayout = new TableLayout(c);
        tableLayout.setPadding(0, 0, 0, 0);
        tableLayout.setClipChildren(false);
        tableLayout.setClipToPadding(false);
        tableLayout.setDividerPadding(0);
        return tableLayout;
    }

    private TableRow newTableRow() {
        TableRow tableRow = new TableRow(c);
        tableRow.setPadding(0, 0, 0, 0);
        tableRow.setDividerPadding(0);
        return tableRow;
    }

    private LinearLayout newLinearLayout() {
        LinearLayout linearLayout = new LinearLayout(c);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(0, 0, 0, 0);
        linearLayout.setLayoutParams(llp);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(0, 0, 0, 0);
        linearLayout.setClipChildren(false);
        linearLayout.setClipToPadding(false);
        return linearLayout;
    }

    private TextView newTextView(String linetype, Typeface typeface, float size, int color) {
        TextView textView = new TextView(c);
        if (trimLines && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            int trimval;
            if (linetype.equals("chord") || linetype.equals("capoline")) {
                trimval = (int) (size * scaleChords * lineSpacing);
            } else if (linetype.equals("heading")) {
                trimval = (int) (size * scaleHeadings * lineSpacing);
            } else {
                trimval = (int) (size * lineSpacing);
            }
            textView.setPadding(0, -trimval, 0, -trimval);
        } else {
            textView.setPadding(0, 0, 0, 0);
        }
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTextSize(size);
        textView.setTypeface(typeface);
        textView.setTextColor(color);
        textView.setIncludeFontPadding(false);
        if (linetype.equals("heading")) {
            if (displayBoldChordsHeadings) {
                // IV - Fake bold will be applied if the font does not support bold
                textView.setPaintFlags(textView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG | Paint.UNDERLINE_TEXT_FLAG);
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            } else {
                textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        }
        if ((linetype.equals("chord") || linetype.equals("capoline")) && displayBoldChordsHeadings) {
            // IV - Fake bold will be applied if the font does not support bold
            textView.setPaintFlags(textView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        }
        return textView;
    }

    public int getMaxValue(ArrayList<Integer> values, int start, int end) {
        int maxValue = 0;
        if (start > values.size()) {
            start = values.size();
        }
        if (end > values.size()) {
            end = values.size();
        }
        for (int i = start; i < end; i++) {
            maxValue = Math.max(maxValue, values.get(i));
        }
        return maxValue;
    }

    public int getTotal(ArrayList<Integer> values, int start, int end) {
        int total = 0;
        if (start > values.size()) {
            start = values.size();
        }
        if (end > values.size()) {
            end = values.size();
        }
        for (int i = start; i < end; i++) {
            total += values.get(i);
        }
        return total;
    }

    private void setMargins(LinearLayout linearLayout, int leftMargin, int rightMargin) {
        LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
        llp.setMargins(leftMargin, 0, rightMargin, 0);
        linearLayout.setClipToPadding(false);
        linearLayout.setClipChildren(false);
        linearLayout.setLayoutParams(llp);
        linearLayout.setPadding(0, 0, 0, 0);
    }

    public void setMakingScaledScreenShot(boolean makingScaledScreenShot) {
        this.makingScaledScreenShot = makingScaledScreenShot;
    }

    private void scaleView(LinearLayout innerColumn, float scaleSize) {
        // IV - Cope with empty songs!
        if (scaleSize == Double.POSITIVE_INFINITY) {
            scaleSize = 1.0f;
        }
        if (innerColumn!=null) {
            innerColumn.setPivotX(0);
            innerColumn.setPivotY(0);
            innerColumn.setScaleX(scaleSize);
            innerColumn.setScaleY(scaleSize);
        }
    }

    // v6 logic that splits always by the biggest scaling arrangement
    private float[] columnSplitAlgorithm(ArrayList<Integer> sectionWidths, ArrayList<Integer> sectionHeights,
                                         int availableWidth, int availableHeight, String autoScale,
                                         boolean forceColumns, int[] forceColumnInfo, boolean presentation) {
        // An updated algorithm to calculate the best way to split a song into columns

        // TODO IV to check this value - v5 used a -12 weighting (based on number of lines)
        // v6 algorithm uses the actual section widths and heights.
        // This fudge factor is effectively a percentage of the running total subtracted from the best so far
        // This favours more in column 1.  It doesn't need much!!  1% is a little too high.
        float fudgeFactor = 0.005f;

        // Prepare the return float.  [0]=num columns best
        float[] returnFloats = null;

        // Calculate the available widths for 2 and 3 columns
        int availableWidth2 = (int) (((float) availableWidth / 2.0f));
        int availableWidth3 = (int) (((float) availableWidth / 3.0f));

        // If we have chosen to add section space, we need to add this to the bottom of all views
        // Do not add to the last view in a column though!
        int sectionSpace = 0;
        int totalSectionSpace = 0;
        if (!presentation && addSectionSpace && !mainActivityInterface.getMakePDF().getIsSetListPrinting()) {
            sectionSpace = (int) (0.75 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, defFontSize, c.getResources().getDisplayMetrics()));
            if (sectionHeights.size() > 1) {
                totalSectionSpace = sectionSpace * (sectionHeights.size() - 1);
            }
        }

        // Work out the maximum scale allowed by preferred max scale font size
        float maxFontScale = fontSizeMax / defFontSize;

        // Work out the minimum scale allowed by preferred min scale font size
        float minFontScale = fontSizeMin / defFontSize;

        // Firstly, work out the scaling for one column
        int col1_1Width = getMaxValue(sectionWidths, 0, sectionWidths.size());
        int col1_1Height = getTotal(sectionHeights, 0, sectionHeights.size()) + totalSectionSpace;
        float col1_1XScale = (float) availableWidth / (float) col1_1Width;
        float col1_1YScale = (float) availableHeight / (float) col1_1Height;
        float oneColumnScale = Math.min(col1_1XScale, col1_1YScale);

        float col1_2ScaleBest = 0f, col2_2ScaleBest = 0f, twoColumnScale = 0f;
        int columnBreak2 = 0;

        float col1_3ScaleBest = 0f, col2_3ScaleBest = 0f, col3_3ScaleBest = 0f, threeColumnScale = 0f;
        int columnBreak3_a = 0, columnBreak3_b = 0;
        int col1_2Width = 0, col2_2Width = 0, col1_3Width = 0, col2_3Width = 0, col3_3Width = 0;
        int col1_2Height = 0, col2_2Height = 0, col1_3Height = 0, col2_3Height = 0, col3_3Height = 0;

        // Only need to work out 2/3 columns if full autoscaling, or we have force splitpoints
        if (autoScale.equals("Y") || (presentation && mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)))) {

            if (forceColumns && forceColumnInfo != null && forceColumnInfo[0] != 1) {
                if (forceColumnInfo[0] == 2) {
                    // Two columns, so get the widths and heights of both
                    columnBreak2 = forceColumnInfo[1];
                    int totalSectionSpace1_2 = sectionSpace * (columnBreak2 - 1);
                    int totalSectionSpace2_2 = sectionSpace * (sectionWidths.size() - columnBreak2);
                    col1_2Width = getMaxValue(sectionWidths, 0, columnBreak2);
                    col1_2Height = getTotal(sectionHeights, 0, columnBreak2) + totalSectionSpace1_2;
                    col2_2Width = getMaxValue(sectionWidths, columnBreak2, sectionWidths.size());
                    col2_2Height = getTotal(sectionHeights, columnBreak2, sectionHeights.size()) + totalSectionSpace2_2;
                    float scale1X = (float) (availableWidth2 - padding) / (float) col1_2Width;
                    float scale1Y = (float) availableHeight / (float) col1_2Height;
                    col1_2ScaleBest = Math.min(scale1X, scale1Y);
                    float scale2X = (float) (availableWidth2 - padding) / (float) col2_2Width;
                    float scale2Y = (float) availableHeight / (float) col2_2Height;
                    col2_2ScaleBest = Math.min(scale2X, scale2Y);
                    twoColumnScale = Math.min(col1_2ScaleBest, col2_2ScaleBest);

                } else if (forceColumnInfo[0] == 3) {
                    // Three columns, so get the widths and heights of all of them
                    columnBreak3_a = forceColumnInfo[1];
                    columnBreak3_b = forceColumnInfo[2];
                    int totalSectionSpace1_3 = sectionSpace * (columnBreak3_a - 1);
                    int totalSectionSpace2_3 = sectionSpace * (columnBreak3_b - columnBreak3_a - 1);
                    int totalSectionSpace3_3 = sectionSpace * (sectionWidths.size() - columnBreak3_b - 1);
                    col1_3Width = getMaxValue(sectionWidths, 0, columnBreak3_a);
                    col1_3Height = getTotal(sectionHeights, 0, columnBreak3_a) + totalSectionSpace1_3;
                    col2_3Width = getMaxValue(sectionWidths, columnBreak3_a, columnBreak3_b);
                    col2_3Height = getTotal(sectionHeights, columnBreak3_a, columnBreak3_b) + totalSectionSpace2_3;
                    col3_3Width = getMaxValue(sectionWidths, columnBreak3_b, sectionWidths.size());
                    col3_3Height = getTotal(sectionHeights, columnBreak3_b, sectionHeights.size()) + totalSectionSpace3_3;
                    float scale1X = (float) (availableWidth3 - padding) / (float) col1_3Width;
                    float scale1Y = (float) availableHeight / (float) col1_3Height;
                    col1_3ScaleBest = Math.min(scale1X, scale1Y);
                    float scale2X = (float) (availableWidth3 - (2 * padding)) / (float) col2_3Width;
                    float scale2Y = (float) availableHeight / (float) col2_3Height;
                    col2_3ScaleBest = Math.min(scale2X, scale2Y);
                    float scale3X = (float) (availableWidth3 - padding) / (float) col3_3Width;
                    float scale3Y = (float) availableHeight / (float) col3_3Height;
                    col3_3ScaleBest = Math.min(scale3X, scale3Y);
                    threeColumnScale = Math.min(col1_3ScaleBest, Math.min(col2_3ScaleBest, col3_3ScaleBest));
                }
            } else {
                // Not point working out 2 columns unless we have more than 1 section!
                if (sectionWidths.size() > 1) {
                    // Now try the two columns
                    // We start moving the more sections to column 1 from column 2
                    // Each time, recalculate the scaling and keep a record of the best option
                    for (int v = 1; v < sectionWidths.size(); v++) {
                        int totalSectionSpace1_2 = sectionSpace * (v - 1);
                        int totalSectionSpace2_2 = sectionSpace * (sectionWidths.size() - v - 1);
                        int thisWidth1_2 = getMaxValue(sectionWidths, 0, v);
                        int thisHeight1_2 = getTotal(sectionHeights, 0, v) + totalSectionSpace1_2;
                        int thisWidth2_2 = getMaxValue(sectionWidths, v, sectionWidths.size());
                        int thisHeight2_2 = getTotal(sectionHeights, v, sectionHeights.size()) + totalSectionSpace2_2;
                        float scale1 = 0f;
                        float scale2 = 0f;
                        if (thisWidth1_2 > 0 && thisHeight1_2 > 0) {
                            float maxXScale1 = (float) (availableWidth2 - padding) / (float) thisWidth1_2;
                            float maxYScale1 = (float) availableHeight / (float) thisHeight1_2;
                            scale1 = Math.min(maxXScale1, maxYScale1);
                        }
                        if (thisWidth2_2 > 0 && thisHeight2_2 > 0) {
                            float maxXScale2 = (float) (availableWidth2 - padding) / (float) thisWidth2_2;
                            float maxYScale2 = (float) availableHeight / (float) thisHeight2_2;
                            scale2 = Math.min(maxXScale2, maxYScale2);
                        }

                        if (Math.min(scale1, scale2) > twoColumnScale - fudgeFactor * twoColumnScale) {
                            // Improved column split option, so get the values
                            col1_2Width = thisWidth1_2;
                            col1_2Height = thisHeight1_2;
                            col2_2Width = thisWidth2_2;
                            col2_2Height = thisHeight2_2;
                            col1_2ScaleBest = scale1;
                            col2_2ScaleBest = scale2;
                            twoColumnScale = Math.min(scale1, scale2);
                            columnBreak2 = v;
                        }
                    }
                }

                // No point working out 3 columns unless we have 3 or more sections
                if (sectionWidths.size() > 3) {
                    // Now try the three columns
                    // Start with 1 section in column 1, 1 section in column 2 and the rest in column 3
                    // Gradually move column 3 contents into column 2 until only 1 is left in column 3
                    // Then move 2 sections into column 1, 1 into column 2 and the rest in column 3
                    // Repeat...

                    // E.g a 5 section song goes through these steps:
                    // 1 1 3  >  1 2 2  >  1 3 1  >  2 1 2  >  2 2 1  >  3 1 1
                    // Too many sections might really slow this down - we'll see!

                    int c1;
                    int c2 = 1;
                    int c3 = 1;
                    for (c1 = 1; c1 < sectionWidths.size() - c2 - c3; c1++) {
                        for (c3 = sectionWidths.size() - c1 - c2; c3 >= 1; c3--) {
                            int totalSectionSpace1_3 = sectionSpace * (c1 - 1);
                            int totalSectionSpace2_3 = sectionSpace * (c2 - 1);
                            int totalSectionSpace3_3 = sectionSpace * (c3 - 1);
                            int this1_3Width = getMaxValue(sectionWidths, 0, c1);
                            int this2_3Width = getMaxValue(sectionWidths, c1, c1 + c2);
                            int this3_3Width = getMaxValue(sectionWidths, c1 + c2, sectionWidths.size());
                            int this1_3Height = getTotal(sectionHeights, 0, c1) + totalSectionSpace1_3;
                            int this2_3Height = getTotal(sectionHeights, c1, c1 + c2) + totalSectionSpace2_3;
                            int this3_3Height = getTotal(sectionHeights, c1 + c2, sectionHeights.size()) + totalSectionSpace3_3;
                            float scaleX1 = (float) (availableWidth3 - padding) / (float) this1_3Width;
                            float scaleX2 = (float) (availableWidth3 - (2 * padding)) / (float) this2_3Width;
                            float scaleX3 = (float) (availableWidth3 - padding) / (float) this3_3Width;
                            float scaleY1 = (float) (availableHeight) / (float) this1_3Height;
                            float scaleY2 = (float) (availableHeight) / (float) this2_3Height;
                            float scaleY3 = (float) (availableHeight) / (float) this3_3Height;
                            float scale1 = Math.min(scaleX1, scaleY1);
                            float scale2 = Math.min(scaleX2, scaleY2);
                            float scale3 = Math.min(scaleX3, scaleY3);
                            float min = Math.min(scale1, Math.min(scale2, scale3));
                            if (min > threeColumnScale - fudgeFactor * threeColumnScale) {
                                col1_3Width = this1_3Width;
                                col1_3Height = this1_3Height;
                                col2_3Width = this2_3Width;
                                col2_3Height = this2_3Height;
                                col3_3Width = this3_3Width;
                                col3_3Height = this3_3Height;
                                col1_3ScaleBest = scale1;
                                col2_3ScaleBest = scale2;
                                col3_3ScaleBest = scale3;
                                threeColumnScale = min;
                                columnBreak3_a = c1;
                                columnBreak3_b = c1 + c2;
                            }
                            c2++;
                        }
                        c2 = 1;
                    }
                }
            }
        } else {
            // Autoscale turned off - base the scaling on the desired user font size
            oneColumnScale = fontSize / defFontSize;
        }

        // Decide which is the best
        boolean oneColumn = (oneColumnScale >= twoColumnScale && oneColumnScale >= threeColumnScale) || !autoScale.equals("Y");
        boolean twoColumn = (forceColumns && forceColumnInfo != null && forceColumnInfo[0] == 2) ||
                (twoColumnScale > oneColumnScale && twoColumnScale >= threeColumnScale);
        boolean threeColumn = (forceColumns && forceColumnInfo != null && forceColumnInfo[0] == 3) ||
                threeColumnScale > oneColumnScale && threeColumnScale > twoColumnScale;

        if (threeColumn) {
            // Compare with max scaling due to font size allowed
            threeColumnScale = Math.min(maxFontScale, threeColumnScale);
            col1_3ScaleBest = Math.min(maxFontScale, col1_3ScaleBest);
            col2_3ScaleBest = Math.min(maxFontScale, col2_3ScaleBest);
            col3_3ScaleBest = Math.min(maxFontScale, col3_3ScaleBest);

            // If we have override to width, then check we are above the minimum font size
            // If not, we revert to the oneColumn logic
            if (threeColumnScale >= minFontScale || !songAutoScaleOverrideFull) {
                returnFloats = new float[]{3,// Number of columns
                        threeColumnScale,   // Overall best scale
                        columnBreak3_a,     // Break point 1
                        columnBreak3_b,     // Break point 2
                        col1_3ScaleBest,    // Best col 1 scale
                        col1_3Width,        // Column 1 max width
                        col1_3Height,       // Column 1 total height
                        col2_3ScaleBest,    // Best col 2 scale
                        col2_3Width,        // Column 2 max width
                        col2_3Height,       // Column 2 total height
                        col3_3ScaleBest,    // Best col 3 scale
                        col3_3Width,        // Column 3 max width
                        col3_3Height,       // Column 3 total height
                        sectionSpace};      // Section space per view except last in column
            } else {
                oneColumn = true;
                threeColumn = false;
            }

        } else if (twoColumn) {
            // Compare with max scaling due to font size allowed
            twoColumnScale = Math.min(maxFontScale, twoColumnScale);
            col1_2ScaleBest = Math.min(maxFontScale, col1_2ScaleBest);
            col2_2ScaleBest = Math.min(maxFontScale, col2_2ScaleBest);

            // If we have override to width, then check we are above the minimum font size
            // If not, we revert to the oneColumn logic
            if (twoColumnScale >= minFontScale || !songAutoScaleOverrideFull) {
                returnFloats = new float[]{2,// Number of columns
                        twoColumnScale,     // Overall best scale
                        columnBreak2,       // Break point
                        col1_2ScaleBest,    // Best col 1 scale
                        col1_2Width,        // Column 1 max width
                        col1_2Height,       // Column 1 total height
                        col2_2ScaleBest,    // Best col 2 scale
                        col2_2Width,        // Column 2 max width
                        col2_2Height,       // Column 2 total height
                        sectionSpace};      // Section space per view except last in column
            } else {
                oneColumn = true;
                twoColumn = false;
            }
        }

        if (oneColumn && !twoColumn && !threeColumn) {
            // Compare with max scaling due to font size allowed
            if (!presentation) {
                if (autoScale.equals("Y")) {
                    oneColumnScale = Math.min(maxFontScale, oneColumnScale);
                    if (oneColumnScale < minFontScale && songAutoScaleOverrideFull) {
                        autoScale = "W";
                    }
                }

                if (autoScale.equals("W")) {
                    oneColumnScale = Math.min(maxFontScale, (float) availableWidth / (float) col1_1Width);
                    if (oneColumnScale < minFontScale && songAutoScaleOverrideWidth) {
                        oneColumnScale = fontSize / defFontSize;
                    }
                }
            }

            returnFloats = new float[]{1,// Number of columns
                    oneColumnScale, // Overall best scale
                    col1_1Width,    // Column 1 max width
                    col1_1Height,   // Column 1 total height
                    sectionSpace};  // Section space per view except last in column
        }

        return returnFloats;
    }

    // These are called from the VTO listener - draw the stuff to the screen as 1,2 or 3 columns
    // This then returns the best (largest) scaling size as a float
    public float[] addViewsToScreen(ArrayList<View> sectionViews,
                                    ArrayList<Integer> sectionWidths, ArrayList<Integer> sectionHeights,
                                    RelativeLayout pageHolder, LinearLayout songView,
                                    LinearLayout songSheetView, int availableWidth, int availableHeight,
                                    LinearLayout column1, LinearLayout column2, LinearLayout column3,
                                    boolean presentation, DisplayMetrics displayMetrics) {
        updateProcessingPreferences();

        // Now we have all the sizes in, determines the best way to show the song
        // This will be single, two or three columns.  The best one will be the one
        // which gives the best scale size

        // Clear and reset the view's scaling
        resetRelativeLayout(pageHolder);
        clearAndResetLinearLayout(songView, false);
        if (songSheetView!=null) {
            clearAndResetLinearLayout(songSheetView, false);
        }
        clearAndResetLinearLayout(column1, true);
        clearAndResetLinearLayout(column2, true);
        clearAndResetLinearLayout(column3, true);

        // Set the padding and boxpadding from 8dp to px
        float scale = displayMetrics.density;
        padding = (int) (4 * scale);

        // Reset any scaling of the views
        for (int i=0; i<sectionViews.size(); i++) {
            sectionViews.get(i).setScaleX(1f);
            sectionViews.get(i).setScaleY(1f);
        }

        int currentWidth = getMaxValue(sectionWidths, 0, sectionWidths.size());
        int currentHeight = getTotal(sectionHeights, 0, sectionHeights.size());

        // Subtract the height of the songsheetview from the available height.
        int songSheetTitleHeight = 0;
        if (songSheetView!=null &&
                mainActivityInterface.getPreferences().getMyPreferenceBoolean("songSheet", false) &&
                 !mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter))) {
            songSheetTitleHeight = mainActivityInterface.getSongSheetTitleLayout().getHeight();
            // Check the songsheet isn't already attached to a view
            if (mainActivityInterface.getSongSheetTitleLayout().getParent()!=null) {
                ((ViewGroup) mainActivityInterface.getSongSheetTitleLayout().getParent()).removeAllViews();
            }
            songSheetView.addView(mainActivityInterface.getSongSheetTitleLayout());
            songSheetView.setVisibility(View.VISIBLE);
        } else if (songSheetView!=null) {
            songSheetView.setVisibility(View.GONE);
        }

        String thisAutoScale = songAutoScale;

        // A request to force columns (still a maximum of 3 allowed though)
        boolean doForceColumns = false;
        int cols = 1;
        int split1 = -1;
        int split2 = -1;
        ArrayList<Integer> sectionswithbreak = new ArrayList<>();
        if (thisAutoScale.equals("Y") && forceColumns && mainActivityInterface.getSong().getLyrics().contains("!--")) {
            // The song sections will have the page break at the start.
            // Count the columns forced (max of 3 though!!!).
            // Need to count the sections
            for (int x=0; x<mainActivityInterface.getSong().getPresoOrderSongSections().size();x++) {
                String section = mainActivityInterface.getSong().getPresoOrderSongSections().get(x);
                if (section.contains(columnbreak_string)) {
                    sectionswithbreak.add(x+1); // Add afterwards
                    if (split1==-1) {
                        split1 = x+1;
                    } else if (split2==-1) {
                        split2 = x+1;
                    }
                }
            }
            if (!sectionswithbreak.isEmpty()) {
                doForceColumns = true;
                if (sectionswithbreak.size()==1) {
                    cols=2;
                } else {
                    cols=3;  // The max allowed
                }
            }
        }
        int[] forceColumnsInfo = new int[]{cols,split1,split2};

        // Figure out the best option of 1,2 or 3 columns and how to arrange them
        float[] columnInfo = columnSplitAlgorithm(sectionWidths, sectionHeights, availableWidth,
                availableHeight-songSheetTitleHeight,songAutoScale,
                doForceColumns, forceColumnsInfo, presentation);

        if (columnInfo[0]==1) {
            if (availableWidth>currentWidth) {
                currentWidth = availableWidth;
            }
            if (!presentation) {
                // Used by primary screen highlighter file naming
                primaryScreenColumns = 1;
            }
            createOneColumn(sectionViews, sectionWidths, sectionHeights, column1, column2, column3, currentWidth,
                    currentHeight, columnInfo[1], presentation, songSheetTitleHeight, (int)columnInfo[4]);

        } else if (columnInfo[0]==2) {
            // If we have 2 force column tags, use those positions instead
            if (!presentation) {
                // Used by primary screen highlighter file naming
                primaryScreenColumns = 2;
            }
            createTwoColumns(sectionViews, column1, column2,
                    column3, columnInfo, presentation, songSheetTitleHeight);

        } else if (columnInfo[0]==3) {
            if (!presentation) {
                // Used by primary screen highlighter file naming
                primaryScreenColumns = 3;
            }
            createThreeColumns(sectionViews, column1, column2, column3, columnInfo,
                    presentation, songSheetTitleHeight);

        }
        return columnInfo;
    }


    // 1 column stuff
    private void createOneColumn(ArrayList<View> sectionViews, ArrayList<Integer> sectionWidths,
                                 ArrayList<Integer> sectionHeights, LinearLayout column1,
                                 LinearLayout column2, LinearLayout column3, int maxWidth,
                                 int totalHeight, float scaleSize, boolean presentation,
                                 int songSheetTitleHeight, int sectionSpace) {
        // Hide all columns for now
        columnVisibility(column1, column2, column3, false, false, false);

        // Prepare the inner column
        LinearLayout innerCol1 = newLinearLayout();

        // For each section, add it
        for (int i=0; i<sectionViews.size(); i++) {
            View v = sectionViews.get(i);

            v.getLayoutParams().width = maxWidth;

            // If this isn't the last view, add the section space
            if (i!=sectionViews.size()-1) {
                v.setPadding(0,0,0,sectionSpace);
            }
            if (childNotInLinearLayoutParent(v)) {
                innerCol1.addView(v);
            }
        }

        // Now scale the column to the correct sizes
        scaleView(innerCol1, scaleSize);

        // TODO Figure out why!!
        // GE Adding extra height here.
        // It seems to be linked to the display cutout height (on rotation so cutout isn't there it isn't an issue)
        // For testing, this happens on Abba Medley, American Pie (i.e. long songs!)
        innerCol1.setLayoutParams(new LinearLayout.LayoutParams((int)(maxWidth*scaleSize),(int)(totalHeight*scaleSize) + 1000));

        // Sort the margins
        setMargins(innerCol1, 0, 0);
        column1.addView(innerCol1);
        columnVisibility(column1, column2, column3, true, false, false);
        if (!presentation && !makingScaledScreenShot) {
            mainActivityInterface.updateSizes((int) (maxWidth * scaleSize), (int) (totalHeight * scaleSize) + songSheetTitleHeight);
        }
    }

    // 2 column stuff
    private void createTwoColumns(ArrayList<View>sectionViews, LinearLayout column1,
                                  LinearLayout column2, LinearLayout column3,
                                  float[] columnInfo,
                                  boolean presentation, int songSheetTitleHeight) {

        /*
        columnInfo = new float[]{2,         // Number of columns
                        twoColumnScale,     // Overall best scale
                        columnBreak2,       // Break point
                        col1_2ScaleBest,    // Best col 1 scale
                        col1_2Width,        // Column 1 max width
                        col1_2Height,       // Column 1 total height
                        col2_2ScaleBest,    // Best col 2 scale
                        col2_2Width,        // Column 2 max width
                        col2_2Height,       // Column 2 total height
                        sectionSpace}       // Section space
         */
        float bestOverallScale = columnInfo[1];
        int columnBreak2 = (int)columnInfo[2];
        float col1_2ScaleBest = columnInfo[3];
        int col1_2Width = (int)columnInfo[4];
        int col1_2Height = (int)columnInfo[5];
        float col2_2ScaleBest = columnInfo[6];
        int col2_2Width = (int)columnInfo[7];
        int col2_2Height = (int)columnInfo[8];
        int sectionSpace = (int)columnInfo[9];

        // Use 2 columns, but hide them all for now
        columnVisibility(column1, column2, column3, false, false, false);

        // Decide on the scaling for each column
        if (!songAutoScaleColumnMaximise) {
            // Set both columns to the same minimum size
            col1_2ScaleBest = bestOverallScale;
            col2_2ScaleBest = bestOverallScale;
        }

        // Prepare the inner columns
        LinearLayout innerCol1 = newLinearLayout();
        LinearLayout innerCol2 = newLinearLayout();

        // Make the inner column big enough for the unscaled content
        // Need to consider scaling <1.  If I did this now, the views get cropped
        // TODO as with column 1, adding height just in case of cropping
        innerCol1.getLayoutParams().width = col1_2Width;
        innerCol1.getLayoutParams().height = col1_2Height+1000;
        innerCol2.getLayoutParams().width = col2_2Width;
        innerCol2.getLayoutParams().height = col2_2Height+1000;

        // Scale the inner columns
        scaleView(innerCol1, col1_2ScaleBest);
        scaleView(innerCol2, col2_2ScaleBest);

        for (int i = 0; i < columnBreak2; i++) {
            // Make all the views the same width as each other
            sectionViews.get(i).getLayoutParams().width = col1_2Width;
            // If this isn't the last view in the column, add the sectionSpace
            if (i!=columnBreak2-1) {
                sectionViews.get(i).setPadding(0,0,0,sectionSpace);
            }
            // Add the views to the scaled inner column
            if (childNotInLinearLayoutParent(sectionViews.get(i))) {
                innerCol1.addView(sectionViews.get(i));
            }
        }
        for (int i = columnBreak2; i < sectionViews.size(); i++) {
            // Make all the views the same width as each other
            sectionViews.get(i).getLayoutParams().width = col2_2Width;
            // If this isn't the last view in the column, add the sectionSpace
            if (i!=sectionViews.size()-1) {
                sectionViews.get(i).setPadding(0,0,0,sectionSpace);
            }
            // Add the views to the scaled inner column
            if (childNotInLinearLayoutParent(sectionViews.get(i))) {
                innerCol2.addView(sectionViews.get(i));
            }
        }

        // Now the view has been drawn, scale the innerCols
        scaleView(innerCol1, col1_2ScaleBest);
        scaleView(innerCol2, col2_2ScaleBest);

        // Resize the holding column height to the correct size
        // Don't adjust the width, as this is set by weights in the layout
        int col1h = (int) (col1_2Height*col1_2ScaleBest);
        int col2h = (int) (col2_2Height*col2_2ScaleBest);
        // TODO as with column 1 adding extra height
        column1.getLayoutParams().height = col1h+1000;
        column2.getLayoutParams().height = col2h+1000;

        columnVisibility(column1, column2, column3, true, true, false);
        column1.addView(innerCol1);
        column2.addView(innerCol2);

        // Set the margins
        setMargins(innerCol1, 0, padding);
        setMargins(innerCol2, padding, 0);

        if (!presentation && !makingScaledScreenShot) {
            mainActivityInterface.updateSizes(mainActivityInterface.getDisplayMetrics()[0],
                    Math.max(col1h,col2h) + songSheetTitleHeight);
        }
    }

    // 3 column stuff
    private void createThreeColumns(ArrayList<View>sectionViews, LinearLayout column1,
                                    LinearLayout column2, LinearLayout column3, float[] columnInfo,
                                    boolean presentation, int songSheetTitleHeight) {
        /*columnInfo new float[]{3,          // Number of columns
        1                threeColumnScale,   // Overall best scale
        2                columnBreak3_a,     // Break point 1
        3                columnBreak3_b,     // Break point 2
        4                col1_3ScaleBest,    // Best col 1 scale
        5                col1_3Width,        // Column 1 max width
        6                col1_3Height,       // Column 1 total height
        7                col2_3ScaleBest,    // Best col 2 scale
        8                col2_3Width,        // Column 2 max width
        9                col2_3Height,       // Column 2 total height
        10               col3_3ScaleBest,    // Best col 3 scale
        11               col3_3Width,        // Column 3 max width
        12               col3_3Height,       // Column 3 total height
        13               sectionSpace};      // Section space per view except last in column */

        float bestOverallScale = columnInfo[1];
        int columnBreak3_a = (int)columnInfo[2];
        int columnBreak3_b = (int)columnInfo[3];
        float col1_3ScaleBest = columnInfo[4];
        int col1_3Width = (int)columnInfo[5];
        int col1_3Height = (int)columnInfo[6];
        float col2_3ScaleBest = columnInfo[7];
        int col2_3Width = (int)columnInfo[8];
        int col2_3Height = (int)columnInfo[9];
        float col3_3ScaleBest = columnInfo[10];
        int col3_3Width = (int)columnInfo[11];
        int col3_3Height = (int)columnInfo[12];
        int sectionSpace = (int)columnInfo[13];

        // Use 3 columns, but hide them all for now
        columnVisibility(column1, column2, column3, false, false, false);

        if (!songAutoScaleColumnMaximise) {
            // Set both columns to the same minimum size
            col1_3ScaleBest = bestOverallScale;
            col2_3ScaleBest = bestOverallScale;
            col3_3ScaleBest = bestOverallScale;
        }

        // Prepare the inner columns
        LinearLayout innerCol1 = newLinearLayout();
        LinearLayout innerCol2 = newLinearLayout();
        LinearLayout innerCol3 = newLinearLayout();

        // Make the inner column big enough for the unscaled content
        // Need to consider scaling <1.  If I did this now, the views get cropped
        // TODO as with column 1 adding extra height
        innerCol1.getLayoutParams().width = col1_3Width;
        innerCol1.getLayoutParams().height = col1_3Height+1000;
        innerCol2.getLayoutParams().width = col2_3Width;
        innerCol2.getLayoutParams().height = col2_3Height+1000;
        innerCol3.getLayoutParams().width = col3_3Width;
        innerCol3.getLayoutParams().height = col3_3Height+1000;

        for (int i = 0; i < columnBreak3_a; i++) {
            // Make all the views the same width as each other
            sectionViews.get(i).getLayoutParams().width = col1_3Width;
            // If this isn't the last view in the column, add the sectionSpace
            if (i!=columnBreak3_a-1) {
                sectionViews.get(i).setPadding(0,0,0,sectionSpace);
            }
            // Add the views to the scaled inner column
            if (childNotInLinearLayoutParent(sectionViews.get(i))) {
                innerCol1.addView(sectionViews.get(i));
            }
        }
        for (int i = columnBreak3_a; i<columnBreak3_b; i++) {
            // Make all the views the same width as each other
            sectionViews.get(i).getLayoutParams().width = col2_3Width;
            // If this isn't the last view in the column, add the sectionSpace
            if (i!=columnBreak3_b-1) {
                sectionViews.get(i).setPadding(0,0,0,sectionSpace);
            }
            // Add the views to the scaled inner column
            if (childNotInLinearLayoutParent(sectionViews.get(i))) {
                innerCol2.addView(sectionViews.get(i));
            }
        }
        for (int i = columnBreak3_b; i < sectionViews.size(); i++) {
            // Make all the views the same width as each other
            sectionViews.get(i).getLayoutParams().width = col3_3Width;
            // If this isn't the last view in the column, add the sectionSpace
            if (i!=sectionViews.size()-1) {
                sectionViews.get(i).setPadding(0,0,0,sectionSpace);
            }
            // Add the views to the scaled inner column
            if (childNotInLinearLayoutParent(sectionViews.get(i))) {
                innerCol3.addView(sectionViews.get(i));
            }
        }

        // Now the view has been drawn, scale the innerCols
        scaleView(innerCol1, col1_3ScaleBest);
        scaleView(innerCol2, col2_3ScaleBest);
        scaleView(innerCol3, col3_3ScaleBest);

        // Resize the holding column height to the correct size
        // Don't adjust the width, as this is set by weights in the layout
        int col1h = (int) (col1_3Height*col1_3ScaleBest);
        int col2h = (int) (col2_3Height*col2_3ScaleBest);
        int col3h = (int) (col3_3Height*col3_3ScaleBest);
        // TODO as with column 1 adding extra height
        innerCol1.getLayoutParams().height = col1h+1000;
        innerCol2.getLayoutParams().height = col2h+1000;
        innerCol3.getLayoutParams().height = col3h+1000;

        columnVisibility(column1, column2, column3, true, true, true);
        column1.addView(innerCol1);
        column2.addView(innerCol2);
        column3.addView(innerCol3);

        // Set the margins
        setMargins(column1, 0, padding);
        setMargins(column2, padding, padding);
        setMargins(column3, padding,0);

        if (!presentation && !makingScaledScreenShot) {
            mainActivityInterface.updateSizes(mainActivityInterface.getDisplayMetrics()[0],
                    Math.max(col1h,Math.max(col2h,col3h)) + songSheetTitleHeight);
        }
    }


    // Now the stuff to read in pdf files (converts the pages to an image for displaying)
    // This uses Android built in PdfRenderer, so will only work on Lollipop+

    private boolean childNotInLinearLayoutParent(View v) {
        if (v!=null) {
            try {
                if (((LinearLayout)v.getParent())!=null) {
                    ((LinearLayout)(LinearLayout) v.getParent()).removeView(v);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Bitmap getBitmapFromPDF(String folder, String filename, int page, int allowedWidth,
                                   int allowedHeight, String scale) {
        Bitmap bmp = null;

        Uri uri;
        if (folder==null) {
            // This is an external file
            uri = mainActivityInterface.getImportUri();
        } else {
            uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, filename);
        }

        // FileDescriptor for file, it allows you to close file when you are done with it
        ParcelFileDescriptor parcelFileDescriptor = getPDFParcelFileDescriptor(uri);

        // Get PDF renderer
        PdfRenderer pdfRenderer = getPDFRenderer(parcelFileDescriptor);

        // Get the page count
        mainActivityInterface.getSong().setPdfPageCount(getPDFPageCount(pdfRenderer));

        // Set the current page number
        page = getCurrentPage(page);

        if (parcelFileDescriptor != null && pdfRenderer != null && mainActivityInterface.getSong().getPdfPageCount() > 0) {
            // Good to continue!

            // Get the currentPDF page
            PdfRenderer.Page currentPage = getPDFPage(pdfRenderer, page);

            // Get the currentPDF size
            ArrayList<Integer> pdfSize = getPDFPageSize(currentPage);

            // Get the scaled sizes for the bitmap
            ArrayList<Integer> bmpSize = getBitmapScaledSize(pdfSize, allowedWidth, allowedHeight, scale);

            // Get a scaled bitmap for these sizes
            bmp = createBitmapFromPage(bmpSize, currentPage, true);

            // Try to close the pdf stuff down to recover memory
            try {
                currentPage.close();
                pdfRenderer.close();
                parcelFileDescriptor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bmp;
    }

    public ParcelFileDescriptor getPDFParcelFileDescriptor(Uri uri) {
        try {
            return c.getContentResolver().openFileDescriptor(uri, "r");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PdfRenderer getPDFRenderer(ParcelFileDescriptor parcelFileDescriptor) {
        try {
            return new PdfRenderer(parcelFileDescriptor);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public int getPDFPageCount(PdfRenderer pdfRenderer) {
        if (pdfRenderer != null) {
            return pdfRenderer.getPageCount();
        } else {
            return 0;
        }
    }

    public int getCurrentPage(int page) {
        if (!mainActivityInterface.getSong().getShowstartofpdf()) {
            // This is to deal with swiping backwards through songs, show the last page first!
            page = mainActivityInterface.getSong().getPdfPageCount() - 1;
            mainActivityInterface.getSong().setShowstartofpdf(true);
            mainActivityInterface.getSong().setCurrentSection(mainActivityInterface.getSong().getPdfPageCount() - 1);
        }
        if (page >= mainActivityInterface.getSong().getPdfPageCount()) {
            mainActivityInterface.getSong().setPdfPageCurrent(0);
            mainActivityInterface.getSong().setCurrentSection(0);
            page = 0;
        } else {
            mainActivityInterface.getSong().setPdfPageCurrent(page);
            mainActivityInterface.getSong().setCurrentSection(page);
        }
        return page;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PdfRenderer.Page getPDFPage(PdfRenderer pdfRenderer, int page) {
        return pdfRenderer.openPage(page);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ArrayList<Integer> getPDFPageSize(PdfRenderer.Page currentPage) {
        ArrayList<Integer> sizes = new ArrayList<>();
        // Get pdf size from page
        int pdfwidth;
        int pdfheight;
        if (currentPage != null) {
            pdfwidth = currentPage.getWidth();
            pdfheight = currentPage.getHeight();
        } else {
            pdfwidth = 1;
            pdfheight = 1;
        }
        sizes.add(pdfwidth);
        sizes.add(pdfheight);
        return sizes;
    }

    public ArrayList<Integer> getBitmapScaledSize(ArrayList<Integer> pdfSize, int allowedWidth, int allowedHeight, String scale) {
        ArrayList<Integer> sizes = new ArrayList<>();

        int bmpwidth;
        int bmpheight;

        float xscale = (float) allowedWidth / (float) pdfSize.get(0);
        float yscale = (float) allowedHeight / (float) pdfSize.get(1);
        float maxscale = Math.min(xscale, yscale);

        switch (scale) {
            case "Y":
                bmpheight = (int) ((float) pdfSize.get(1) * maxscale);
                bmpwidth = (int) ((float) pdfSize.get(0) * maxscale);
                break;

            case "W":
                bmpheight = (int) (xscale * (float) pdfSize.get(1));
                bmpwidth = allowedWidth;
                break;

            default:
                // This means pdf will never be bigger than needed (even if scale is off)
                // This avoids massive files calling out of memory error
                if (pdfSize.get(0) > allowedWidth) {
                    bmpheight = (int) (xscale * (float) pdfSize.get(1));
                    bmpwidth = allowedWidth;
                } else {
                    bmpheight = pdfSize.get(1);
                    bmpwidth = pdfSize.get(0);
                }
                break;
        }
        if (bmpwidth == 0) {
            bmpwidth = 1;
        }
        if (bmpheight == 0) {
            bmpheight = 1;
        }

        sizes.add(bmpwidth);
        sizes.add(bmpheight);
        return sizes;
    }

    public Bitmap invertBitmap (Bitmap src) {
        int height = src.getHeight();
        int width = src.getWidth();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        ColorMatrix matrixGrayscale = new ColorMatrix();
        matrixGrayscale.setSaturation(0);

        ColorMatrix matrixInvert = new ColorMatrix();
        matrixInvert.set(new float[] {
                        -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                        0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                        0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                });
        matrixInvert.preConcat(matrixGrayscale);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrixInvert);
        paint.setColorFilter(filter);

        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Bitmap createBitmapFromPage(ArrayList<Integer> bmpSize, PdfRenderer.Page currentPage, boolean forDisplayOnly) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(bmpSize.get(0), bmpSize.get(1), Bitmap.Config.ARGB_8888);
            // Make a canvas with which we can draw to the bitmap to make it white
            Canvas canvas = new Canvas(bitmap);
            // Fill with white
            canvas.drawColor(0xffffffff);

            // Be aware this pdf might have transparency.  For now, I've just set the background
            // of the image view to white.  This is fine for most PDF files.
            int resolution;
            if (forDisplayOnly) {
                resolution = PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY;
            } else {
                resolution = PdfRenderer.Page.RENDER_MODE_FOR_PRINT;
            }
            try {
                currentPage.render(bitmap, null, null, resolution);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mainActivityInterface.getMyThemeColors().getInvertPDF()) {
                return invertBitmap(bitmap);
            } else {
                return bitmap;
            }

        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Not working or implemented yet.  This will allow trimming images/pdfs by whitespace
    @SuppressWarnings("unused")
    public Bitmap trimBitmap(Bitmap bmp) {
        boolean trimMargins = false;
        if (trimMargins) {
            // TODO Auto-generated method stub
            int color = Color.WHITE | Color.TRANSPARENT;

            long dtMili = System.currentTimeMillis();
            int MTop = 0, MBot = 0, MLeft = 0, MRight = 0;
            boolean found1 = false, found2 = false;

            int[] bmpIn = new int[bmp.getWidth() * bmp.getHeight()];
            int[][] bmpInt = new int[bmp.getWidth()][bmp.getHeight()];

            bmp.getPixels(bmpIn, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
                    bmp.getHeight());

            for (int ii = 0, contX = 0, contY = 0; ii < bmpIn.length; ii++) {
                bmpInt[contX][contY] = bmpIn[ii];
                contX++;
                if (contX >= bmp.getWidth()) {
                    contX = 0;
                    contY++;
                    if (contY >= bmp.getHeight()) {
                        break;
                    }
                }
            }

            for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
                // looking for MTop
                for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
                    if (bmpInt[wP][hP] != color) {
                        Log.e("MTop 2", "Pixel found @" + hP);
                        MTop = hP;
                        found2 = true;
                        break;
                    }
                }
            }
            found2 = false;

            for (int hP = bmpInt[0].length - 1; hP >= 0 && !found2; hP--) {
                // looking for MBot
                for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
                    if (bmpInt[wP][hP] != color) {
                        Log.e("MBot 2", "Pixel found @" + hP);
                        MBot = bmp.getHeight() - hP;
                        found2 = true;
                        break;
                    }
                }
            }
            found2 = false;

            for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
                // looking for MLeft
                for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
                    if (bmpInt[wP][hP] != color) {
                        Log.e("MLeft 2", "Pixel found @" + wP);
                        MLeft = wP;
                        found2 = true;
                        break;
                    }
                }
            }
            found2 = false;

            for (int wP = bmpInt.length - 1; wP >= 0 && !found2; wP--) {
                // looking for MRight
                for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
                    if (bmpInt[wP][hP] != color) {
                        Log.e("MRight 2", "Pixel found @" + wP);
                        MRight = bmp.getWidth() - wP;
                        found2 = true;
                        break;
                    }
                }
            }
            found2 = false;

            int sizeY = bmp.getHeight() - MBot - MTop, sizeX = bmp.getWidth()
                    - MRight - MLeft;

            Bitmap bmp2 = Bitmap.createBitmap(bmp, MLeft, MTop, sizeX, sizeY);
            dtMili = (System.currentTimeMillis() - dtMili);
            Log.e("Margin   2",
                    "Time needed " + dtMili + "mSec\nh:" + bmp.getWidth() + "w:"
                            + bmp.getHeight() + "\narray x:" + bmpInt.length + "y:"
                            + bmpInt[0].length);
            return bmp2;
        } else {
            return bmp;
        }
    }


    // This stuff deals with the highlighter notes
    public String getHighlighterFilename(Song song, boolean portrait, int fakeColumns) {
        // Fake columns are set for manual filename identification, not what the song actually uses at the time
        if (fakeColumns==-1) {
            fakeColumns = primaryScreenColumns;
        }
        // The highlighter song file is encoded as FOLDER_FILENAME_{p or l LANDSCAPE}{if pdf _PAGENUMBER_}.png
        // v6, however now uses the landscape as the column version (historically only written if full autoscale is on)

        // Actually we need the following options
        // p: Single column - use portrait suffix (works for any scale mode or orientation)
        // c: Columns - use when in portrait, but more than one column due to autoscale // NEW
        // l: Landscape when in landscape mode more than one column is used
        String filename = song.getFolder().replace("/", "_") + "_" +
                song.getFilename();

        if (song.getFiletype().equals("PDF")) {
            filename += "_" + song.getPdfPageCurrent();
        } else {
            //if (portrait) {  // old v5 logic which only worked for full autoscale
            if (fakeColumns == 1) {
                // Single column.  Can be any mode or scaling
                filename += "_p";
                // More than one column, but portrait
            } else if (portrait) {
                filename += "_c";
            } else {
                // More than one column, but landscape
                filename += "_l";
            }
        }
        filename += ".png";

        // Ignore any **Variation prefixes (as these aren't included in the highlighter filename)
        // Only done if the variation is a key change
        if ((filename.contains("**Variation") || filename.contains("**"+c.getString(R.string.variation))) && filename.contains("_"+song.getKey()+"_")) {
            filename = filename.replace("**Variation_", "");
            filename = filename.replace("**"+c.getString(R.string.variation),"");
            // Remove any key from the variation, as these aren't included
            filename = filename.replace("_" + song.getKey() + "_", "_");
        }

        return filename;
    }

    public ArrayList<String> getInfoFromHighlighterFilename(String filename) {
        ArrayList<String> bits = new ArrayList<>();
        String[] filebits = filename.split("_");
        int namepos = 1;
        for (int x = 0; x < filebits.length; x++) {
            if (filebits[x].equals("p") || filebits[x].equals("l")) {
                // the pos is before this
                namepos = x - 1;
            }
        }
        if (namepos > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int x = 0; x < namepos; x++) {
                stringBuilder.append(filebits[x]).append("_");
            }
            bits.add(stringBuilder.toString());   // The folder

            stringBuilder = new StringBuilder();
            for (int x = namepos; x < filebits.length; x++) {
                stringBuilder.append(filebits[x]).append("_");
            }
            bits.add(stringBuilder.toString());   // The file

            // Get rid of underscores
            bits.set(0, bits.get(0).substring(0, bits.get(0).lastIndexOf("_")));
            bits.set(1, bits.get(1).substring(0, bits.get(1).lastIndexOf("_")));
        } else {
            bits.add(0, "");
            bits.add(1, filename);
        }
        return bits;

    }

    public Bitmap getPDFHighlighterBitmap(Song song, int w, int h, int pageNum) {
        // The pdf highlighter song file is encoded as FOLDER_FILENAME_PAGENUM.png
        String filename = song.getFolder().replace("/", "_") + "_" +
                song.getFilename() + "_" + pageNum+".png";
        return getHighlighterBitmap(filename, w, h);
    }

    public Bitmap getHighlighterFile(int w, int h) {
        String filename;
        int orientation = c.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            filename = getHighlighterFilename(mainActivityInterface.getSong(), true, -1);
        } else {
            filename = getHighlighterFilename(mainActivityInterface.getSong(), false, -1);
        }
        return getHighlighterBitmap(filename, w, h);
    }

    private Bitmap getHighlighterBitmap(String filename, int w, int h) {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", filename);
        if (mainActivityInterface.getStorageAccess().uriExists(uri)) {
            return getBitmapFromUri(uri, w, h);
        } else {
            return null;
        }
    }

    public Bitmap getSongBitmap(String folder, String filename) {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, filename);
        if (mainActivityInterface.getStorageAccess().uriExists(uri)) {
            return getBitmapFromUri(uri, -1, -1);
        } else {
            return null;
        }
    }

    public Bitmap getBitmapFromUri(Uri uri, int w, int h) {
        // Load in the bitmap
        try {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (w > 0 && h > 0) {
                options.outWidth = w;
                options.outHeight = h;
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, w,
                        h, true);
                inputStream.close();
                return newBitmap;
            } else {
                return BitmapFactory.decodeStream(inputStream, null, options);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // This bit deals with the song headings used for PDF prints and song sheet view


    // This bit is for the edit song fragments
    public void editBoxToMultiline(MyMaterialEditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_ACTION_NONE);
        editText.setHorizontallyScrolling(true);
        editText.setVerticalScrollBarEnabled(false);
        editText.setAutoSizeTextTypeUniformWithConfiguration(8, 18, 1);
    }

    public void stretchEditBoxToLines(MyMaterialEditText editText, int minLines) {
        String[] lines = editText.getText().toString().split("\n");
        int num = lines.length+1;
        if (num > minLines) {
            editText.setLines(num);
            editText.setMinLines(num);
            editText.setLines(num);
        } else {
            editText.setLines(minLines);
            editText.setMinLines(minLines);
            editText.setLines(minLines);
        }
    }

    public void splitTextByMaxChars(MyMaterialEditText editText, String text, int maxChars,
                                    int maxLines, boolean showVerseNumbers) {

        boolean keepGoing = true;
        if (!showVerseNumbers) {
            while (text.contains("{") && text.contains("}") && keepGoing) {
                int startPos = text.indexOf("{");
                if (startPos > -1) {
                    int endPos = text.indexOf("}", startPos);
                    if (endPos > -1) {
                        String replaceText = text.substring(startPos, endPos) + "}";
                        text = text.replace(replaceText, "");
                    } else {
                        keepGoing = false;
                    }
                } else {
                    keepGoing = false;
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder currentLine;
        int numLines = 0;
        // Current line breaks are still valid
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (numLines > maxLines) {
                stringBuilder.append("\n---\n");
                numLines = 1;
            }
            numLines++;
            currentLine = new StringBuilder();
            String[] words = line.split(" ");
            for (String word : words) {
                if ((currentLine.length() + word.length() + 1) > maxChars) {
                    // Start a new line
                    if (numLines > maxLines) {
                        stringBuilder.append("---\n");
                        numLines = 1;
                    }
                    stringBuilder.append(currentLine.toString().trim()).append("\n");
                    currentLine = new StringBuilder();
                    currentLine.append(word);
                    numLines++;

                } else {
                    currentLine.append(" ").append(word);
                }
            }
            stringBuilder.append(currentLine.toString().trim()).append("\n");
        }
        editText.setText(stringBuilder.toString());
        stretchEditBoxToLines(editText, 4);
    }


    public String tidyThemeString(String themeString) {
        // This just tidies up the theme tags.
        // First split by ;
        String[] stringArray = themeString.split(";");
        ArrayList<String> newArray = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : stringArray) {
            if (!string.trim().isEmpty()) {
                newArray.add(string.trim());
            }
        }
        // Sort alphabetically
        Collections.sort(newArray);

        // Build back to a string
        for (String string : newArray) {
            stringBuilder.append(string).append("; ");
        }
        themeString = stringBuilder.toString();
        if (themeString.startsWith(";")) {
            themeString = themeString.replaceFirst(";", "").trim();
        }
        if (themeString.trim().endsWith(";")) {
            themeString = themeString.substring(0, themeString.lastIndexOf((";")));
        }
        return themeString.trim();
    }

    public ArrayList<String> getSectionHeadings(String lyrics) {
        // Get any named section headings i.e. [...]
        // These are used to create buttons in the edit song tags section
        // All sections are obtained regardless if the user doesn't want them in presentationOrder
        String nums = "0123456789";
        String[] bits = lyrics.split("\\[");
        ArrayList<String> sections = new ArrayList<>();
        for (String bit : bits) {
            if (bit.contains("]") && bit.indexOf("]") < 20) {
                String section = bit.substring(0, bit.indexOf("]"));
                boolean multiverse = false;
                // Check for multiverse/chorus
                String[] lines = bit.split("\n");
                for (String line : lines) {
                    if (line.length() > 2 &&
                            nums.contains(line.substring(0, 1)) &&
                            !sections.contains(section + line.charAt(0))) {
                        sections.add(section + line.charAt(0));
                        multiverse = true;
                    }
                }
                if (!multiverse) {
                    sections.add(section);
                }
            }
        }
        return sections;
    }

    // A check for songs we can edit, etc.
    public boolean isValidSong(Song thisSong) {
        boolean filenameOk = !thisSong.getFilename().equals(c.getString(R.string.welcome)) &&
                !thisSong.getFilename().equals("Welcome to OpenSongApp");
        boolean lyricsOk = !thisSong.getLyrics().contains(c.getString(R.string.song_doesnt_exist));
        boolean folderOk = !thisSong.getFolder().contains("**Image") && !thisSong.getFolder().contains("../Image");

        return filenameOk && lyricsOk && folderOk;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public ArrayList<ImageView> getPDFAsImageViews(Context c, Uri pdfUri) {
        ArrayList<ImageView> imageViews = new ArrayList<>();

        if (mainActivityInterface.getStorageAccess().uriExists(pdfUri)) {
            int totalPages;
            ParcelFileDescriptor parcelFileDescriptor = mainActivityInterface.getProcessSong().getPDFParcelFileDescriptor(pdfUri);
            PdfRenderer pdfRenderer = mainActivityInterface.getProcessSong().getPDFRenderer(parcelFileDescriptor);
            if (pdfRenderer != null) {
                totalPages = pdfRenderer.getPageCount();
            } else {
                totalPages = 0;
            }

            if (pdfRenderer != null) {
                for (int x = 0; x < totalPages; x++) {
                    ImageView imageView = new ImageView(c);
                    PdfRenderer.Page page = pdfRenderer.openPage(x);
                    int width = page.getWidth();
                    int height = page.getHeight();
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(width,height));

                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    Glide.with(c).load(bitmap).override(width,height).into(imageView);
                    imageViews.add(imageView);

                    page.close();
                }
            }

            try {
                if (pdfRenderer != null) {
                    pdfRenderer.close();
                }
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return imageViews;
    }
}
