package com.garethevans.church.opensongtablet.songprocessing;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
import com.garethevans.church.opensongtablet.customviews.MaterialEditText;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

// TODO Tidy up

public class ProcessSong {

    public ProcessSong(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    // The variables used for repeated song processing
    // TODO If the user updates these in the app, check they get updated here as well as the saved preferences!
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "ProcessSong";
    private final float defFontSize = 8.0f;
    private boolean addSectionSpace, blockShadow, displayBoldChordsHeadings,
            displayChords, displayLyrics, displayCapoChords, displayCapoAndNativeChords,
            trimWordSpacing, songAutoScaleColumnMaximise, songAutoScaleOverrideFull,
            songAutoScaleOverrideWidth, trimLines, trimSections, multiLineVerseKeepCompact,
            addSectionBox, multilineSong;
    private float fontSize, fontSizeMax, fontSizeMin, blockShadowAlpha,
            lineSpacing, scaleHeadings, scaleChords, scaleComments;
    private String songAutoScale;
    // Stuff for resizing/scaling
    private int padding = 8;
    private String thisAutoScale;

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
        songAutoScale = mainActivityInterface.getPreferences().getMyPreferenceString("songAutoScale", "W");
        songAutoScaleColumnMaximise = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songAutoScaleColumnMaximise", true);
        songAutoScaleOverrideFull = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songAutoScaleOverrideFull", true);
        songAutoScaleOverrideWidth = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songAutoScaleOverrideWidth", false);
        trimLines = mainActivityInterface.getPreferences().getMyPreferenceBoolean("trimLines", true);
        trimSections = mainActivityInterface.getPreferences().getMyPreferenceBoolean("trimSections", true);
        trimWordSpacing = mainActivityInterface.getPreferences().getMyPreferenceBoolean("trimWordSpacing", true);
        addSectionBox = mainActivityInterface.getPreferences().getMyPreferenceBoolean("addSectionBox",false);
        fontSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("fontSize", 20f);
        fontSizeMax = mainActivityInterface.getPreferences().getMyPreferenceFloat("fontSizeMax", 50f);
        fontSizeMin = mainActivityInterface.getPreferences().getMyPreferenceFloat("fontSizeMin", 8f);
        lineSpacing = mainActivityInterface.getPreferences().getMyPreferenceFloat("lineSpacing", 0.1f);
        scaleHeadings = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleHeadings", 0.6f);
        scaleChords = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleChords", 0.8f);
        scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleComments", 0.8f);
        multiLineVerseKeepCompact = mainActivityInterface.getPreferences().getMyPreferenceBoolean("multiLineVerseKeepCompact", false);
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

        return myNEWXML;
    }

    // These are to deal with custom files (scriptures, etc.)
    public String getLocation(String string) {
        if (string.startsWith("../")) {
            return string.replace("../", "");
        } else {
            return "Songs";
        }
    }

    // These is used when loading and converting songs (ChordPro, badly formatted XML, etc).
    public String parseHTML(String s) {
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

        return s;
    }

