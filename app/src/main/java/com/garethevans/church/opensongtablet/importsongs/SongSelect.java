package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.os.Build;
import android.text.Html;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.Arrays;
import java.util.List;

public class SongSelect {

    // User can either click on the download link (which triggers a pdf download),
    // Or click on the extract button.  To keep it legal, this triggers the download button too!
    // Song is effectively written in chordpro.
    // Chord lines will have the chord identifier in them.  That can be removed
    // Text is html entitied - i.e. " is shown as &quot;, ' is shown as &#039;

    public Song processContentChordPro(Context c, MainActivityInterface mainActivityInterface, Song newSong, String s) {    // Get the content we want
        s = getSubstring(s,"<span class=\"cproSongHeader\">","<p class=\"disclaimer\">");

        newSong.setTitle(mainActivityInterface.getProcessSong().parseHTML(getTitle(s)));
        newSong.setFilename(newSong.getTitle());
        newSong.setFiletype("XML");
        newSong.setAuthor(mainActivityInterface.getProcessSong().parseHTML(getAuthor(s)));
        newSong.setCcli(mainActivityInterface.getProcessSong().parseHTML(getCCLI(s)));
        newSong.setCopyright(mainActivityInterface.getProcessSong().parseHTML(getCopyright(s)));
        getTempoTimeSig(newSong,s);
        // Extract the key
        String key = getKey(s);
        // Key may be from import of a Solfege SongSelect song, transpose any Solfege key just in case
        final String[] fromSongSelectSolfege =  "LA SI DO RE MI FA SOL".split(" ");
        final String[] toStandard =  "A B C D E F G".split(" ");
        for (int z = 0; z < fromSongSelectSolfege.length; z++) key = key.replace(fromSongSelectSolfege[z], toStandard[z]);
        // Check the key is a Standard key - if not set no key
        int index = -1;
        List<String> key_choice = Arrays.asList(c.getResources().getStringArray(R.array.key_choice));
        for (int w = 0; w < key_choice.size();w++) {
            if (key.equals(key_choice.get(w))) {
                index = w;
            }
        }
        if (index == -1) {
            key = "";
        }
        newSong.setKey(key);

        // IV - Handle lyrics with with one or more cproSongBody where split over pages
        StringBuilder lyricsBuilder = new StringBuilder();
        while (s.contains("<pre class=\"cproSongBody\">")) {
            int start = s.indexOf("<pre class=\"cproSongBody\">");
            int end = s.indexOf("</pre>", start);

            // IV - Overwrite so that next loop finds any further SongBody
            s = s.replaceFirst("<pre class=\"cproSongBody\">", "<xxxxxxxxxxxxxxxxxxxxxxxx>");

            if (start > -1 && end > -1 && end > start) {
                lyricsBuilder.append(s.substring(start + 26, end)).append("\n");
            }
        }
        String lyrics = lyricsBuilder.toString();

        // Fix the content
        lyrics = fixHeaders(lyrics);
        lyrics = fixChords(lyrics);
        lyrics = mainActivityInterface.getProcessSong().parseHTML(lyrics);
        // Get rid of the rest of the stuff
        lyrics = stripOutTags(lyrics);
        // End trim
        lyrics = lyrics.replaceAll("\\s+$", "");

        // Convert to OpenSongFormat
        lyrics = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(lyrics);
        newSong.setLyrics(lyrics);

        return newSong;
    }

    public String getTitle(String s) {
        // IV - Try chordpro style
        int start = s.indexOf("<span class=\"cproTitle\">");
        start = start > -1 ? start + 24 : -1;
        int end = s.indexOf("</span>", start);

        // IV - Try song viewer style
        if (start == -1) {
            start = s.indexOf("class=\"song-header\">");
            start = start > -1 ? start + 20 : -1;
            end = s.indexOf("</h2>", start);
        }

        // IV - Try page style
        if (start == -1) {
            start = s.indexOf("data-title=\"");
            start = start > -1 ? start + 12 : -1;
            end = s.indexOf("\"", start);
        }

        if (start > -1 && end > -1 && end > start) {
            return parseFromHTML(s.substring(start, end));
        } else {
            return "Not available";
        }
    }
    
