package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.util.Base64;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.Preferences;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.StaticVariables;
import com.garethevans.church.opensongtablet.StorageAccess;

import java.util.ArrayList;

public class ProcessSong {
    
    String parseHTML(String s) {
        if (s == null) {
            return "";
        }
        s = s.replace("&amp;apos;", "'");
        s = s.replace("&amp;quote;", "\"");
        s = s.replace("&amp;quot;", "\"");
        s = s.replace("&amp;lt;", "<");
        s = s.replace("&amp;gt;", ">");
        s = s.replace("&amp;", "&");
        s = s.replace("&lt;", "<");
        s = s.replace("&gt;", ">");
        s = s.replace("&apos;", "'");
        s = s.replace("&quote;", "\"");
        s = s.replace("&quot;", "\"");
        return s;
    }
    String parseToHTMLEntities(String s) {
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

    String fixStartOfLines(String lyrics) {
        StringBuilder fixedlyrics = new StringBuilder();
        String[] lines = lyrics.split("\n");

        for (String line:lines) {
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

    String fixLineBreaksAndSlashes(String s) {
        s = s.replace("\r\n", "\n");
        s = s.replace("\r", "\n");
        s = s.replace("\n\n\n", "\n\n");
        s = s.replace("&quot;", "\"");
        s = s.replace("\\'", "'");
        s = s.replace("&quot;", "\"");
        s = s.replace("<", "(");
        s = s.replace(">", ")");
        s = s.replace("&#39;", "'");
        s = s.replace("\t", "    ");
        s = s.replace("\\'", "'");

        return s;
    }

    String determineLineTypes(String string, Context c) {
        String type;
        if (string.indexOf(".")==0) {
            type = "chord";
        } else if (string.indexOf(";__" + c.getResources().getString(R.string.edit_song_capo))==0) {
            type = "capoinfo";
        } else if (string.indexOf(";__")==0) {
            type = "extra";
            //} else if (string.startsWith(";"+c.getString(R.string.music_score))) {
            //    type = "abcnotation";
        } else if (string.startsWith(";") && string.length()>4 && (string.indexOf("|")==2 || string.indexOf("|")==3)) {
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
    String fixLineLength(String string, int newlength) {
        int extraspacesrequired = newlength - string.length();
        StringBuilder stringBuilder = new StringBuilder(string);
        for (int x = 0; x<extraspacesrequired; x++) {
            stringBuilder.append(" ");
        }
        string = stringBuilder.toString();
        return string;
    }
    String[] getChordPositions(String string) {
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

            String prevchar;
            boolean prevcharempty = false;
            prevchar = string.substring(x-1,x);
            if (prevchar.equals(" ") || prevchar.equals("|")) {
                prevcharempty = true;
            }

            if (!thischarempty && prevcharempty) {
                // This is a chord position
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
    String[] getLyricSections(String string, String[] pos_string) {
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

    String parseLyrics(String myLyrics, Context c) {
        myLyrics = myLyrics.replace("]\n\n","]\n");
        myLyrics = myLyrics.replaceAll("\r\n", "\n");
        myLyrics = myLyrics.replaceAll("\r", "\n");
        myLyrics = myLyrics.replaceAll("\\t", "    ");
        myLyrics = myLyrics.replaceAll("\f", "    ");
        myLyrics = myLyrics.replace("\r", "");
        myLyrics = myLyrics.replace("\t", "    ");
        myLyrics = myLyrics.replace("\b", "    ");
        myLyrics = myLyrics.replace("\f", "    ");
        myLyrics = myLyrics.replace("&#27;", "'");
        myLyrics = myLyrics.replace("&#027;", "'");
        myLyrics = myLyrics.replace("&#39;","'");
        myLyrics = myLyrics.replace("&#34;","'");
        myLyrics = myLyrics.replace("&#039;","'");
        myLyrics = myLyrics.replace("&ndash;","-");
        myLyrics = myLyrics.replace("&mdash;","-");
        myLyrics = myLyrics.replace("&apos;","'");
        myLyrics = myLyrics.replace("&lt;", "<");
        myLyrics = myLyrics.replace("&gt;", ">");
        myLyrics = myLyrics.replace("&quot;", "\"");
        myLyrics = myLyrics.replace("&rdquo;","'");
        myLyrics = myLyrics.replace("&rdquor;","'");
        myLyrics = myLyrics.replace("&rsquo;","'");
        myLyrics = myLyrics.replace("&rdquor;","'");
        myLyrics = myLyrics.replaceAll("\u0092", "'");
        myLyrics = myLyrics.replaceAll("\u0093", "'");
        myLyrics = myLyrics.replaceAll("\u2018", "'");
        myLyrics = myLyrics.replaceAll("\u2019", "'");

        // If UG has been bad, replace these bits:
        myLyrics = myLyrics.replace("pre class=\"\"","");

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
        myLyrics = myLyrics.replace("["+languageverse_lowercase,"["+languageverse);
        myLyrics = myLyrics.replace("["+languageverse_uppercase,"["+languageverse);
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
        String languagechorus_lowercase = languagechorus.toLowerCase(StaticVariables.locale);
        String languagechorus_uppercase = languagechorus.toUpperCase(StaticVariables.locale);
        myLyrics = myLyrics.replace("["+languagechorus_lowercase,"["+languagechorus);
        myLyrics = myLyrics.replace("["+languagechorus_uppercase,"["+languagechorus);
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





    public String songHTML (Context c, StorageAccess storageAccess, Preferences preferences,
                     int lyricsBackgroundColor, int lyricsTextColor, int lyricsChordColor) {
        String unencodedHtml =  "" +
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

        return Base64.encodeToString(unencodedHtml.getBytes(), Base64.NO_PADDING);
    }

    private String getHTMLFontImports(Context c, Preferences preferences,
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

    private String processHTMLLyrics(Context c, Preferences preferences,
                             int lyricsTextColor, int lyricsChordColor) {
        // This goes through the song a section at a time and prepares the table contents
        String[] lines = StaticVariables.mLyrics.split("\n");
        String previousline, previouslineType;
        String thisline, thislineType;
        String nextline, nextlineType;

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
                htmltext.append("<u>").append(fixHeading(c,thisline)).append("</u>").append("<br>");
            } else {
                // TODO
                htmltext.append(thisline).append("<br>\n");
            }
        }
        return htmltext.toString();
    }

    private String getLineType(String line) {
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
    }

    private String fixHeading(Context c, String line) {
        line = line.replace("[","");
        line = line.replace("]","");

        switch (line) {
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
                line = line.replace("V", c.getResources().getString(R.string.tag_verse) + " ");
                line = line.replace("-", "");
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
                line = line.replace("T", c.getResources().getString(R.string.tag_tag) + " ");
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
                line = line.replace("C", c.getResources().getString(R.string.tag_chorus) + " ");
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
                line = line.replace("B", c.getResources().getString(R.string.tag_bridge) + " ");
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
                line = line.replace("P", c.getResources().getString(R.string.tag_prechorus) + " ");
                break;
        }
        return line;
    }
}