    public String parseToHTMLEntities(String s) {
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
                } else if (totallines == 1 || nextlinetype.equals("") || nextlinetype.equals("chord")) {
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

    /*public String changeSlideHeadings(String s) {
        if (!mainActivityInterface.getSong().getFolder().contains(c.getString(R.string.slide)) &&
                !mainActivityInterface.getSong().getFolder().contains(c.getResources().getString(R.string.image)) &&
                !mainActivityInterface.getSong().getFolder().contains(c.getResources().getString(R.string.note)) &&
                !mainActivityInterface.getSong().getFolder().contains(c.getResources().getString(R.string.scripture))) {
            s = s.replace("Slide 1", "[V1]").
                    replace("Slide 2", "[V2]").
                    replace("Slide 3", "[V3]").
                    replace("Slide 4", "[V4]").
                    replace("Slide 5", "[V5]");
        }
        return s;
    }*/

    public String[] getChordPositions(String chord, String lyric) {
        ArrayList<String> chordpositions = new ArrayList<>();

        // IV - Set ready for the loop
        boolean thischordcharempty;
        boolean prevchordcharempty = false;
        boolean prevlyriccharempty;
        boolean prevlyricempty = true;

        for (int x = 1; x < (chord.length()); x++) {
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

        // Look for caps or English tags for non-English app users
        line = replaceBadHeadings(line, "verse", "V");
        line = replaceBadHeadings(line, "prechorus", "P");
        line = replaceBadHeadings(line, "pre-chorus", "P");
        line = replaceBadHeadings(line, "chorus", "C");
        line = replaceBadHeadings(line, "tag", "T");
        line = replaceBadHeadings(line, "bridge", "B");

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

    // Splitting the song up in to manageable chunks
    public String makeGroups(String string, boolean displayChords) {
        if (string == null) {
            string = "";
        }
        String[] lines = string.split("\n");
        StringBuilder sb = new StringBuilder();

        // Go through each line and add bits together as groups ($_groupline_$ between bits, \n for new group)
        int i = 0;
        while (i < lines.length) {
            if (lines[i].startsWith(".") && displayChords) {
                // This is a chord line = this needs to be part of a group
                sb.append("\n").append(lines[i]);
                // If the next line is a lyric or comment add this to the group and stop there
                int nl = i + 1;
                boolean stillworking = true;
                if (shouldNextLineBeAdded(nl, lines, true)) {
                    sb.append("____groupline____").append(fixWordStretch(lines[nl]));
                    while (stillworking) {
                        // Keep going for multiple lines to be added
                        if (shouldNextLineBeAdded(nl + 1, lines, false)) {
                            i = nl;
                            nl++;
                            sb.append("____groupline____").append(fixWordStretch(lines[nl]));
                        } else {
                            i++;
                            stillworking = false;
                        }
                    }
                    // IV - Removed 'While the next line is still a chordline add this line' as breaks highlighting
                }
            } else if (!lines[i].startsWith(".") && !displayChords) {
                // Tidy it up
                lines[i] = lines[i].trim().replace("_", "");
                lines[i] = " " + lines[i];
                sb.append("\n").append(lines[i]);
            } else if (displayLyrics && !lines[i].startsWith(".")) {
                sb.append("\n").append(fixWordStretch(lines[i]));
            }
            i++;
        }
        return sb.toString();
    }

    private boolean shouldNextLineBeAdded(int nl, String[] lines, boolean incnormallyricline) {
        if (incnormallyricline) {
            return (nl < lines.length && (lines[nl].startsWith(" ") || lines[nl].startsWith(";") ||
                    lines[nl].matches("^[0-9].*$")));
        } else {
            return (nl < lines.length && (lines[nl].matches("^[0-9].*$")));
        }
    }

    private String makeSections(String string) {
        string = string.replace("§", "\n____SPLIT____").
                replace("\n\n\n", "\n \n____SPLIT____").
                replace("\n \n \n", "\n \n____SPLIT____").
                replace("\n\n", "\n \n____SPLIT____").
                replace("\n \n", "\n \n____SPLIT____").
                replace("\n[", "\n____SPLIT____[").
                replace("\n [", "\n____SPLIT____[").
                replace("\n[", "\n____SPLIT____[").
                replace("____SPLIT________SPLIT____", "____SPLIT____");
        if (string.trim().startsWith("____SPLIT____")) {
            string = string.replaceFirst(" ____SPLIT____", "");
            while (string.startsWith("\n") || string.startsWith(" ")) {
                if (string.startsWith(" ")) {
                    string = string.replaceFirst(" ", "");
                } else {
                    string = string.replaceFirst("\n", "");
                }
            }
        }
        if (string.startsWith("____SPLIT____")) {
            string = string.replaceFirst("____SPLIT____", "");
        }
        return string;
    }

    private TableLayout groupTable(String string, int lyricColor, int chordColor, int capoColor,
                                   int highlightChordColor, boolean presentation) {
        TableLayout tableLayout = newTableLayout();

        // If we have a capo and want to show capo chords, duplicate and tranpose the chord line
        String capoText = mainActivityInterface.getSong().getCapo();
        boolean hasCapo = capoText!=null && !capoText.isEmpty();
        if (hasCapo && (displayCapoChords || displayCapoAndNativeChords)) {
            int capo = Integer.parseInt(capoText);
            String chordbit = string.substring(0,string.indexOf("____groupline____"));
            chordbit = mainActivityInterface.getTranspose().transposeChordForCapo(capo,chordbit).replaceFirst(".","˄");
            // Add it back in with a capo identifying this part
            string = chordbit + "____groupline____" + string;
        }

        // Split the group into lines
        String[] lines = string.split("____groupline____");

        // Line 0 is the chord line (or capo line).  All other lines need to be at least this size
        // Make it 1 char bigger to identify the end of it
        lines[0] += " ";
        if (lineIsChordForMultiline(lines)) {
            lines[0] = ". " + lines[0].substring(1);
        }

        int minlength = lines[0].length();
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

        String linetype;
        String lastlinetype = "";

        // Now we have the sizes, split into individual TextViews inside a TableRow for each line
        for (int t = 0; t < lines.length; t++) {
            TableRow tableRow = newTableRow();
            if (presentation) {
                tableRow.setGravity(mainActivityInterface.getPresenterSettings().getPresoLyricsAlign());
            }
            linetype = getLineType(lines[t]);

            // IV - Add back a quirk of the older layout engine that rendered a comment line following a chord line as a lyric line;
            // IV - Commenting a lyric line is effective at suppressing a lyric line when presenting - useful when a line has variations with different chords
            if (lastlinetype.equals("chord") && linetype.equals("comment")) {
                lastlinetype = "comment";
                linetype = "lyric";
                lines[t] = " " + lines[t].substring(1);
            } else {
                lastlinetype = linetype;
            }

            Typeface typeface = getTypeface(presentation, linetype);
            float size = getFontSize(linetype);
            int color = getFontColor(linetype, lyricColor, chordColor, capoColor);
            int startpos = 0;
            for (int endpos : pos) {
                if (endpos != 0) {
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
                                if (trimWordSpacing) {
                                    if (!multiLineVerseKeepCompact && !multilineSong) {
                                        str = fixExcessSpaces(str);
                                    }
                                }
                                if (presentation || !mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
                                    str = fixExcessSpaces(str);
                                }

                                String start = "";
                                String end = "";
                                if (str.startsWith(" ") && !str.trim().startsWith("-")) {
                                    start = " ";
                                }
                                if (str.endsWith(" ") && !str.trim().endsWith("-")) {
                                    end = " ";
                                }
                                str = start + str.trim() + end;
                                textView.setText(str);
                            } else {
                                textView = null;
                            }
                            break;
                        default:
                            // Just set the text
                            if (presentation || !mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) ||
                                    (!multiLineVerseKeepCompact && !multilineSong)) {
                                str = fixExcessSpaces(str);
                            }
                            textView.setText(str);
                            break;
                    }
                    if (textView!=null) {
                        tableRow.addView(textView);
                    }
                    startpos = endpos;
                }
            }
            // Add the final position
            TextView textView = newTextView(linetype, typeface, size, color);
            String str = lines[t].substring(startpos);
            if (str.startsWith(".")) {
                str = str.replaceFirst(".", "");
            }
            if (str.startsWith("˄")) {
                str = str.replaceFirst("˄", "");
            }
            if (linetype.equals("chord") && displayChords && (!hasCapo || displayCapoAndNativeChords || !displayCapoChords)) {
                if (highlightChordColor != 0x00000000) {
                    textView.setText(highlightChords(str, highlightChordColor));
                } else {
                    textView.setText(str);
                }
            } else if (linetype.equals("capoline") && displayChords && (hasCapo || displayCapoChords || displayCapoAndNativeChords)) {
                if (highlightChordColor != 0x00000000) {
                    textView.setText(highlightChords(str, highlightChordColor));
                } else {
                    textView.setText(str);
                }
            } else if (linetype.equals("lyric")) {
                if (displayLyrics) {
                    str = str.replace("_","");
                    str = str.replaceAll("[|]"," ");
                    if (trimWordSpacing) {
                        str = fixExcessSpaces(str);
                    }
                    str = str.trim();
                    textView.setText(str);
                } else {
                    textView = null;
                }
            } else if (linetype.equals("chord") || linetype.equals("chordline")) {
                textView = null;
            } else {
                textView.setText(str.trim());
            }
            if (textView!=null) {
                tableRow.addView(textView);
            }
            tableLayout.addView(tableRow);
        }
        return tableLayout;
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
        // Look for [v] or [c] case insensitive
        // And it needs to be followed by a line starting with 1 and 2
        if (string != null) {
            try {
                String[] sl = string.split("\n");
                boolean has_multiline_vtag = false;
                boolean has_multiline_ctag = false;
                boolean has_multiline_1tag = false;
                boolean has_multiline_2tag = false;

                for (String l : sl) {
                    if (l.toLowerCase(mainActivityInterface.getLocale()).startsWith("[v]")) {
                        has_multiline_vtag = true;
                    } else if (l.toLowerCase(mainActivityInterface.getLocale()).startsWith("[c]")) {
                        has_multiline_ctag = true;
                    } else if (l.toLowerCase(mainActivityInterface.getLocale()).startsWith("1")) {
                        has_multiline_1tag = true;
                    } else if (l.toLowerCase(mainActivityInterface.getLocale()).startsWith("2")) {
                        has_multiline_2tag = true;
                    }
                }

                return (has_multiline_vtag || has_multiline_ctag) && has_multiline_1tag && has_multiline_2tag;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean lineIsChordForMultiline(String[] lines) {
        return (lines[0].length() > 1 && lines.length > 1 && lines[1].matches("^[0-9].*$"));
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
                        chorus[cnum] += lines[z].substring(2) + "\n";
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
                    replacementtext.append(chordlines[x]).append("\n").append(section[x + 1]).append("\n");
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
                song.getPresentationorder() != null && !song.getPresentationorder().isEmpty()) {
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
                InformationBottomSheet informationBottomSheet = new InformationBottomSheet(
                        c.getString(R.string.presentation_order), errors.toString().trim(),
                        c.getString(R.string.edit_song), c.getString(R.string.deeplink_edit));
                informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "InformationBottomSheet");
            }

            song.setPresoOrderSongSections(newSections);
            song.setPresoOrderSongHeadings(newHeaders);
        } else {
            // Not using presentation order, so just return what we have
            song.setPresoOrderSongSections(song.getGroupedSections());
            song.setPresoOrderSongHeadings(song.getSongSectionHeadings());
        }
    }

