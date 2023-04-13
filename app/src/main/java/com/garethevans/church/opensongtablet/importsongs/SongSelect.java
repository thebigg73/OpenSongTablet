package com.garethevans.church.opensongtablet.importsongs;

import android.os.Build;
import android.text.Html;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class SongSelect {

    // User can either click on the download link (which triggers a pdf download),
    // Or click on the extract button.  To keep it legal, this triggers the download button too!
    // Song is effectively written in chordpro.
    // Chord lines will have the chord identifier in them.  That can be removed
    // Text is html entitied - i.e. " is shown as &quot;, ' is shown as &#039;

    public Song processContentChordPro(MainActivityInterface mainActivityInterface, Song newSong, String s) {
        // Get the content we want
        s = getSubstring(s,"<span class=\"cproSongHeader\">","<p class=\"disclaimer\">");

        newSong.setTitle(mainActivityInterface.getProcessSong().parseHTML(getTitle(s)));
        newSong.setFilename(newSong.getTitle());
        newSong.setFiletype("XML");
        newSong.setAuthor(mainActivityInterface.getProcessSong().parseHTML(getAuthor(s)));
        newSong.setCcli(mainActivityInterface.getProcessSong().parseHTML(getCCLI(s)));
        newSong.setCopyright(mainActivityInterface.getProcessSong().parseHTML(getCopyright(s)));
        getTempoTimeSig(newSong,s);
        newSong.setKey(getKey(s));

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

    private String getTitle(String s) {
        // IV - Try chordpro style
        int start = s.indexOf("<span class=\"" +
                "cproTitle\">");
        int end = s.indexOf("</span>",start);

        // IV - Try song viewer style
        if (start == -1) {
            start = s.indexOf("<h2 class=\"song-viewer-title\">");
            end = s.indexOf("</h2>",start);
        }

        // IV - Try page style
        if (start == -1) {
            start = s.indexOf("<div class=\"content-title\">");
            if (start > -1) {
                start = s.indexOf("<h1>", start);
                end = s.indexOf("</h1>", start);
            }
        }

        if (start>-1 && end>-1 && end>start) {
            start = s.indexOf(">",start) + 1;
            String t = s.substring(start,end);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                t = Html.fromHtml(t, 0).toString();
            } else {
                t = Html.fromHtml(t).toString();
            }
            return t.trim();
        } else {
            return "Not available";
        }
    }

    private String getAuthor(String s) {
        // Extract the author
        // IV - Try chordpro style
        int start = s.indexOf("<span class=\"cproAuthors\">");
        int end = s.indexOf("</span>",start);

        // IV - Try song viewer style
        if (start == -1) {
            start = s.indexOf("<p class=\"contributor\">");
            end = s.indexOf("</p>",start);
        }

        if (start>-1 && end>-1 && end>start) {
            start = s.indexOf(">",start) + 1;
            // IV - Remove line breaks, replace non breaking spaces, replace use of | as separator with comma
            String a = s.substring(start,end).
                    replaceAll("<br>",", ").
                    replace("</li><li>",", ").
                    replace("<li>","").
                    replace ("</li>","").
                    replaceAll("&nbsp;"," ").
                    replaceAll("\u00A0"," ").
                    replaceAll("\\Q |\\E",",");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                a = Html.fromHtml(a, 0).toString();
            } else {
                a = Html.fromHtml(a).toString();
            }
            return a.trim();
        } else {
            return "";
        }
    }

    private String getKey(String s) {
        String key = "";
        int start = s.indexOf("<code class=\"cproSongKey\"");
        if (start>0) {
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
                replace("&nbsp;"," "));
    }

    private String getCopyright(String s) {
        // IV - Same class for chordpro and song viewer styles
        if (s.contains("<ul class=\"copyright\">")) {
            s = getSubstring(s, "<ul class=\"copyright\">", "</ul>").
            // IV - Remove line breaks, copyright, replace non breaking spaces, replace use of | as separator with comma and remove '(Admin. by)' content
                    replace("</li><li>",", ").
                    replace("<li>","").
                    replace ("</li>","").
                    replace("©","").
                    replaceAll("&nbsp;"," ").
                    replaceAll("\u00A0"," ").
                    replaceAll("\\Q |\\E",",").
                    replaceAll(" \\(Admin\\..*?\\)","");
            return s.trim();
        } else {
            return "";
        }
    }

    private String getCCLI(String s) {
        // IV - Same class for chordpro and song viewer styles
        // IV - Step over leading words
        String ccli;
        int start;
        ccli = getSubstring(s,"<p class=\"songnumber\">CCLI Song #","</p>").trim();
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

        if (startText!=null && endText!=null && pos1>0 && pos2>pos1) {
            return s.substring(pos1,pos2);
        } else if (startText==null & endText!=null && pos2>0) {
            return s.substring(0,pos2);
        } else if (startText!=null && endText==null & pos1>0) {
            return s.substring(pos1);
        } else {
            return "";
        }
    }

    private String stripOutTags(String s) {
        s = s.replaceAll("\\<.*?\\>", "");
        return s;
    }

    public Song processContentLyricsText(MainActivityInterface mainActivityInterface, Song newSong, String s) {
        int start;
        int end;

        start = s.indexOf("<div class=\"song-viewer lyrics\" id=\"song-viewer\">");
        end = s.indexOf("</div>", start);
        if (start > -1 && end > -1 && end > start) {
            start = s.indexOf(">", start) + 1;
            String lyrics = s.substring(start, end).trim();
            // IV - Drop the bottom copyright info
            end = lyrics.indexOf("<div class=\"copyright-info\">");
            if (end > -1) {
                lyrics = lyrics.substring(0, end);
            }
            // IV - Mark a song viewer part as a tag
            lyrics = lyrics.replaceAll("<h3 class=\"song-viewer-part\">", "<h3 class=\"song-viewer-part\">#[").
                    replaceAll("</h3>", "]</h3>");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                lyrics = Html.fromHtml(lyrics, 0).toString();
            } else {
                lyrics = Html.fromHtml(lyrics).toString();
            }
            // IV - Remove extra line after tag
            lyrics = lyrics.replaceAll("]\n\n", "]\n");

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
}
