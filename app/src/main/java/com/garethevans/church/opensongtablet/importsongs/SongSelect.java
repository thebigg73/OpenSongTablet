package com.garethevans.church.opensongtablet.importsongs;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class SongSelect {

    // User can either click on the download link (which triggers a pdf download),
    // Or click on the extract button.  To keep it legal, this triggers the download button too!
    // Song is effectively written in chopro.
    // Chord lines will have the chord identifier in them.  That can be removed
    // Text is htmlentitied - i.e. " is shown as &quot;, ' is shown as &#039;

    private final String[] bitsToClear = new String[] {"</span>", "<span class=\"chordLyrics\">",
            "<span class=\"chordWrapper\">","</code>", "<span class=\"cproSongLine\">",
            "<pre class=\"cproSongBody\">", "<pre class=\"cproSongSection\">", "<pre class=\"cproComment\">"};

    public Song processContent(MainActivityInterface mainActivityInterface,
                               Song newSong, String s) {
        // Get the content we want
        s = getSubstring(s,"<span class=\"cproTitle\">","<p class=\"disclaimer\">");

        newSong.setTitle(mainActivityInterface.getProcessSong().parseHTML(getHeader(s)));
        newSong.setFilename(newSong.getTitle());
        newSong.setFiletype("XML");
        newSong.setAuthor(mainActivityInterface.getProcessSong().parseHTML(getAuthor(s)));
        newSong.setCcli(mainActivityInterface.getProcessSong().parseHTML(getCCLI(s)));
        newSong.setCopyright(mainActivityInterface.getProcessSong().parseHTML(getCopyright(s)));
        getTempoTimeSig(newSong,s);
        newSong.setKey(getKey(s));

        // Trim the song contents further
        s = getSubstring(s,"<pre class=\"cproSongBody\">","</pre>");

        // Now split into lines and fix the content
        StringBuilder stringBuilder = new StringBuilder();
        String[] lines = s.split("\n");
        for (String line:lines) {
            line = fixHeaders(line);
            line = fixChords(line);
            line = mainActivityInterface.getProcessSong().parseHTML(line);
            stringBuilder.append(line).append("\n");
        }

        // Get rid of the rest of the stuff
        String content = stripOutTags(stringBuilder.toString());

        // Convert to OpenSongFormat
        content = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(content);
        newSong.setLyrics(content);

        return newSong;
    }

    private String getHeader(String s) {
        return s.substring(0,s.indexOf("</span>")).trim();
    }

    private String getAuthor(String s) {
        return getSubstring(s,"<span class=\"cproAuthors\">","</span>").trim();
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
        newSong.setTempo(getSubstring(s,"Tempo -","|").trim());
        newSong.setTimesig(getSubstring(s,"Time -",null).trim());
    }

    private String getCopyright(String s) {
        if (s.contains("<ul class=\"copyright\">")) {
            s = getSubstring(s, "<ul class=\"copyright\">", "</ul>");
            s = s.replace("<li>", "");
            s = s.replace("</li>", " ");

            return s.trim();
        } else {
            return "";
        }
    }

    private String getCCLI(String s) {
        return getSubstring(s,"<p class=\"songnumber\">CCLI Song #","</p>").trim();
    }

    private String fixHeaders(String line) {
        if (line.contains("<span class=\"cproSongSection\">")) {
            line = getSubstring(line,"<span class=\"cproComment\">","</span>");
        }
        return line;
    }

    private String fixChords(String line) {
        if (line.contains("<span class=\"chordWrapper\">")) {
            line = line.replace("<span class=\"chordWrapper\">","[");
            while (line.contains("<code class=\"chord\"")) {
                int start = line.indexOf("<code class=\"chord\"");
                int end = line.indexOf(">",start);
                if (start>0 && end>start) {
                    String remove = line.substring(start, end+1);
                    line = line.replace(remove, "");
                }
            }
            line = line.replace("</code>","]");
        }
        return line;
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
        for (String bit:bitsToClear) {
            s = s.replace(bit,"");
        }
        s = s.replaceAll("<(.*?)>", "");
        return s;
    }

}