    private TextView lineText(String linetype,
                              String string, Typeface typeface, float size, int color,
                              int highlightHeadingColor, int highlightChordColor, boolean presentation) {
        TextView textView = newTextView(linetype, typeface, size, color);
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
                // TODO
                // IV - This will need more complexity depending on mode and if showing chords
                if ((!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                        (presentation || trimWordSpacing)) ||
                        (!multilineSong || !multiLineVerseKeepCompact)) {
                    str = fixExcessSpaces(str);
                }
                textView.setText(str.replaceAll("[|_]", " "));
            } else {
                textView.setText(str);
            }
        }
        return textView;
    }

    public String fixExcessSpaces(String str) {
        if (trimWordSpacing) {
            // Encode new lines as something else first
            str = str.replace("\n","___NEWLINE___");
            // This removes multiple spaces and returns single spaces
            str = str.replaceAll("\\s+", " ");
            // Now fix sentences
            str = str.replace(". ", ".  ");
            // Return new lines
            str = str.replace("___NEWLINE___","\n");
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

        // 2. Check for multiline verse formatting e.g. [V] 1. 2. etc.
        lyrics = fixMultiLineFormat(lyrics, presentation);

        // 3. Go through the song lyrics and get any section headers.  These get added to the song object
        song.setSongSectionHeadings(getSectionHeadings(lyrics));

        // 4. Prepare for line splits: | are relevant to Presenter mode only without chord display
        String lineSplit = " ";
        if (presentation && !mainActivityInterface.getPresenterSettings().getPresoShowChords()) {
            lineSplit = "\n";
        }

        // 5. Prepare for section splits: || are relevant to presentation and Stage mode.
        // If sectionSplit is ║ is used to test for further processing later.
        String sectionSplit = "";
        if (presentation || mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage))) {
            sectionSplit = "║";
        }

        // 6. Prepare for double new line:  We split at \n\n but not for scripture
        String doubleNewlineSplit = "\n\n";
        if (!mainActivityInterface.getSong().getFolder().contains(c.getResources().getString(R.string.scripture))) {
            doubleNewlineSplit = "§";
        }

        // 7. Process || and | split markers on lyric lines
        // Add a trailing ¶ to force a split behaviour that copes with a trailing new line!
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : (lyrics + "¶").split("\n")) {
            // IV - Use leading \n as we can be certain it is safe to remove later
            stringBuilder.append("\n");
            if (line.startsWith(" ")) {
                line = line
                        .replace("||", sectionSplit)
                        .replace("|", lineSplit);
            }
            // Add it back up
            stringBuilder.append(line);
        }
        lyrics = stringBuilder.toString()
                .replace("-!!", "")
                // --- Process new section markers
                .replace("\n ---", "\n[]")
                .replace("\n---", "\n[]")
                .substring(1).replace("¶", "")
                // Prevent empty lines
                .replace("\n\n", doubleNewlineSplit)
                .replace("\n[", "§[")
                .replace("§\n", "§")
                .replace("\n§", "§")
                .replace("§ §", "§")
                .replace("§§", "§");
        //.split("§");

        // Next up we go through the lyrics and group lines that should be in a table for alignment purposes
        if (presentation) {
            lyrics = makeGroups(lyrics, mainActivityInterface.getPresenterSettings().getPresoShowChords());
        } else {
            lyrics = makeGroups(lyrics, displayChords);
        }

        // Next we generate the split points for sections
        lyrics = makeSections(lyrics);

        // Split into sections and process each separately
        String[] sections = lyrics.split("____SPLIT____");

        // Build the songSections for later recall
        // The song sections are not the views (which can have sections repeated using presentationOrder
        // The grouped sections are used for alignments
        ArrayList<String> songSections = new ArrayList<>();
        ArrayList<String> groupedSections = new ArrayList<>();
        for (String thisSection : sections) {
            String thisSectionCleaned = thisSection.replace("____groupline____", "\n");
            if (!thisSection.trim().isEmpty()) {
                songSections.add(thisSectionCleaned);
                groupedSections.add(thisSection);
            }
        }
        song.setSongSections(songSections);
        song.setGroupedSections(groupedSections);
    }

    public ArrayList<View> setSongInLayout(Song song, boolean asPDF, boolean presentation) {
        ArrayList<View> sectionViews = new ArrayList<>();
        ArrayList<Integer> sectionColors = new ArrayList<>();

        // First we process the song (could be the loaded song, or a temp song - that's why we take a reference)
        // If this is for a presentation, we've already dealt with it
        if (!presentation) {
            processSongIntoSections(song, presentation);
            // We also consider any presentation order that is set
            matchPresentationOrder(song);
        }

        //
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
                if (trimSections) {
                    section = section.trim();
                }
                if (!presentation && addSectionSpace && !mainActivityInterface.getMakePDF().getIsSetListPrinting() &&
                        sect != (song.getPresoOrderSongSections().size() - 1)) { // Don't do for last section
                    section = section + "\n ";
                }
                LinearLayout linearLayout = newLinearLayout(); // transparent color
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
                    // Now split by line
                    String[] lines = section.split("\n");
                    for (int l=0; l<lines.length; l++) {
                        String line = lines[l];
                        // Get the text stylings
                        String linetype = getLineType(line);

                        if (presentation && linetype.equals("heading")) {
                            // Don't need this for the presentation view
                            line = "";
                        }
                        backgroundColor = overallBackgroundColor;
                        if (!asPDF && !presentation && (linetype.equals("heading") || linetype.equals("comment") || linetype.equals("tab"))) {
                            backgroundColor = getBGColor(line);
                            if (l==0) {
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


                        if (line.contains("____groupline____")) {
                            // Has lyrics and chords
                            if (asPDF) {
                                linearLayout.addView(groupTable(line, Color.BLACK, Color.BLACK,
                                        Color.BLACK, Color.TRANSPARENT, false));
                            } else if (presentation) {
                                linearLayout.addView(groupTable(line,
                                        mainActivityInterface.getMyThemeColors().getPresoFontColor(),
                                        mainActivityInterface.getMyThemeColors().getPresoChordColor(),
                                        mainActivityInterface.getMyThemeColors().getLyricsCapoColor(),
                                        mainActivityInterface.getMyThemeColors().getHighlightChordColor(), true));
                            } else {
                                TableLayout tl = groupTable(line,
                                        mainActivityInterface.getMyThemeColors().getLyricsTextColor(),
                                        mainActivityInterface.getMyThemeColors().getLyricsChordsColor(),
                                        mainActivityInterface.getMyThemeColors().getLyricsCapoColor(),
                                        mainActivityInterface.getMyThemeColors().getHighlightChordColor(), false);
                                tl.setBackgroundColor(backgroundColor);
                                linearLayout.addView(tl);
                            }
                        } else {
                            if (!presentation || !line.isEmpty()) {
                                TextView tv = lineText(linetype, line, typeface,
                                        size, textColor,
                                        mainActivityInterface.getMyThemeColors().getHighlightHeadingColor(),
                                        mainActivityInterface.getMyThemeColors().getHighlightChordColor(), presentation);
                                tv.setBackgroundColor(backgroundColor);
                                linearLayout.addView(tv);
                            } else {
                                // PDF or presentation
                                linearLayout.addView(lineText(linetype, line, typeface,
                                        size, textColor, Color.TRANSPARENT, Color.TRANSPARENT, presentation));
                            }
                        }
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
            if (string.equals("chord")) {
                return mainActivityInterface.getMyFonts().getChordFont();
            }
            return mainActivityInterface.getMyFonts().getLyricFont();
        }
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

    private int getBGColor(String line) {
        if (line.startsWith(";")) {
            return mainActivityInterface.getMyThemeColors().getLyricsCommentColor();
        } else if (beautifyHeading(line).contains(c.getString(R.string.verse))) {
            return mainActivityInterface.getMyThemeColors().getLyricsVerseColor();
        } else if (beautifyHeading(line).contains(c.getString(R.string.prechorus))) {
            return mainActivityInterface.getMyThemeColors().getLyricsPreChorusColor();
        } else if (beautifyHeading(line).contains(c.getString(R.string.chorus))) {
            return mainActivityInterface.getMyThemeColors().getLyricsChorusColor();
        } else if (beautifyHeading(line).contains(c.getString(R.string.bridge))) {
            return mainActivityInterface.getMyThemeColors().getLyricsBridgeColor();
        } else if (beautifyHeading(line).contains(c.getString(R.string.tag))) {
            return mainActivityInterface.getMyThemeColors().getLyricsTagColor();
        } else if (beautifyHeading(line).contains(c.getString(R.string.custom))) {
            return mainActivityInterface.getMyThemeColors().getLyricsCustomColor();
        } else {
            return mainActivityInterface.getMyThemeColors().getLyricsVerseColor();
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
                textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
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

    private FrameLayout newFrameLayout(int color) {
        FrameLayout frameLayout = new FrameLayout(c);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(0, 0, 0, 0);
        frameLayout.setLayoutParams(llp);
        frameLayout.setPadding(0, 0, 0, 0);
        frameLayout.setClipToPadding(false);
        frameLayout.setClipChildren(false);
        frameLayout.setBackgroundColor(color);
        return frameLayout;
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

    private float setScaledView(LinearLayout innerColumn, float scaleSize, float maxFontSize) {
        if (innerColumn!=null) {
            innerColumn.setPivotX(0);
            innerColumn.setPivotY(0);
            // Don't scale above the preferred maximum font size
            float maxScaleSize = maxFontSize / defFontSize;
            if (scaleSize > maxScaleSize) {
                scaleSize = maxScaleSize;
            }
            innerColumn.setScaleX(scaleSize);
            innerColumn.setScaleY(scaleSize);
        }
        return scaleSize;
    }

    private void resizeColumn(LinearLayout column, int startWidth, int startHeight, float scaleSize) {
        // Used to resize the inner columns which get added to columns 1,2,3
        int newWidth = Math.round(startWidth*scaleSize);
        int newHeight = Math.max(Math.round(startHeight * scaleSize), startHeight);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(newWidth, newHeight);
        column.setLayoutParams(lp);
    }

    private int howManyColumnsAreBest(float col1, float[] col2, float[] col3, String autoScale,
                                      float fontSizeMin, boolean songAutoScaleOverrideFull,
                                      boolean need23ColumnCheck) {
        // There's a few things to consider here.  Firstly, if scaling is off, best is 1 column.
        // If we are overriding full scale to width only, or 1 col to off, best is 1 column.
        if (!need23ColumnCheck && (autoScale.equals("N") || autoScale.equals("W"))) {
            return 1;
        } else {
            float col2best = Math.min(col2[0], col2[1]);
            float col3best = Math.min(col3[0], Math.min(col3[1], col3[2]));
            int best;
            if (col1 > col2best) {
                best = 1;
                if (col3best > col1) {
                    best = 3;
                }
            } else {
                best = 2;
                if (col3best > col2best) {
                    best = 3;
                }
            }
            // Default font size is 14sp when drawing. If scaling takes this below the min font Size, override back to 1 column
            if (best == 2) {
                if (col2[2] == 0) {
                    return 1;
                }
                float newFontSize2Col = defFontSize * col2best;

                if (!need23ColumnCheck && songAutoScaleOverrideFull && newFontSize2Col < fontSizeMin) {
                    thisAutoScale = "W";
                    return 1;
                }
            }
            if (best == 3) {
                if (col3[3] == 0) {
                    return 2;
                }
                float newFontSize3Col = defFontSize * col3best;
                if (songAutoScaleOverrideFull && newFontSize3Col < fontSizeMin) {
                    thisAutoScale = "W";
                    return 1;
                }
            }
            return best;
        }
    }


    // These are called from the VTO listener - draw the stuff to the screen as 1,2 or 3 columns
    // This then returns the best (largest) scaling size as a float
    public float[] addViewsToScreen(boolean need23ColumnCheck, ArrayList<View> sectionViews,
                                    ArrayList<Integer> sectionWidths, ArrayList<Integer> sectionHeights,
                                    RelativeLayout pageHolder, LinearLayout songView,
                                    LinearLayout songSheetView, int screenWidth, int screenHeight,
                                    LinearLayout column1, LinearLayout column2, LinearLayout column3,
                                    boolean presentation, DisplayMetrics displayMetrics) {
        // Now we have all the sizes in, determines the best way to show the song
        // This will be single, two or three columns.  The best one will be the one
        // which gives the best scale size

        // Clear and reset the view's scaling
        resetRelativeLayout(pageHolder);
        clearAndResetLinearLayout(songView, false);
        if (songSheetView!=null) {
            clearAndResetLinearLayout(songSheetView, false);
        }
        //pageHolder.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT));
        //songView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        clearAndResetLinearLayout(column1, true);
        clearAndResetLinearLayout(column2, true);
        clearAndResetLinearLayout(column3, true);

        // Set the padding and boxpadding from 8dp to px
        float scale = displayMetrics.density;
        padding = (int) (8 * scale);

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
            songSheetView.addView(mainActivityInterface.getSongSheetTitleLayout());
            songSheetView.setVisibility(View.VISIBLE);
        } else if (songSheetView!=null) {
            songSheetView.setVisibility(View.GONE);
        }

        thisAutoScale = songAutoScale;

        float[] scaleSize_2cols = new float[3];
        float[] scaleSize_3cols = new float[5];
        // All scaling types need to process the single column view, either to use it or compare to 2/3 columns
        if (songAutoScale.equals("Y") || need23ColumnCheck) {
            // Figure out two and three columns.  Only do this if we need to to save processing time.
            scaleSize_2cols = col2Scale(screenWidth, screenHeight-songSheetTitleHeight, currentHeight, songAutoScaleColumnMaximise, sectionWidths, sectionHeights);
            scaleSize_3cols = col3Scale(screenWidth, screenHeight-songSheetTitleHeight, currentHeight, songAutoScaleColumnMaximise, sectionWidths, sectionHeights);
        }

        // Set the scaleSize_1col
        float[] scaleSize_1col = col1Scale(screenWidth, screenHeight-songSheetTitleHeight, currentWidth, currentHeight);

        // Now decide if 1,2 or 3 columns is best
        int howManyColumns = howManyColumnsAreBest(scaleSize_1col[0], scaleSize_2cols, scaleSize_3cols, songAutoScale, fontSizeMin, songAutoScaleOverrideFull,need23ColumnCheck);

        switch (howManyColumns) {
            case 1:
            default:
                // If we are using one column and resizing to width only, change the scale size
                if (!presentation) {
                    if (songAutoScale.equals("W") || thisAutoScale.equals("W")) {
                        scaleSize_1col[0] = (float) screenWidth / (float) currentWidth;
                        if (defFontSize * scaleSize_1col[0] < fontSizeMin && songAutoScaleOverrideWidth) {
                            thisAutoScale = "N";
                        }
                    }
                    // If autoscale is off, scale to the desired fontsize
                    if (songAutoScale.equals("N") || thisAutoScale.equals("N")) {
                        scaleSize_1col[0] = fontSize / defFontSize;
                    }
                }
                return setOneColumn(sectionViews, column1, column2, column3, currentWidth,
                        currentHeight, scaleSize_1col, fontSizeMax, songSheetTitleHeight, presentation);

            case 2:
                return setTwoColumns(sectionViews, column1, column2, column3,
                        sectionHeights, scaleSize_2cols, fontSizeMax, (int) ((float) screenWidth / 2.0f - padding),
                        songSheetTitleHeight, presentation, displayMetrics);

            case 3:
                return setThreeColumns(sectionViews, column1, column2, column3, sectionWidths,
                        sectionHeights, scaleSize_3cols, fontSizeMax, (int) ((float) screenWidth / 3.0f - padding),
                        songSheetTitleHeight, presentation, displayMetrics);
        }
    }

    // 1 column stuff
    private float[] col1Scale(int screenWidth, int screenHeight, int viewWidth, int viewHeight) {
        // 0=scale col1   1=scaled height
        float[] scale = new float[2];

        float x_scale = (float) screenWidth / (float) viewWidth;
        float y_scale = (float) screenHeight / ((float) viewHeight);
        scale[0] = Math.min(x_scale, y_scale);
        scale[1] = scale[0] * viewHeight;
        return scale;
    }

    private float[] setOneColumn(ArrayList<View> sectionViews, LinearLayout column1,
                                 LinearLayout column2, LinearLayout column3, int currentWidth,
                                 int currentHeight, float[] scaleSize, float maxFontSize,
                                 int songSheetTitleHeight, boolean presentation) {
        // Hide all columns for now
        columnVisibility(column1, column2, column3, false, false, false);

        // Prepare the inner column
        LinearLayout innerCol1 = newLinearLayout();

        // Get the actual scale which might be less due to the maxFontSize being exceeded
        float thisScale = setScaledView(innerCol1, scaleSize[0], maxFontSize);
        innerCol1.setLayoutParams(new LinearLayout.LayoutParams((int)(currentWidth*thisScale),LinearLayout.LayoutParams.WRAP_CONTENT));

        int color;
        // For each section, add it to a relativelayout to deal with the background colour.
        for (View v : sectionViews) {
            color = Color.TRANSPARENT;
            Drawable background = v.getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }

            FrameLayout frameLayout = newFrameLayout(color);
            if (v.getParent() != null) {
                ((ViewGroup) v.getParent()).removeAllViews();
            }
            frameLayout.setClipChildren(false);
            frameLayout.setClipToPadding(false);
            frameLayout.addView(v);
            // Now the view is created and has content, size it to the correct width
            frameLayout.setLayoutParams(new LinearLayout.LayoutParams(currentWidth,LinearLayout.LayoutParams.WRAP_CONTENT));
            innerCol1.addView(frameLayout);
        }

        // Get a scaled view (get the actual scale returned as it might be smaller based on max font size)
        resizeColumn(innerCol1, currentWidth, currentHeight, thisScale);
        setMargins(column1, 0, 0);
        column1.addView(innerCol1);
        columnVisibility(column1, column2, column3, true, false, false);
        if (!presentation) {
            mainActivityInterface.updateSizes((int) (currentWidth * thisScale), (int) (currentHeight * thisScale) + songSheetTitleHeight);
        }
        scaleSize[0] = thisScale;
        scaleSize[1] = thisScale * currentHeight;
        return scaleSize;
    }


    // 2 column stuff
    private float[] col2Scale(int screenWidth, int screenHeight, int totalViewHeight, boolean songAutoScaleColumnMaximise,
                              ArrayList<Integer> viewWidth, ArrayList<Integer> viewHeight) {
        // 0=scale col1   1=scale col2    2=section num col2 starts at    3=biggest scaled height
        float[] scaleSize_2cols = new float[4];

        // Now go through the views and decide on the number for the first column (the rest is the second column)
        int col1Height = 0;
        int col2Height = totalViewHeight;
        int preHalfWay = 0;
        int postHalfWay = 0;
        int i = 0;
        while (i < viewHeight.size() && col1Height < col2Height) {
            preHalfWay = i;
            postHalfWay = preHalfWay + 1;
            col1Height += viewHeight.get(i);
            col2Height -= viewHeight.get(i);
            i++;
        }

        // Get the max width for pre halfway split column 1
        int maxWidth_preHalfWay1 = getMaxValue(viewWidth, 0, preHalfWay);
        int maxWidth_preHalfWay2 = getMaxValue(viewWidth, preHalfWay, viewWidth.size());
        int totaHeight_preHalfWay1 = getTotal(viewHeight, 0, preHalfWay);
        int totaHeight_preHalfWay2 = getTotal(viewHeight, preHalfWay, viewHeight.size());
        int maxWidth_postHalfWay1 = getMaxValue(viewWidth, 0, postHalfWay);
        int maxWidth_postHalfWay2 = getMaxValue(viewWidth, postHalfWay, viewWidth.size());
        int totaHeight_postHalfWay1 = getTotal(viewHeight, 0, postHalfWay);
        int totaHeight_postHalfWay2 = getTotal(viewHeight, postHalfWay, viewHeight.size());

        // Get pre and post halfway scales
        float halfWidth = ((float) screenWidth / 2.0f) - padding - 0.5f;
        float preCol1scaleX = halfWidth / (float) maxWidth_preHalfWay1;
        float preCol1scaleY = (float) screenHeight / (float) totaHeight_preHalfWay1;
        float preCol1Scale = Math.min(preCol1scaleX, preCol1scaleY);
        float preCol2scaleX = halfWidth / (float) maxWidth_preHalfWay2;
        float preCol2scaleY = (float) screenHeight / (float) totaHeight_preHalfWay2;
        float preCol2Scale = Math.min(preCol2scaleX, preCol2scaleY);

        float postCol1scaleX = halfWidth / (float) maxWidth_postHalfWay1;
        float postCol1scaleY = (float) screenHeight / (float) totaHeight_postHalfWay1;
        float postCol1Scale = Math.min(postCol1scaleX, postCol1scaleY);
        float postCol2scaleX = halfWidth / (float) maxWidth_postHalfWay2;
        float postCol2scaleY = (float) screenHeight / (float) totaHeight_postHalfWay2;
        float postCol2Scale = Math.min(postCol2scaleX, postCol2scaleY);

        // Prefer the method that gives the largest scaling of col1 + col2
        float preScaleTotal = preCol1Scale + preCol2Scale;
        float postScaleTotal = postCol1Scale + postCol2Scale;

        if (preScaleTotal >= postScaleTotal) {
            scaleSize_2cols[0] = preCol1Scale;
            scaleSize_2cols[1] = preCol2Scale;
            scaleSize_2cols[2] = preHalfWay;
        } else {
            scaleSize_2cols[0] = postCol1Scale;
            scaleSize_2cols[1] = postCol2Scale;
            scaleSize_2cols[2] = postHalfWay;
        }

        if (!songAutoScaleColumnMaximise) {
            // make 2 all the values of the smallest (but the same)
            float min = Math.min(scaleSize_2cols[0], scaleSize_2cols[1]);
            scaleSize_2cols[0] = min;
            scaleSize_2cols[1] = min;
        }

        return scaleSize_2cols;
    }

    private float[] setTwoColumns(ArrayList<View> sectionViews, LinearLayout column1,
                                  LinearLayout column2, LinearLayout column3,
                                  ArrayList<Integer> sectionHeights,
                                  float[] scaleSize, float maxFontSize, int halfwidth,
                                  int songSheetTitleHeight, boolean presentation,
                                  DisplayMetrics displayMetrics) {
        // Use 2 columns, but hide them all for now
        columnVisibility(column1, column2, column3, false, false, false);

        // Prepare the inner columns
        LinearLayout innerCol1 = newLinearLayout();
        LinearLayout innerCol2 = newLinearLayout();

        int col1Height = getTotal(sectionHeights, 0, (int) scaleSize[2]);
        int col2Height = getTotal(sectionHeights, (int) scaleSize[2], sectionHeights.size());

        // Get the actual scale which might be less due to the maxFontSize being exceeded
        float thisScale1 = setScaledView(innerCol1, scaleSize[0], maxFontSize);
        innerCol1.setLayoutParams(new LinearLayout.LayoutParams((int)(halfwidth),LinearLayout.LayoutParams.WRAP_CONTENT));
        float thisScale2 = setScaledView(innerCol2, scaleSize[1], maxFontSize);
        innerCol2.setLayoutParams(new LinearLayout.LayoutParams((int)(halfwidth),LinearLayout.LayoutParams.WRAP_CONTENT));

        int color;
        for (int i = 0; i < scaleSize[2]; i++) {
            color = Color.TRANSPARENT;
            Drawable background = sectionViews.get(i).getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            FrameLayout frameLayout = newFrameLayout(color);
            if (sectionViews.get(i).getParent()!=null) {
                ((ViewGroup)sectionViews.get(i).getParent()).removeView(sectionViews.get(i));
            }
            frameLayout.setClipChildren(false);
            frameLayout.setClipToPadding(false);
            frameLayout.addView(sectionViews.get(i));

            // Now the view is created and has content, size it to the correct width
            frameLayout.setLayoutParams(new LinearLayout.LayoutParams(halfwidth,LinearLayout.LayoutParams.WRAP_CONTENT));
            innerCol1.addView(frameLayout);
        }
        for (int i = (int) scaleSize[2]; i < sectionViews.size(); i++) {
            color = Color.TRANSPARENT;
            Drawable background = sectionViews.get(i).getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            FrameLayout frameLayout = newFrameLayout(color);
            if (sectionViews.get(i).getParent()!=null) {
                ((ViewGroup)sectionViews.get(i).getParent()).removeView(sectionViews.get(i));
            }
            frameLayout.setClipChildren(false);
            frameLayout.setClipToPadding(false);
            frameLayout.addView(sectionViews.get(i));

            // Now the view is created and has content, size it to the correct width
            frameLayout.setLayoutParams(new LinearLayout.LayoutParams(halfwidth,LinearLayout.LayoutParams.WRAP_CONTENT));
            innerCol2.addView(frameLayout);
        }
        columnVisibility(column1, column2, column3, true, true, false);

        resizeColumn(innerCol1, halfwidth, col1Height, thisScale1);
        resizeColumn(innerCol2, halfwidth, col2Height, thisScale2);

        column1.addView(innerCol1);
        column2.addView(innerCol2);

        setMargins(column1, 0, padding);
        setMargins(column2, padding, 0);

        int col1h = (int) (col1Height*thisScale1);
        int col2h = (int) (col2Height*thisScale2);

        scaleSize[0] = thisScale1;
        scaleSize[1] = thisScale2;
        scaleSize[3] = Math.max(col1h,col2h);
        if (!presentation) {
            mainActivityInterface.updateSizes(displayMetrics.widthPixels,
                    (int) scaleSize[3] + songSheetTitleHeight);
        }
        return scaleSize;
    }

    // 3 column stuff
    private float[] col3Scale(int screenWidth, int screenHeight, int totalViewHeight,
                              boolean songAutoScaleColumnMaximise, ArrayList<Integer> viewWidth,
                              ArrayList<Integer> viewHeight) {
        // 0=scale col1   1=scale col2    2=scale col2
        // 3=section num col2 starts at   4=section num col3 starts at
        // 5=biggest scaled height
        float[] scaleSize_3cols = new float[6];

        // Find the third height of all of the views together
        float thirdViewheight = (float) totalViewHeight / 3.0f;

        // Go through the three sections and try to get them similar
        int col1Height = 0;
        int preThirdWay = 0;
        int postThirdWay = 0;
        int i = 0;
        while (i < viewHeight.size() && col1Height < thirdViewheight) {
            preThirdWay = i;
            postThirdWay = preThirdWay + 1;
            col1Height += viewHeight.get(i);
            i++;
        }
        if (postThirdWay > viewHeight.size()) {
            postThirdWay = preThirdWay;
        }

        // Decide if we're closer underheight or overheight
        int col1Height_pre = getTotal(viewHeight, 0, preThirdWay);
        int col1Height_post = getTotal(viewHeight, 0, postThirdWay);
        int diff_pre = Math.abs((int) thirdViewheight - getTotal(viewHeight, 0, preThirdWay));
        int diff_post = Math.abs((int) thirdViewheight - getTotal(viewHeight, 0, postThirdWay));

        int thirdWay;
        if (diff_pre <= diff_post) {
            thirdWay = preThirdWay;
            col1Height = col1Height_pre;
        } else {
            thirdWay = postThirdWay;
            col1Height = col1Height_post;
        }

        // Now we have the best first column, we compare column2 and column3 in ths same way as 2 columns
        int col2Height = 0;
        int col3Height = totalViewHeight - col1Height;
        int preTwoThirdWay = 0;
        int postTwoThirdWay = 0;
        i = thirdWay;
        while (i < viewHeight.size() && col2Height < col3Height) {
            preTwoThirdWay = i;
            postTwoThirdWay = preTwoThirdWay + 1;
            col2Height += viewHeight.get(i);
            col3Height -= viewHeight.get(i);
        }
        if (postTwoThirdWay > viewHeight.size()) {
            postTwoThirdWay = preTwoThirdWay;
        }

        // Decide if we're closer underheight or overheight
        int col2Height_pre = getTotal(viewHeight, thirdWay, preTwoThirdWay);
        int col2Height_post = getTotal(viewHeight, thirdWay, postTwoThirdWay);
        int col3Height_pre = totalViewHeight - col2Height_pre;
        int col3Height_post = totalViewHeight - col2Height_post;
        diff_pre = Math.abs(col2Height_pre - col3Height_pre);
        diff_post = Math.abs(col2Height_post - col3Height_post);

        int twoThirdWay;
        if (diff_pre <= diff_post) {
            twoThirdWay = preTwoThirdWay;
            col2Height = col2Height_pre;
            col3Height = col3Height_pre;
        } else {
            twoThirdWay = postTwoThirdWay;
            col2Height = col2Height_post;
            col3Height = col3Height_post;
        }

        // Now decide on the x and y scaling available for each column
        int maxWidthCol1 = getMaxValue(viewWidth, 0, thirdWay);
        int maxWidthCol2 = getMaxValue(viewWidth, thirdWay, twoThirdWay);
        int maxWidthCol3 = getMaxValue(viewWidth, twoThirdWay, viewWidth.size());

        float thirdWidth = ((float) screenWidth / 3.0f) - padding;

        float col1Xscale = thirdWidth / (float) maxWidthCol1;
        float col1Yscale = (float) screenHeight / (float) col1Height;
        float col1Scale = Math.min(col1Xscale, col1Yscale);
        float col2Xscale = thirdWidth / (float) maxWidthCol2;
        float col2Yscale = (float) screenHeight / (float) col2Height;
        float col2Scale = Math.min(col2Xscale, col2Yscale);
        float col3Xscale = thirdWidth / (float) maxWidthCol3;
        float col3Yscale = (float) screenHeight / (float) col3Height;
        float col3Scale = Math.min(col3Xscale, col3Yscale);

        scaleSize_3cols[0] = col1Scale;
        scaleSize_3cols[1] = col2Scale;
        scaleSize_3cols[2] = col3Scale;
        scaleSize_3cols[3] = thirdWay;
        scaleSize_3cols[4] = twoThirdWay;

        if (!songAutoScaleColumnMaximise) {
            // make 2 all the values of the smallest (but the same)
            float min = Math.min(scaleSize_3cols[0], Math.min(scaleSize_3cols[1], scaleSize_3cols[2]));
            scaleSize_3cols[0] = min;
            scaleSize_3cols[1] = min;
            scaleSize_3cols[2] = min;
        }

        // Get the planned heights of the column and get the biggest value
        int height1 = Math.round(col1Height*scaleSize_3cols[0]);
        int height2 = Math.round(col1Height*scaleSize_3cols[1]);
        int height3 = Math.round(col1Height*scaleSize_3cols[2]);
        scaleSize_3cols[5] = Math.max(height1,Math.max(height2,height3));

        return scaleSize_3cols;
    }

    private float[] setThreeColumns(ArrayList<View> sectionViews, LinearLayout column1,
                                 LinearLayout column2, LinearLayout column3, ArrayList<Integer> sectionWidths,
                                 ArrayList<Integer> sectionHeights, float[] scaleSize,
                                 float maxFontSize, float thirdWidth, int songSheetTitleHeight,
                                    boolean presentation, DisplayMetrics displayMetrics) {
        // Use 3 columns, but hide them all for now
        columnVisibility(column1, column2, column3, false, false, false);

        // Prepare the inner columns
        LinearLayout innerCol1 = newLinearLayout();
        LinearLayout innerCol2 = newLinearLayout();
        LinearLayout innerCol3 = newLinearLayout();

        int col1Height = getTotal(sectionHeights, 0, (int) scaleSize[3]);
        int col2Height = getTotal(sectionHeights, (int) scaleSize[3], (int) scaleSize[4]);
        int col3Height = getTotal(sectionHeights, (int) scaleSize[4], sectionHeights.size());

        // Get the actual scale which might be less due to the maxFontSize being exceeded
        float thisScale1 = setScaledView(innerCol1, scaleSize[0], maxFontSize);
        innerCol1.setLayoutParams(new LinearLayout.LayoutParams((int)(thirdWidth),LinearLayout.LayoutParams.WRAP_CONTENT));
        float thisScale2 = setScaledView(innerCol2, scaleSize[1], maxFontSize);
        innerCol2.setLayoutParams(new LinearLayout.LayoutParams((int)(thirdWidth),LinearLayout.LayoutParams.WRAP_CONTENT));
        float thisScale3 = setScaledView(innerCol3, scaleSize[2], maxFontSize);
        innerCol3.setLayoutParams(new LinearLayout.LayoutParams((int)(thirdWidth),LinearLayout.LayoutParams.WRAP_CONTENT));

        int color;

        for (int i = 0; i < scaleSize[3]; i++) {
            color = Color.TRANSPARENT;
            Drawable background = sectionViews.get(i).getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            FrameLayout frameLayout = newFrameLayout(color);
            if (sectionViews.get(i).getParent()!=null) {
                ((ViewGroup)sectionViews.get(i).getParent()).removeView(sectionViews.get(i));
            }
            frameLayout.setClipChildren(false);
            frameLayout.setClipToPadding(false);
            frameLayout.addView(sectionViews.get(i));

            // Now the view is created and has content, size it to the correct width
            frameLayout.setLayoutParams(new LinearLayout.LayoutParams((int)thirdWidth,LinearLayout.LayoutParams.WRAP_CONTENT));
            innerCol1.addView(frameLayout);
        }
        for (int i = (int) scaleSize[3]; i < (int) scaleSize[4]; i++) {
            color = Color.TRANSPARENT;
            Drawable background = sectionViews.get(i).getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            FrameLayout frameLayout = newFrameLayout(color);
            if (sectionViews.get(i).getParent()!=null) {
                ((ViewGroup)sectionViews.get(i).getParent()).removeView(sectionViews.get(i));
            }
            frameLayout.setClipChildren(false);
            frameLayout.setClipToPadding(false);
            frameLayout.addView(sectionViews.get(i));

            // Now the view is created and has content, size it to the correct width
            frameLayout.setLayoutParams(new LinearLayout.LayoutParams((int)thirdWidth,LinearLayout.LayoutParams.WRAP_CONTENT));
            innerCol2.addView(frameLayout);
        }
        for (int i = (int) scaleSize[4]; i < sectionViews.size(); i++) {
            color = Color.TRANSPARENT;
            Drawable background = sectionViews.get(i).getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            FrameLayout frameLayout = newFrameLayout(color);
            if (sectionViews.get(i).getParent()!=null) {
                ((ViewGroup)sectionViews.get(i).getParent()).removeView(sectionViews.get(i));
            }
            frameLayout.setClipChildren(false);
            frameLayout.setClipToPadding(false);
            frameLayout.addView(sectionViews.get(i));

            // Now the view is created and has content, size it to the correct width
            frameLayout.setLayoutParams(new LinearLayout.LayoutParams((int)thirdWidth,LinearLayout.LayoutParams.WRAP_CONTENT));
            innerCol3.addView(frameLayout);
        }

        columnVisibility(column1, column2, column3, true, true, true);

        int col1Width = getMaxValue(sectionWidths, 0, (int) scaleSize[3]);
        int col2Width = getMaxValue(sectionWidths, (int) scaleSize[3], (int) scaleSize[4]);
        int col3Width = getMaxValue(sectionWidths, (int) scaleSize[4], sectionWidths.size());

        resizeColumn(innerCol1, col1Width, col1Height, thisScale1);
        resizeColumn(innerCol2, col2Width, col2Height, thisScale2);
        resizeColumn(innerCol3, col3Width, col3Height, thisScale3);

        column1.addView(innerCol1);
        column2.addView(innerCol2);
        column3.addView(innerCol3);

        setMargins(column1, 0, padding);
        setMargins(column2, padding, padding);
        setMargins(column3, padding, 0);

        int col1h = (int) (col1Height*thisScale1);
        int col2h = (int) (col2Height*thisScale2);
        int col3h = (int) (col3Height*thisScale3);

        scaleSize[0] = thisScale1;
        scaleSize[1] = thisScale2;
        scaleSize[2] = thisScale3;
        scaleSize[5] = Math.max(col1h,Math.max(col2h,col3h));
        if (!presentation) {
            mainActivityInterface.updateSizes(displayMetrics.widthPixels,
                    (int) scaleSize[5] + songSheetTitleHeight);
        }
        return scaleSize;
    }

    // Now the stuff to read in pdf files (converts the pages to an image for displaying)
    // This uses Android built in PdfRenderer, so will only work on Lollipop+


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

        int bmpwidth = 0;
        int bmpheight = 0;

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

            return bitmap;
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // TODO Not working yet
    public Bitmap trimBitmap(Bitmap bmp) {
        int imgHeight = bmp.getHeight();
        int imgWidth = bmp.getWidth();

        //TRIM WIDTH - LEFT
        int startWidth = 0;
        for (int x = 0; x < imgWidth / 2; x++) {
            if (startWidth == 0) {
                for (int y = 0; y < imgHeight/2; y++) {
                    if (bmp.getPixel(x, y) != Color.TRANSPARENT) {
                        startWidth = x;
                        break;
                    }
                }
            } else {
                break;
            }
        }

        //TRIM WIDTH - RIGHT
        int endWidth = 0;
        for (int x = imgWidth - 1; x >= imgWidth / 2; x--) {
            if (endWidth == 0) {
                for (int y = 0; y < imgHeight/2; y++) {
                    if (bmp.getPixel(x, y) != Color.TRANSPARENT) {
                        endWidth = x;
                        break;
                    }
                }
            } else {
                break;
            }
        }

        //TRIM HEIGHT - TOP
        int startHeight = 0;
        for(int y = 0; y < imgHeight/2; y++) {
            if (startHeight == 0) {
                for (int x = 0; x < imgWidth/2; x++) {
                    if (bmp.getPixel(x, y) != Color.TRANSPARENT) {
                        startHeight = y;
                        break;
                    }
                }
            } else break;
        }

        //TRIM HEIGHT - BOTTOM
        int endHeight = 0;
        for(int y = imgHeight - 1; y >= imgHeight/2; y--) {
            if (endHeight == 0 ) {
                for (int x = 0; x < imgWidth/2; x++) {
                    if (bmp.getPixel(x, y) != Color.TRANSPARENT) {
                        endHeight = y;
                        break;
                    }
                }
            } else break;
        }

        Bitmap resizedBitmap = Bitmap.createBitmap(
                bmp,
                startWidth,
                startHeight,
                endWidth - startWidth,
                endHeight - startHeight
        );
        //bmp.recycle();
        return resizedBitmap;
    }



    // This stuff deals with the highlighter notes
    public String getHighlighterFilename(Song song, boolean portrait) {
        // The highlighter song file is encoded as FOLDER_FILENAME_{p or l LANDSCAPE}{if pdf _PAGENUMBER_}.png
        String filename = song.getFolder().replace("/", "_") + "_" +
                song.getFilename();

        if (song.getFiletype().equals("PDF")) {
            filename += "_" + song.getPdfPageCurrent();
        } else {
            if (portrait) {
                filename += "_p";
            } else {
                filename += "_l";
            }
        }
        filename += ".png";
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
            filename = getHighlighterFilename(mainActivityInterface.getSong(), true);
        } else {
            filename = getHighlighterFilename(mainActivityInterface.getSong(), false);
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
    public void editBoxToMultiline(MaterialEditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_ACTION_NONE);
        editText.setHorizontallyScrolling(true);
        editText.setAutoSizeTextTypeUniformWithConfiguration(8, 18, 1);
    }

    public void stretchEditBoxToLines(MaterialEditText editText, int minLines) {
        String[] lines = editText.getText().toString().split("\n");
        int num = lines.length;
        if (num > minLines) {
            editText.setLines(lines.length);
            editText.setMinLines(lines.length);
            editText.setLines(lines.length);
        } else {
            editText.setLines(minLines);
            editText.setMinLines(minLines);
            editText.setLines(minLines);
        }
    }

    public void splitTextByMaxChars(MaterialEditText editText, String text, int maxChars,
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

}