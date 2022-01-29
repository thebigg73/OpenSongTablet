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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MaterialEditText;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

// TODO Line and section breaks | and ||

public class ProcessSong {

    // The variables used for repeated song processing
    // TODO If the user updates these in the app, check they get updated here as well as the saved preferences!
    private final String TAG = "ProcessSong";
    private boolean addSectionSpace, blockShadow, displayBoldChordsHeadings,
            displayChords, displayLyrics,
            songAutoScaleColumnMaximise, songAutoScaleOverrideFull,
            songAutoScaleOverrideWidth, trimLines, trimSections;
    private float fontSize, fontSizeMax, fontSizeMin, blockShadowAlpha,
            lineSpacing, scaleHeadings, scaleChords, scaleComments;
    private String songAutoScale;

    // Update the processing preferences
    public void updateProcessingPreferences(Context c, MainActivityInterface mainActivityInterface) {
        addSectionSpace = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "addSectionSpace", true);
        blockShadow = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "blockShadow", false);
        blockShadowAlpha = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "blockShadowAlpha", 0.7f);
        //displayCapoChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "displayCapoChords", true);
        //displayCapoAndNativeChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "displayCapoAndNativeChords", false);
        displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "displayChords", true);
        displayLyrics = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "displayLyrics", true);
        displayBoldChordsHeadings = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "displayBoldChordsHeadings", false);
        songAutoScale = mainActivityInterface.getPreferences().getMyPreferenceString(c,"songAutoScale","W");
        songAutoScaleColumnMaximise = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"songAutoScaleColumnMaximise",true);
        songAutoScaleOverrideFull = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"songAutoScaleOverrideFull",true);
        songAutoScaleOverrideWidth = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"songAutoScaleOverrideWidth",false);
        trimLines = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"trimLines",true);
        trimSections = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"trimSections",true);

        fontSize = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "fontSize", 20f);
        fontSizeMax = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "fontSizeMax", 50f);
        fontSizeMin = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "fontSizeMin", 8f);
        lineSpacing = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "lineSpacing", 0.1f);
        scaleHeadings = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"scaleHeadings",0.6f);
        scaleChords = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"scaleChords",0.8f);
        scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"scaleComments",0.8f);
    }

    // Get some preferences back
    public float getScaleComments() {
        return scaleComments;
    }

    public Song initialiseSong(MainActivityInterface mainActivityInterface, String newFolder, String newFilename) {
        Song song = new Song();
        song.setFilename(newFilename);
        song.setFolder(newFolder);
        song.setSongid(mainActivityInterface.getCommonSQL().getAnySongId(newFolder, newFilename));
        return song;
    }

    // This deals with creating the song XML file
    public String getXML(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
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

        if (thisSong.getHasExtraStuff()) {
            String extraStuff = mainActivityInterface.getLoadSong().getExtraStuff(c, mainActivityInterface, thisSong);
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

    public String fixStartOfLines(String lyrics) {
        StringBuilder fixedlyrics = new StringBuilder();
        String[] lines = lyrics.split("\n");

        for (String line : lines) {
            if (line.length()==0 || !("[;. 123456789-".contains(line.substring(0,1)))) {
                line = " " + line;
            } else if (line.matches("^[0-9].*$") && line.length() > 1 && !line.startsWith(".", 1)) {
                // Multiline verse
                line = line.charAt(0) + ". " + line.substring(1);
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

    String determineLineTypes(String string, Context c) {
        String type;
        if (string.startsWith("[")) {
            type = "heading";
        } else if (string.startsWith(".")) {
            type = "chord";
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

    String howToProcessLines(int linenum, int totallines, String thislinetype, String nextlinetype, String previouslinetype) {
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

    String fixLineLength(String string, int newlength) {
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

    public String changeSlideHeadings(Context c, MainActivityInterface mainActivityInterface, String s) {
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

    public String parseLyrics(Context c, Locale locale, Song song) {
        if (locale==null) {
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

    private String trimOutLineIdentifiers(Context c, MainActivityInterface mainActivityInterface,
                                          String linetype, String string) {
        switch (linetype) {
            case "heading":
                string = beautifyHeading(c, mainActivityInterface, string);
                if (!mainActivityInterface.getSong().getSongSectionHeadings().contains(string)) {
                    mainActivityInterface.getSong().getSongSectionHeadings().add(string);
                }
                break;
            case "chord":
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

    public String beautifyHeading(Context c, MainActivityInterface mainActivityInterface, String line) {
        boolean annotated = line.contains("-]");

        if (line.equals("")) {
            return "";
        }

        if (annotated) {
            line = line
                    .replace("[","")
                    .replace(" -]", "")
                    .replace("-]", "")
                    .replace ("]","")
                    .trim();
        } else {
            line = line
                    .replace("[","")
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
        line = replaceBadHeadings(mainActivityInterface, line,"verse", "V");
        line = replaceBadHeadings(mainActivityInterface, line, "prechorus","P");
        line = replaceBadHeadings(mainActivityInterface, line, "pre-chorus","P");
        line = replaceBadHeadings(mainActivityInterface, line, "chorus","C");
        line = replaceBadHeadings(mainActivityInterface, line, "tag","T");
        line = replaceBadHeadings(mainActivityInterface, line, "bridge","B");

        return line.trim();
    }
    private String replaceBadHeadings(MainActivityInterface mainActivityInterface, String line, String fix, String replacement) {
        if (line.contains(fix) || line.contains(fix.toUpperCase(mainActivityInterface.getLocale()))) {
            line = line.replace(fix+" ",replacement).
                    replace(fix.toUpperCase(mainActivityInterface.getLocale())+" ",replacement).
                    replace(fix,replacement).
                    replace(fix.toUpperCase(mainActivityInterface.getLocale()),replacement);
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

    // This is used for preparing the lyrics as views
    // When processing the lyrics, chords+lyrics or chords+comments or multiple chords+chords are processed
    // as groups of lines and returned as a TableLayout containing two or more rows to allow alignment


    // Splitting the song up in to manageable chunks
    private String makeGroups(String string, boolean displayChords) {
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
                    sb.append("____groupline____").append(lines[nl]);
                    while (stillworking) {
                        // Keep going for multiple lines to be added
                        if (shouldNextLineBeAdded(nl + 1, lines, false)) {
                            i = nl;
                            nl++;
                            sb.append("____groupline____").append(lines[nl]);
                        } else {
                            i++;
                            stillworking = false;
                        }
                    }
                    // IV - Removed 'While the next line is still a chordline add this line' as breaks highlighting
                }
            } else if (!lines[i].startsWith(".") && !displayChords){
                // Tidy it up
                lines[i] = lines[i].trim().replace("_","");
                lines[i] = " " + lines[i];
                sb.append("\n").append(lines[i]);
            } else if (displayLyrics && !lines[i].startsWith(".")) {
                sb.append("\n").append(lines[i]);
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
        string = string.replace("§","\n____SPLIT____").
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

    private TableLayout groupTable(Context c, MainActivityInterface mainActivityInterface,
                                   String string, int lyricColor, int chordColor,
                                   int highlightChordColor, boolean presentation) {
        TableLayout tableLayout = newTableLayout(c);

        // Split the group into lines
        String[] lines = string.split("____groupline____");

        // Line 0 is the chord line.  All other lines need to be at least this size
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
            if (lines[1].startsWith(".")) {
                // IV - A chord line follows so position this line referring only to itself
                chordPos = getChordPositions(lines[0], lines[0]);
            } else {
                chordPos = getChordPositions(lines[0], lines[1]);
            }
            for (String p:chordPos) {
                pos.add(Integer.valueOf(p));
            }
        }

        String linetype;
        String lastlinetype = "";

        // Now we have the sizes, split into individual TextViews inside a TableRow for each line
        for (int t = 0; t < lines.length; t++) {
            TableRow tableRow = newTableRow(c);
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

            Typeface typeface = getTypeface(mainActivityInterface, presentation, linetype);
            float size = getFontSize(linetype);
            int color = getFontColor(linetype, lyricColor, chordColor);
            int startpos = 0;
            for (int endpos : pos) {
                if (endpos != 0) {
                    TextView textView = newTextView(c, linetype, typeface, size, color);
                    String str = lines[t].substring(startpos, endpos);
                    if (startpos == 0) {
                        str = trimOutLineIdentifiers(c, mainActivityInterface, linetype, str);
                    }
                    if (linetype.equals("chord") && highlightChordColor != 0x00000000) {
                        textView.setText(highlightChords(str, highlightChordColor));
                    } else if (linetype.equals("lyric")) {
                        // TODO
                        // IV - This will need more complexity depending on mode and if showing chords
                        textView.setText(str.replaceAll("[|_]"," "));
                    } else {
                        textView.setText(str);
                    }
                    tableRow.addView(textView);
                    startpos = endpos;
                }
            }
            // Add the final position
            TextView textView = newTextView(c, linetype, typeface, size, color);
            String str = lines[t].substring(startpos);
            if (str.startsWith(".")) {
                str = str.replaceFirst(".", "");
            }
            if (linetype.equals("chord") && highlightChordColor != 0x00000000) {
                textView.setText(highlightChords(str, highlightChordColor));
            } else if (linetype.equals("lyric")) {
                // TODO
                // IV - This will need more complexity depending on mode and if showing chords
                textView.setText(str.replaceAll("[|_]"," "));
            } else {
                textView.setText(str);
            }
            tableRow.addView(textView);
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

    private boolean isMultiLineFormatSong(MainActivityInterface mainActivityInterface, String string) {
        // Best way to determine if the song is in multiline format is
        // Look for [v] or [c] case insensitive
        // And it needs to be followed by a line starting with 1 and 2
        if (string!=null) {
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

    private String fixMultiLineFormat(Context c, MainActivityInterface mainActivityInterface, String string) {

        if (!mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "multiLineVerseKeepCompact", false) &&
                isMultiLineFormatSong(mainActivityInterface, string)) {
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

                boolean mlv = isMultiLine(mainActivityInterface, l, l_1, l_2, "v");
                boolean mlc = isMultiLine(mainActivityInterface, l, l_1, l_2, "c");

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
        } else {
            // Not multiline format, or not wanting to expand it
            return string;
        }
    }

    private boolean isMultiLine(MainActivityInterface mainActivityInterface, String l, String l_1, String l_2, String type) {
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


    public void matchPresentationOrder(MainActivityInterface mainActivityInterface, Song song) {
        // presentationOrder probably looks like "Intro V1 V2 C V3 C C Guitar Solo C Outro"
        // We need to identify the sections in the song that are in here
        // What if sections aren't in the song (e.g. Intro V2 and Outro)
        // The other issue is that custom tags (e.g. Guitar Solo) can have spaces in them

        // If we've already done this processing, don't do it again.
        if (song.getPresoOrderSongSections()==null || song.getPresoOrderSongSections().size()==0) {

            if (mainActivityInterface.getPresenterSettings().getUsePresentationOrder() &&
                    song.getPresentationorder() != null && !song.getPresentationorder().isEmpty()) {
                // Update to match the presentation order
                ArrayList<String> newSections = new ArrayList<>();
                ArrayList<String> newHeaders = new ArrayList<>();

                StringBuilder tempPresentationOrder = new StringBuilder(song.getPresentationorder() + " ");
                StringBuilder errors = new StringBuilder();

                // Go through each tag in the song
                for (String tag : song.getSongSectionHeadings()) {
                    Log.d(TAG, "PresentationOrder:tag=" + tag);
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
                        errors.append(tag).append(" - not found in presentation order");
                    }
                }
                Log.d(TAG, "PresentationOrder: tempPresentationOrder=" + tempPresentationOrder);
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

                for (String bit : tempPresOrderArray) {
                    Log.d(TAG, "PresentationOrder: tempPresOrderArray[bit]=" + bit);
                }

                // Go through the tempPresOrderArray and add the sections back together
                for (String aTempPresOrderArray : tempPresOrderArray) {
                    if (!aTempPresOrderArray.equals("")) {
                        for (int a = 0; a < song.getSongSectionHeadings().size(); a++) {
                            if (song.getSongSectionHeadings().get(a).trim().equals(aTempPresOrderArray.trim())) {
                                newSections.add(song.getSongSections().get(a));
                                newHeaders.add(song.getSongSectionHeadings().get(a));
                            }
                        }
                    }
                }

                // Display any errors
                mainActivityInterface.getShowToast().doIt(errors.toString());
                song.setPresoOrderSongSections(newSections);
                song.setPresoOrderSongHeadings(newHeaders);
            } else {
                // Not using presentation order, so just return what we have
                song.setPresoOrderSongSections(song.getSongSections());
                song.setPresoOrderSongHeadings(song.getSongSectionHeadings());
            }
        }
    }


    private TextView lineText(Context c, MainActivityInterface mainActivityInterface, String linetype,
                              String string, Typeface typeface, float size, int color,
                              int highlightHeadingColor, int highlightChordColor, boolean presentation) {
        TextView textView = newTextView(c, linetype, typeface, size, color);
        if (presentation) {
            textView.setGravity(mainActivityInterface.getPresenterSettings().getPresoLyricsAlign());
        }
        String str = trimOutLineIdentifiers(c, mainActivityInterface, linetype, string);
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
                textView.setText(str.replaceAll("[|_]"," "));
            } else {
                textView.setText(str);
            }
        }
        return textView;
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

    private void processSongIntoSections(Context c, MainActivityInterface mainActivityInterface,
                                         Song song, boolean presentation) {
        // First we process the song (could be the loaded song, or a temp song - that's why we take a reference)
        // 1. Get a temporary version of the lyrics (as we are going to process them)
        String lyrics = song.getLyrics();

        // 2. Check for multiline verse formatting e.g. [V] 1. 2. etc.
        lyrics = fixMultiLineFormat(c, mainActivityInterface, lyrics);

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
        if (presentation || mainActivityInterface.getMode().equals("Stage")) {
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
        for (String line : (lyrics+"¶").split("\n")) {
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
                .replace("\n ---","\n[]")
                .replace("\n---","\n[]")
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
        ArrayList<String> songSections = new ArrayList<>();
        for (String thisSection:sections) {
            thisSection = thisSection.replace("____groupline____","\n");
            if (!thisSection.trim().isEmpty()) {
                songSections.add(thisSection);
            }
        }
        song.setSongSections(songSections);
    }

    public ArrayList<View> setSongInLayout(Context c, MainActivityInterface mainActivityInterface,
                                           Song song, boolean asPDF, boolean presentation) {
        ArrayList<View> sectionViews = new ArrayList<>();
        ArrayList<Integer> sectionColors = new ArrayList<>();

        // First we process the song (could be the loaded song, or a temp song - that's why we take a reference)
        processSongIntoSections(c,mainActivityInterface,song,presentation);

        // Now we deal with creating the views from the available sections
        int backgroundColor;
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

        // We also consider any presentation order that is set
        matchPresentationOrder(mainActivityInterface,song);

        for (int sect = 0; sect < song.getPresoOrderSongSections().size(); sect++) {
            String section = song.getPresoOrderSongSections().get(sect);
            section = section.replace("____groupline____","\n");
            Log.d(TAG,"section to process view: "+section);
            if (!section.isEmpty()) {
                if (trimSections) {
                    section = section.trim();
                }
                if (!presentation && addSectionSpace && sect != (song.getPresoOrderSongSections().size() - 1)) { // Don't do for last section
                    section = section + "\n ";
                }
                LinearLayout linearLayout = newLinearLayout(c); // transparent color
                if (presentation) {
                    linearLayout.setGravity(mainActivityInterface.getPresenterSettings().getPresoLyricsAlign());
                }

                if (!mainActivityInterface.getMode().equals("Performance") && blockShadow) {
                    linearLayout.setBackgroundColor(mainActivityInterface.getMyThemeColors().getPresoShadowColor());
                    linearLayout.setBackgroundColor(getColorWithAlpha(mainActivityInterface.
                            getMyThemeColors().getPresoShadowColor(), blockShadowAlpha));
                }

                // Add this section to the array (so it can be called later for presentation)
                if (!section.trim().isEmpty()) {
                    // Now split by line
                    String[] lines = section.split("\n");
                    for (String line : lines) {
                        // Get the text stylings
                        String linetype = getLineType(line);
                        if (presentation && linetype.equals("heading")) {
                            // Don't need this for the presentation view
                            line = "";
                        }
                        if (!asPDF && !presentation && (linetype.equals("heading") || linetype.equals("comment") || linetype.equals("tab"))) {
                            backgroundColor = getBGColor(c, mainActivityInterface, line);
                        }
                        Typeface typeface = getTypeface(mainActivityInterface, presentation, linetype);
                        float size = getFontSize(linetype);
                        if (!asPDF && !presentation) {
                            textColor = getFontColor(linetype, mainActivityInterface.getMyThemeColors().
                                    getLyricsTextColor(), mainActivityInterface.getMyThemeColors().getLyricsChordsColor());
                        }

                        if (line.contains("____groupline____")) {
                            if (asPDF) {
                                linearLayout.addView(groupTable(c, mainActivityInterface, line, Color.BLACK, Color.BLACK,
                                        Color.TRANSPARENT, false));
                            } else if (presentation) {
                                linearLayout.addView(groupTable(c, mainActivityInterface, line,
                                        mainActivityInterface.getMyThemeColors().getPresoFontColor(),
                                        mainActivityInterface.getMyThemeColors().getPresoFontColor(),
                                        mainActivityInterface.getMyThemeColors().getHighlightChordColor(), true));
                            } else {
                                linearLayout.addView(groupTable(c, mainActivityInterface, line,
                                        mainActivityInterface.getMyThemeColors().getLyricsTextColor(),
                                        mainActivityInterface.getMyThemeColors().getLyricsChordsColor(),
                                        mainActivityInterface.getMyThemeColors().getHighlightChordColor(), false));
                            }
                        } else {
                            if (asPDF) {
                                linearLayout.addView(lineText(c, mainActivityInterface, linetype, line, typeface,
                                        size, textColor, Color.TRANSPARENT, Color.TRANSPARENT, presentation));

                            } else if (!presentation || !line.isEmpty()) {
                                linearLayout.addView(lineText(c, mainActivityInterface, linetype, line, typeface,
                                        size, textColor,
                                        mainActivityInterface.getMyThemeColors().getHighlightHeadingColor(),
                                        mainActivityInterface.getMyThemeColors().getHighlightChordColor(), presentation));
                            }
                        }
                    }

                    linearLayout.setBackgroundColor(backgroundColor);
                    sectionColors.add(backgroundColor);

                    sectionViews.add(linearLayout);
                }
            }
        }

        mainActivityInterface.setSectionColors(sectionColors);
        return sectionViews;
    }

    // Get properties for creating the views
    private Typeface getTypeface(MainActivityInterface mainActivityInterface, boolean presentation, String string) {
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

    private int getFontColor(String string, int lyricColor, int chordColor) {
        if (string.equals("chord")) {
            return chordColor;
        } else {
            return lyricColor;
        }
    }

    private float getFontSize(String string) {
        float f = defFontSize;
        switch (string) {
            case "chord":
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

    private int getBGColor(Context c, MainActivityInterface mainActivityInterface, String line) {
        if (line.startsWith(";")) {
            return mainActivityInterface.getMyThemeColors().getLyricsCommentColor();
        } else if (beautifyHeading(c, mainActivityInterface, line).contains(c.getString(R.string.verse))) {
            return mainActivityInterface.getMyThemeColors().getLyricsVerseColor();
        } else if (beautifyHeading(c, mainActivityInterface, line).contains(c.getString(R.string.prechorus))) {
            return mainActivityInterface.getMyThemeColors().getLyricsPreChorusColor();
        } else if (beautifyHeading(c, mainActivityInterface, line).contains(c.getString(R.string.chorus))) {
            return mainActivityInterface.getMyThemeColors().getLyricsChorusColor();
        } else if (beautifyHeading(c, mainActivityInterface, line).contains(c.getString(R.string.bridge))) {
            return mainActivityInterface.getMyThemeColors().getLyricsBridgeColor();
        } else if (beautifyHeading(c, mainActivityInterface, line).contains(c.getString(R.string.tag))) {
            return mainActivityInterface.getMyThemeColors().getLyricsTagColor();
        } else if (beautifyHeading(c, mainActivityInterface, line).contains(c.getString(R.string.custom))) {
            return mainActivityInterface.getMyThemeColors().getLyricsCustomColor();
        } else {
            return mainActivityInterface.getMyThemeColors().getLyricsVerseColor();
        }
    }

    public static int getColorWithAlpha(int color, float ratio) {
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(alpha, r, g, b);
    }


    // Creating new blank views
    private TableLayout newTableLayout(Context c) {
        TableLayout tableLayout = new TableLayout(c);
        tableLayout.setPadding(0, 0, 0, 0);
        tableLayout.setClipChildren(false);
        tableLayout.setClipToPadding(false);
        tableLayout.setDividerPadding(0);
        return tableLayout;
    }

    private TableRow newTableRow(Context c) {
        TableRow tableRow = new TableRow(c);
        tableRow.setPadding(0, 0, 0, 0);
        tableRow.setDividerPadding(0);
        return tableRow;
    }

    private LinearLayout newLinearLayout(Context c) {
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

    private TextView newTextView(Context c, String linetype, Typeface typeface, float size, int color) {
        TextView textView = new TextView(c);
        if (trimLines && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            int trimval;
            if (linetype.equals("chord")) {
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
                textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
            } else {
                textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        }
        if (linetype.equals("chord") && displayBoldChordsHeadings) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        }
        return textView;
    }

    private FrameLayout newFrameLayout(Context c, int color) {
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


    // Stuff for resizing/scaling
    private int padding = 8;
    private final float defFontSize = 8.0f;
    private String thisAutoScale;

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

    private void setScaledView(LinearLayout innerColumn, float scaleSize, float maxFontSize) {
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

    private void resizeColumn(LinearLayout column, int startWidth, int startHeight, float scaleSize) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (startWidth * scaleSize),
                (int) (startHeight * scaleSize));
        column.setLayoutParams(lp);
    }

    private int howManyColumnsAreBest(float col1, float[] col2, float[] col3, String autoScale,
                                      float fontSizeMin, boolean songAutoScaleOverrideFull) {
        // There's a few things to consider here.  Firstly, if scaling is off, best is 1 column.
        // If we are overriding full scale to width only, or 1 col to off, best is 1 column.

        if (autoScale.equals("N") || autoScale.equals("W")) {
            return 1;
        }

        Log.d(TAG,"col1:" + col1 + "  col2[0]:" + col2[0] + "  col2[1]:" + col2[1] +
                "  col3[0]:" + col3[0] + "  col3[1]:" + col3[1] + "  col3[2]:" + col3[2]);

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
        Log.d(TAG,"best="+best);
        Log.d(TAG,"col2[2]="+col2[2]);
        // Default font size is 14sp when drawing. If scaling takes this below the min font Size, override back to 1 column
        if (best == 2) {
            if (col2[2] == 0) {
                return 1;
            }
            float newFontSize2Col = defFontSize * col2best;
            Log.d(TAG,"defFontSize="+defFontSize+"  col2Best="+col2best+"  newFontSize2Col="+newFontSize2Col);
            Log.d(TAG,"songAutoScaleOverrideFull="+songAutoScaleOverrideFull+"  fontSizeMin="+fontSizeMin+"  newFontSize2Col="+newFontSize2Col);

            if (songAutoScaleOverrideFull && newFontSize2Col < fontSizeMin) {
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


    // These are called from the VTO listener - draw the stuff to the screen as 1,2 or 3 columns
    // This then returns the best (largest) scaling size as a float
    public float addViewsToScreen(Context c, MainActivityInterface mainActivityInterface,
                                  RelativeLayout pageHolder,
                                  LinearLayout songView, LinearLayout songSheetView,
                                  int screenWidth, int screenHeight, LinearLayout column1,
                                  LinearLayout column2, LinearLayout column3) {
        // Now we have all the sizes in, determines the best way to show the song
        // This will be single, two or three columns.  The best one will be the one
        // which gives the best scale size

        // Clear and reset the view's scaling
        resetRelativeLayout(pageHolder);
        clearAndResetLinearLayout(songView, false);
        clearAndResetLinearLayout(songSheetView,false);
        pageHolder.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT));
        songView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        clearAndResetLinearLayout(column1, true);
        clearAndResetLinearLayout(column2, true);
        clearAndResetLinearLayout(column3, true);

        // Set the padding and boxpadding from dp to px
        float scale = c.getResources().getDisplayMetrics().density;
        padding = (int) (8 * scale);

        int currentWidth = getMaxValue(mainActivityInterface.getSectionWidths(), 0, mainActivityInterface.getSectionWidths().size());
        int currentHeight = getTotal(mainActivityInterface.getSectionHeights(), 0, mainActivityInterface.getSectionHeights().size());

        // Include the songSheetView if it isn't empty
        int songSheetTitleHeight = mainActivityInterface.getSongSheetTitleLayout().getHeight();
        if (songSheetTitleHeight>0) {
            Log.d(TAG,"songSheetTitleHeight="+songSheetTitleHeight);
            currentHeight = currentHeight + songSheetTitleHeight;
            songSheetView.addView(mainActivityInterface.getSongSheetTitleLayout());
        } else {
            column1.setPadding(0,0,0,0);
        }

        thisAutoScale = songAutoScale;

        // All scaling types need to process the single column view, either to use it or compare to 2/3 columns
        float[] scaleSize_2cols = new float[3];
        float[] scaleSize_3cols = new float[4];
        if (songAutoScale.equals("Y")) {
            // Figure out two and three columns.  Only do this if we need to to save processing time.
            scaleSize_2cols = col2Scale(screenWidth, screenHeight, currentHeight, songAutoScaleColumnMaximise, mainActivityInterface.getSectionWidths(), mainActivityInterface.getSectionHeights());
            scaleSize_3cols = col3Scale(screenWidth, screenHeight, currentHeight, songAutoScaleColumnMaximise, mainActivityInterface.getSectionWidths(), mainActivityInterface.getSectionHeights());
        }

        float scaleSize_1col = col1Scale(screenWidth, screenHeight, currentWidth, currentHeight);

        // Now we've used the views in measure, we need to remove them from the test pane, so we can reallocate them
        //testPane.removeAllViews();

        // Now decide if 1,2 or 3 columns is best
        int howmany = howManyColumnsAreBest(scaleSize_1col, scaleSize_2cols, scaleSize_3cols, songAutoScale, fontSizeMin, songAutoScaleOverrideFull);

        switch (howmany) {
            case 1:
                // If we are using one column and resizing to width only, change the scale size
                if (songAutoScale.equals("W") || thisAutoScale.equals("W")) {
                    scaleSize_1col = (float) screenWidth / (float) currentWidth;
                    if (defFontSize * scaleSize_1col < fontSizeMin && songAutoScaleOverrideWidth) {
                        thisAutoScale = "N";
                    }
                }
                // If autoscale is off, scale to the desired fontsize
                if (songAutoScale.equals("N") || thisAutoScale.equals("N")) {
                    scaleSize_1col = fontSize / defFontSize;
                }
                setOneColumn(c, mainActivityInterface, mainActivityInterface.getSectionViews(), column1, column2, column3, currentWidth, currentHeight, scaleSize_1col, fontSizeMax);
                break;

            case 2:
                setTwoColumns(c, mainActivityInterface, mainActivityInterface.getSectionViews(), column1, column2, column3, mainActivityInterface.getSectionHeights(), scaleSize_2cols, fontSizeMax, (int) ((float) screenWidth / 2.0f - padding));
                break;

            case 3:
                setThreeColumns(c, mainActivityInterface, mainActivityInterface.getSectionViews(), column1, column2, column3, mainActivityInterface.getSectionWidths(), mainActivityInterface.getSectionHeights(), scaleSize_3cols, fontSizeMax);
                break;
        }
        // If we need to move column1 down/up due to potential songSheet and it's scaling, do it
        setScaledView(songSheetView, scaleSize_1col, fontSizeMax);
        column1.setPadding(0,(int)(songSheetTitleHeight*scaleSize_1col),0,0);
        return scaleSize_1col;
    }


    // 1 column stuff
    private float col1Scale(int screenWidth, int screenHeight, int viewWidth, int viewHeight) {
        float x_scale = (float) screenWidth / (float) viewWidth;
        float y_scale = (float) screenHeight / (float) viewHeight;
        return Math.min(x_scale, y_scale);
    }

    private void setOneColumn(Context c, MainActivityInterface mainActivityInterface, ArrayList<View> sectionViews, LinearLayout column1, LinearLayout column2, LinearLayout column3,
                              int currentWidth, int currentHeight, float scaleSize, float maxFontSize) {
        columnVisibility(column1, column2, column3, false, false, false);
        LinearLayout innerCol1 = newLinearLayout(c);

        int color;
        // For each section, add it to a relayivelayout to deal with the background colour.
        for (View v : sectionViews) {
            color = Color.TRANSPARENT;
            Drawable background = v.getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            FrameLayout frameLayout = newFrameLayout(c, color);
            if (v.getParent()!=null) {
                ((ViewGroup)v.getParent()).removeAllViews();
            }
            frameLayout.addView(v);
            innerCol1.addView(frameLayout);
        }
        setScaledView(innerCol1, scaleSize, maxFontSize);
        resizeColumn(innerCol1, currentWidth, currentHeight, scaleSize);
        setMargins(column1, 0, 0);
        column1.setPadding(0, 0, 0, 0);
        column1.setClipChildren(false);
        column1.setClipToPadding(false);
        column1.addView(innerCol1);
        columnVisibility(column1, column2, column3, true, false, false);
        mainActivityInterface.updateSizes((int)(currentWidth * scaleSize),(int) (currentHeight * scaleSize));
    }


    // 2 column stuff
    private float[] col2Scale(int screenWidth, int screenHeight, int totalViewHeight, boolean songAutoScaleColumnMaximise,
                              ArrayList<Integer> viewWidth, ArrayList<Integer> viewHeight) {
        float[] colscale = new float[3];

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

        Log.d(TAG,"preHalfway="+preHalfWay+"  postHalfway="+postHalfWay);
        Log.d(TAG,"col1Height="+col1Height+"  col2Height="+col2Height);

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

        Log.d(TAG,"preScaleTotal="+preScaleTotal + "  postScaleTotal="+postScaleTotal);
        if (preScaleTotal >= postScaleTotal) {
            colscale[0] = preCol1Scale;
            colscale[1] = preCol2Scale;
            colscale[2] = preHalfWay;
        } else {
            colscale[0] = postCol1Scale;
            colscale[1] = postCol2Scale;
            colscale[2] = postHalfWay;
        }

        if (!songAutoScaleColumnMaximise) {
            // make 2 all the values of the smallest (but the same)
            float min = Math.min(colscale[0], colscale[1]);
            colscale[0] = min;
            colscale[1] = min;
        }

        return colscale;
    }

    private void setTwoColumns(Context c, MainActivityInterface mainActivityInterface,
                               ArrayList<View> sectionViews, LinearLayout column1,
                               LinearLayout column2, LinearLayout column3,
                               ArrayList<Integer> sectionHeights, float[] scaleSize,
                               float maxFontSize, int halfwidth) {
        // Use 2 column
        columnVisibility(column1, column2, column3, false, false, false);
        LinearLayout innerCol1 = newLinearLayout(c);
        LinearLayout innerCol2 = newLinearLayout(c);

        int col1Height = getTotal(sectionHeights, 0, (int) scaleSize[2]);
        int col2Height = getTotal(sectionHeights, (int) scaleSize[2], sectionHeights.size());
        setScaledView(innerCol1, scaleSize[0], maxFontSize);
        setScaledView(innerCol2, scaleSize[1], maxFontSize);
        resizeColumn(innerCol1, halfwidth, col1Height, 1);
        resizeColumn(innerCol2, halfwidth, col2Height, 1);

        int color;
        for (int i = 0; i < scaleSize[2]; i++) {
            color = Color.TRANSPARENT;
            Drawable background = sectionViews.get(i).getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            FrameLayout frameLayout = newFrameLayout(c, color);
            frameLayout.addView(sectionViews.get(i));
            innerCol1.addView(frameLayout);
        }
        for (int i = (int) scaleSize[2]; i < sectionViews.size(); i++) {
            color = Color.TRANSPARENT;
            Drawable background = sectionViews.get(i).getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            FrameLayout frameLayout = newFrameLayout(c, color);
            frameLayout.addView(sectionViews.get(i));
            innerCol2.addView(frameLayout);
        }
        columnVisibility(column1, column2, column3, true, true, false);
        column1.addView(innerCol1);
        column2.addView(innerCol2);
        setMargins(column1, 0, padding);
        setMargins(column2, padding, 0);
        int col1h = (int) (col1Height * scaleSize[0]);
        int col2h = (int) (col2Height * scaleSize[1]);
        mainActivityInterface.updateSizes(-1,Math.max(col1h, col2h));
    }

    // 3 column stuff
    private float[] col3Scale(int screenWidth, int screenHeight, int totalViewHeight,
                              boolean songAutoScaleColumnMaximise, ArrayList<Integer> viewWidth,
                              ArrayList<Integer> viewHeight) {
        float[] colscale = new float[5];

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

        colscale[0] = col1Scale;
        colscale[1] = col2Scale;
        colscale[2] = col3Scale;
        colscale[3] = thirdWay;
        colscale[4] = twoThirdWay;

        if (!songAutoScaleColumnMaximise) {
            // make 2 all the values of the smallest (but the same)
            float min = Math.min(colscale[0], Math.min(colscale[1], colscale[2]));
            colscale[0] = min;
            colscale[1] = min;
            colscale[2] = min;
        }

        return colscale;
    }

    private void setThreeColumns(Context c, MainActivityInterface mainActivityInterface, ArrayList<View> sectionViews, LinearLayout column1,
                                 LinearLayout column2, LinearLayout column3, ArrayList<Integer> sectionWidths,
                                 ArrayList<Integer> sectionHeights, float[] scaleSize,
                                 float maxFontSize) {
        // Use 2 column
        columnVisibility(column1, column2, column3, false, false, false);
        LinearLayout innerCol1 = newLinearLayout(c);
        LinearLayout innerCol2 = newLinearLayout(c);
        LinearLayout innerCol3 = newLinearLayout(c);
        int color;
        for (int i = 0; i < scaleSize[3]; i++) {
            color = Color.TRANSPARENT;
            Drawable background = sectionViews.get(i).getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            FrameLayout frameLayout = newFrameLayout(c, color);
            frameLayout.addView(sectionViews.get(i));
            innerCol1.addView(frameLayout);
        }
        for (int i = (int) scaleSize[3]; i < (int) scaleSize[4]; i++) {
            color = Color.TRANSPARENT;
            Drawable background = sectionViews.get(i).getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            FrameLayout frameLayout = newFrameLayout(c, color);
            frameLayout.addView(sectionViews.get(i));
            innerCol2.addView(frameLayout);
        }
        for (int i = (int) scaleSize[4]; i < sectionViews.size(); i++) {
            color = Color.TRANSPARENT;
            Drawable background = sectionViews.get(i).getBackground();
            if (background instanceof ColorDrawable) {
                color = ((ColorDrawable) background).getColor();
            }
            FrameLayout frameLayout = newFrameLayout(c, color);
            frameLayout.addView(sectionViews.get(i));
            innerCol3.addView(frameLayout);
        }
        int col1Width = getMaxValue(sectionWidths, 0, (int) scaleSize[3]);
        int col1Height = getTotal(sectionHeights, 0, (int) scaleSize[3]);
        int col2Width = getMaxValue(sectionWidths, (int) scaleSize[3], (int) scaleSize[4]);
        int col2Height = getTotal(sectionHeights, (int) scaleSize[3], (int) scaleSize[4]);
        int col3Width = getMaxValue(sectionWidths, (int) scaleSize[4], sectionWidths.size());
        int col3Height = getTotal(sectionHeights, (int) scaleSize[4], sectionHeights.size());
        setScaledView(innerCol1, scaleSize[0], maxFontSize);
        setScaledView(innerCol2, scaleSize[1], maxFontSize);
        setScaledView(innerCol3, scaleSize[2], maxFontSize);
        resizeColumn(innerCol1, col1Width, col1Height, scaleSize[0]);
        resizeColumn(innerCol2, col2Width, col2Height, scaleSize[1]);
        resizeColumn(innerCol3, col3Width, col3Height, scaleSize[2]);
        columnVisibility(column1, column2, column3, true, true, true);
        column1.addView(innerCol1);
        column2.addView(innerCol2);
        column3.addView(innerCol3);
        int col1h = (int) (col1Height * scaleSize[0]);
        int col2h = (int) (col2Height * scaleSize[1]);
        int col3h = (int) (col3Height * scaleSize[2]);
        mainActivityInterface.updateSizes(-1,Math.max(col1h, Math.max(col2h, col3h)));
    }


    // Now the stuff to read in pdf files (converts the pages to an image for displaying)
    // This uses Android built in PdfRenderer, so will only work on Lollipop+


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Bitmap getBitmapFromPDF(Context c, MainActivityInterface mainActivityInterface,
                                   String folder, String filename, int page, int allowedWidth,
                                   int allowedHeight, String scale) {
        Bitmap bmp = null;

        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Songs", folder, filename);

        // FileDescriptor for file, it allows you to close file when you are done with it
        ParcelFileDescriptor parcelFileDescriptor = getPDFParcelFileDescriptor(c, uri);

        // Get PDF renderer
        PdfRenderer pdfRenderer = getPDFRenderer(parcelFileDescriptor);

        // Get the page count
        mainActivityInterface.getPDFSong().setPdfPageCount(getPDFPageCount(pdfRenderer));

        // Set the current page number
        page = getCurrentPage(mainActivityInterface, page);

        if (parcelFileDescriptor != null && pdfRenderer != null && mainActivityInterface.getPDFSong().getPdfPageCount() > 0) {
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

    public ParcelFileDescriptor getPDFParcelFileDescriptor(Context c, Uri uri) {
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

    public int getCurrentPage(MainActivityInterface mainActivityInterface, int page) {
        if (!mainActivityInterface.getSong().getShowstartofpdf()) {
            // This is to deal with swiping backwards through songs, show the last page first!
            page = mainActivityInterface.getSong().getPdfPageCount() - 1;
            mainActivityInterface.getSong().setShowstartofpdf(true);
            mainActivityInterface.getSong().setCurrentSection(mainActivityInterface.getSong().getPdfPageCount()-1);
        }
        if (page >= mainActivityInterface.getSong().getPdfPageCount()) {
            mainActivityInterface.getSong().setPdfPageCurrent(0);
            mainActivityInterface.getSong().setCurrentSection(0);
            page = 0;
        } else {
            mainActivityInterface.getSong().setPdfPageCurrent(page);
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
    }

    // Not working yet
    /*public Bitmap trimBitmap(Bitmap bmp) {
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
    }*/

    // These functions deal with nearby navigations
    public int getNearbySection(String incoming) {
        //TODO
        return -1;
    }

    public ArrayList<String> getNearbyIncoming(String incoming) {
        // TODO
        ArrayList<String> arrayList = new ArrayList<>();
        return arrayList;
    }


    // This stuff deals with the highlighter notes
    public String getHighlighterFilename(Song song, boolean portrait) {
        // The highlighter song file is encoded as FOLDER_FILENAME_{p or l LANDSCAPE}{if pdf _PAGENUMBER_}.png
        String filename = song.getFolder().replace("/", "_") + "_" +
                song.getFilename();
        if (portrait) {
            filename += "_p";
        } else {
            filename += "_l";
        }
        filename += ".png";
        return filename;
    }

    public ArrayList<String> getInfoFromHighlighterFilename (String filename) {
        ArrayList<String> bits = new ArrayList<>();
        String[] filebits = filename.split("_");
        int namepos=1;
        for (int x=0;x<filebits.length;x++) {
            if (filebits[x].equals("p") || filebits[x].equals("l")) {
                // the pos is before this
                namepos = x-1;
            }
        }
        if (namepos>0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int x=0; x<namepos; x++) {
                stringBuilder.append(filebits[x]).append("_");
            }
            bits.add(stringBuilder.toString());   // The folder

            stringBuilder = new StringBuilder();
            for (int x=namepos; x<filebits.length; x++) {
                stringBuilder.append(filebits[x]).append("_");
            }
            bits.add(stringBuilder.toString());   // The file

            // Get rid of underscores
            bits.set(0,bits.get(0).substring(0,bits.get(0).lastIndexOf("_")));
            bits.set(1,bits.get(1).substring(0,bits.get(1).lastIndexOf("_")));
        } else {
            bits.add(0,"");
            bits.add(1,filename);
        }
        return bits;

    }
    public Bitmap getPDFHighlighterBitmap(Context c, MainActivityInterface mainActivityInterface,
                                          Song song, int w, int h, int pageNum) {
        // The pdf highlighter song file is encoded as FOLDER_FILENAME_PAGENUM.png
        String filename = song.getFolder().replace("/", "_") + "_" +
                song.getFilename() + "_" + pageNum;
        return getHighlighterBitmap(c,mainActivityInterface,filename,w,h);
    }


    public Bitmap getHighlighterFile(Context c, MainActivityInterface mainActivityInterface, int w, int h) {
        String filename;
        int orientation = c.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            filename = getHighlighterFilename(mainActivityInterface.getSong(), true);
        } else {
            filename = getHighlighterFilename(mainActivityInterface.getSong(), false);
        }
        return getHighlighterBitmap(c,mainActivityInterface,filename,w,h);
    }

    private Bitmap getHighlighterBitmap(Context c, MainActivityInterface mainActivityInterface, String filename, int w, int h) {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Highlighter", "", filename);
        if (mainActivityInterface.getStorageAccess().uriExists(c, uri)) {
            return getBitmapFromUri(c,mainActivityInterface,uri,w,h);
        } else {
            return null;
        }
    }

    public Bitmap getSongBitmap(Context c, MainActivityInterface mainActivityInterface, String folder, String filename) {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Songs", folder, filename);
        if (mainActivityInterface.getStorageAccess().uriExists(c, uri)) {
            return getBitmapFromUri(c,mainActivityInterface,uri,-1,-1);
        } else {
            return null;
        }
    }

    public Bitmap getBitmapFromUri(Context c, MainActivityInterface mainActivityInterface, Uri uri, int w, int h) {
        // Load in the bitmap
        try {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (w>0 && h>0) {
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
        editText.setAutoSizeTextTypeUniformWithConfiguration(8,18,1);
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
        for (String line:lines) {
            if (numLines>maxLines) {
                stringBuilder.append("\n---\n");
                numLines = 1;
            }
            numLines ++;
            currentLine = new StringBuilder();
            String[] words = line.split(" ");
            for (String word:words) {
                if ((currentLine.length() + word.length() + 1) > maxChars) {
                    // Start a new line
                    if (numLines>maxLines) {
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
        stretchEditBoxToLines(editText,4);
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
        return themeString;
    }

    public ArrayList<String> getSectionHeadings(String lyrics) {
        // Get any named section headings i.e. [...]
        // These are used to create buttons in the edit song tags section
        // All sections are obtained regardless if the user doesn't want them in presentationOrder
        String nums = "0123456789";
        String[] bits = lyrics.split("\\[");
        ArrayList<String> sections = new ArrayList<>();
        for (String bit:bits) {
            if (bit.contains("]") && bit.indexOf("]")<20) {
                String section = bit.substring(0,bit.indexOf("]"));
                boolean multiverse = false;
                // Check for multiverse/chorus
                String[] lines = bit.split("\n");
                for (String line:lines) {
                    if (line.length()>2 && line.charAt(1) == '.' &&
                    nums.contains(line.substring(0,1)) &&
                    !sections.contains(section+line.charAt(0))) {
                        sections.add(section+line.charAt(0));
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
}