    private String getAuthor(String s) {
        // Extract the author
        String author = "";

        // IV - Try chordpro style
        int start = s.indexOf("<span class=\"cproAuthors\">");
        int end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            start = s.indexOf(">", start) + 1;
            author = s.substring(start, end);
        }

        // IV - Try song viewer style
        if (start == -1) {
            if (s.contains("class=\"song-footer\">")) {
                // IV - Footer has div for logo, author, copyright - code steps to relevant div
                author = getSubstring(s, "class=\"song-footer\">", "href=") + "¬";
                author = getSubstring(author, "</div><div", "¬") + "¬";
                author = getSubstring(author, "</div><div", "</div>");
                author = stripOutTags("<div" + author);
            } else {
                return "";
            }
        }

        // IV - Remove line breaks, replace non breaking spaces, replace use of | as separator with comma
        author = author.replaceAll("<br>",", ").
                replace("</li><li>",", ").
                replace("<li>","").
                replace ("</li>","").
                replaceAll("&nbsp;"," ").
                replaceAll("\u00A0"," ").
                replaceAll("\\Q |\\E",",");

        return parseFromHTML(author);
    }

    private String getKey(String s) {
        String key = "";
        int start = s.indexOf("<code class=\"cproSongKey\"");
        if (start > 0) {
            int pos1 = s.indexOf(">",start);
            int pos2 = s.indexOf("</code>", pos1);
            if (pos1 > 0 && pos2 > pos1) {
                key = s.substring(pos1 + 1, pos2);
            }
        }
        return key;
    }

    private void getTempoTimeSig(Song newSong, String s) {
        s = getSubstring(s,"<span class=\"cproTempoTimeWrapper\">","</span>");
        newSong.setTempo(getSubstring(s,"Tempo -","|").trim().
                replace("bpm", "").
                replace("BPM", "").
                replace("Bpm", ""));
        newSong.setTimesig(getSubstring(s,"Time -",null).trim().
                replace("&nbsp;",""));
    }

    private String getCopyright(String s) {
        // IV - Try chordpro style
        if (s.contains("<ul class=\"copyright\">")) {
            s = getSubstring(s, "<ul class=\"copyright\">", "</ul>");
        // IV - Try song viewer style
        } else if (s.contains("class=\"song-footer\">")) {
            // IV - Footer has div for logo, author, copyright - code steps to relevant div
            s = getSubstring(s, "class=\"song-footer\">", "href=") + "¬";
            s = getSubstring(s, "</div><div", "¬") + "¬";
            s = getSubstring(s, "</div><div", "¬") + "¬";
            s = getSubstring(s, "</div><div", "</div>");
            s = stripOutTags("<div"+ s);
        } else {
            return "";
        }

        // IV - Remove line breaks, copyright, replace non breaking spaces, replace use of | as separator with comma and remove '(Admin. by)' content
        s = s.replace("</li><li>",", ").
                replace("<li>","").
                replace ("</li>","").
                replace("©","").
                replaceAll("&nbsp;"," ").
                replaceAll("\u00A0"," ").
                replaceAll("\\Q |\\E",",").
                replaceAll(" \\(Admin\\..*?\\)","");
        return s.trim();
    }

    private String getCCLI(String s) {
        String ccli;
        // IV - Try chordpro style
        if (s.contains("<p class=\"songnumber\">")) {
            ccli = stripOutTags(getSubstring(s, "<p class=\"songnumber\">", "</p>"));
        // IV - Try song viewer style
        } else if (s.contains("class=\"song-footer\">")) {
            // IV - Footer has div for logo, author, copyright - code steps to relevant div
            ccli = getSubstring(s, "class=\"song-footer\">", "href=") + "¬";
            ccli = getSubstring(ccli, "</div><div", "</div>");
            ccli = stripOutTags("<" + ccli);
        } else {
            return "";
        }
        int start;
        // IV - Tries to handle local variants, assumes the song number is at the end so removes leading words
        while (ccli.contains(" ")) {
            start = ccli.indexOf(" ");
            ccli = ccli.substring(start + 1);
        }
        return ccli.trim();
    }

    private String fixHeaders(String lyrics) {
        int start;
        while (lyrics.contains("<span class=\"cproSongSection\"><span class=\"cproComment\">")) {
            start = lyrics.indexOf("<span class=\"cproSongSection\"><span class=\"cproComment\">");
            lyrics = lyrics.substring(0, start) +
                    (lyrics.substring(start).
                            replaceFirst("<span class=\"cproSongSection\"><span class=\"cproComment\">","#[").
                            replaceFirst("</span>", "]"));
        }
        return lyrics;
    }

    private String fixChords(String lyrics) {
        // Fix the chords
        // Chords are found in a bit like this: <span class="chordWrapper"><code class="chord" data-chordname="D<sup>2</sup>">D<sup>2</sup></code>
        // We replace start with [< and end with ] to give: [<"D<sup>2</sup>">D<sup>2</sup>]
        // The following stripOutTags clean up removes all "<...>" to give [D2]

        int start;

        while (lyrics.contains("<span class=\"chordWrapper\"><code class=\"chord\" data-chordname=\"")) {
            start = lyrics.indexOf("<span class=\"chordWrapper\"><code class=\"chord\" data-chordname=\"");
            lyrics = lyrics.substring(0, start) +
                    (lyrics.substring(start).
                            replaceFirst("<span class=\"chordWrapper\"><code class=\"chord\" data-chordname=\"","[<").
                            replaceFirst("</code>","]"));
        }
        return lyrics;
    }


    private String getSubstring(String s, String startText, String endText) {
        int pos1=0, pos2=0;
        if (startText!=null) {
            pos1 = s.indexOf(startText) + startText.length();
        }
        if (endText!=null) {
            pos2 = s.indexOf(endText,pos1);
        }

        try {
            if (startText != null && endText != null && pos1 > 0 && pos2 > pos1) {
                return s.substring(pos1, pos2);
            } else if (startText == null & endText != null && pos2 > 0) {
                return s.substring(0, pos2);
            } else if (startText != null && endText == null & pos1 > 0) {
                return s.substring(pos1);
            } else {
                return "";
            }
        } catch (Exception e) {
            // Just in case there is an issue with a null substring, or the start/end exceed the length
            // This has been seen on PlayStore logs -  I think changing the page rapidly while processing may cause this
            return "";
        }
    }

    private String stripOutTags(String s) {
        // As before, keep extra backslashes!
        s = s.replaceAll("\\<.*?\\>", "");
        return s;
    }

    public Song processContentLyricsText(MainActivityInterface mainActivityInterface, Song newSong, String s) {
        int start = s.indexOf("sheet-container\">");
        int end = s.indexOf("</main><footer", start);
        if (start > -1 && end > -1 && end > start) {
            start = s.indexOf(">", start) + 1;
            String lyrics = s.substring(start, end).trim();
 
            // IV - Mark each Song viewer part as a section
            lyrics = lyrics.replaceAll("(?<=<h3).*?(?=>)", "") // Remove text between <h3 and >
                    .replaceAll("<h3>", "<h3>#[")
                    .replaceAll("</h3>", "]</h3>");
            lyrics = parseFromHTML(lyrics);
 
            // IV - Handle blank lines
            lyrics = lyrics.replaceAll("\n\n", "\n")
                    .replace("\n#[", "\n\n#[");
 
            // The first line is the title normally
            end = lyrics.indexOf("\n");
            if (end > -1) {
                newSong.setTitle(lyrics.substring(0, end).trim());
                lyrics = lyrics.substring(end).trim();
            }
            // Convert to OpenSongFormat
            newSong.setLyrics(mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(lyrics));
        } else {
            newSong.setTitle(mainActivityInterface.getProcessSong().parseHTML(getTitle(s)));
        }
        newSong.setFilename(newSong.getTitle());
        newSong.setFiletype("XML");
        newSong.setAuthor(mainActivityInterface.getProcessSong().parseHTML(getAuthor(s)));
        newSong.setCcli(mainActivityInterface.getProcessSong().parseHTML(getCCLI(s)));
        newSong.setCopyright(mainActivityInterface.getProcessSong().parseHTML(getCopyright(s)));

        return newSong;
    }

    private String parseFromHTML(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, 0).toString().trim();
        } else {
            return Html.fromHtml(html).toString().trim();
        }
    }
}