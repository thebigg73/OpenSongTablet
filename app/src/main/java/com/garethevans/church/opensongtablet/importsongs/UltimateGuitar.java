package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class UltimateGuitar {

    // Song is effectively written in <pre> formatting with chords above lyrics.
    // Chord lines will have the chord identifier in them.  That can be removed
    // Text is htmlentitied - i.e. " is shown as &quot;, ' is shown as &#039;

    private final String[] bitsToClear = new String[] {"</span>"};

    // New lines are identified as new lines

    public Song processContent(Context c, MainActivityInterface mainActivityInterface,
                               Song newSong, String s) {
        // First up separate the content from the headers
        String headerTitle = getHeaderTitle(s);

        // Get the title and author from this
        newSong.setFiletype("XML");
        newSong.setTitle(getTitle(mainActivityInterface,headerTitle));
        newSong.setFilename(newSong.getTitle());
        newSong.setAuthor(getAuthor(mainActivityInterface,headerTitle));

        // Get the key which might be in a div
        newSong.setKey(getKey(s));
        newSong.setCapo(getCapo(s));

        /*
        String[] ls = s.split("\n");
        for (String l:ls) {
            Log.d(TAG,l);
        }
        */

        // Trim out everything around the lyrics/content
        String contentStart = "<div class=\"ugm-b-tab--content js-tab-content\">";
        int start = s.indexOf(contentStart);
        String contentEnd = "</pre>";
        int end = s.indexOf(contentEnd,start);
        if (start>=0 && end>start) {
            s = s.substring(start+ contentStart.length(),end);
        }
        // Trim the lyrics start right up to the <pre tag
        start = s.indexOf("<pre");
        end = s.indexOf(">",start);
        if (start>0 && end>start) {
            s = s.substring(end+1);
        }

        // Split the content into lines
        String[] lines = s.split("\n");
        StringBuilder lyrics = new StringBuilder();
        for (String line:lines) {
            String chordIdentfier = "<span class=\"text-chord js-tab-ch js-tapped\">";
            if (line.contains(chordIdentfier)) {
                // Make it a chord line
                line = "." + line;
                line = line.replaceAll(chordIdentfier, "");
                line = line.trim();
            } else if (mainActivityInterface.getProcessSong().looksLikeGuitarTab(line)) {
                // Looks like a tab line
                line = mainActivityInterface.getProcessSong().fixGuitarTabLine(line);
                line = line.trim();
            } else if (mainActivityInterface.getProcessSong().looksLikeHeadingLine(line)) {
                // Looks like it is a heading line
                line = mainActivityInterface.getProcessSong().fixHeadingLine(line);
                line = line.trim();
            } else {
                // Assume it is a lyric line
                line = " " + line;
            }
            line = stripOutTags(line);
            line = fixHTMLStuff(mainActivityInterface,line);
            lyrics.append(line).append("\n");
        }
        newSong.setLyrics(lyrics.toString());

        // If we hae a capo (which means the key and the chords won't match in UG)
        // We will need to transpose the lyrics to match
        newSong = fixChordsForCapo(c,mainActivityInterface,newSong);
        return newSong;
    }

    private String getHeaderTitle(String s) {
        // We use this for the title and author
        int start = s.indexOf("<title>");
        int end = s.indexOf("</title>",start);
        if (start>=0 && end>start) {
            s = s.substring(start+7,end);
            return clearOutRubbish(s);
        } else {
            return "";
        }
    }
    private String getTitle(MainActivityInterface mainActivityInterface, String s) {
        // Likely to be sent something like <title>{TitleInCaps} CHORDS by {Author} @ Ultimate-Guitar.Com</title>
        // Give options in decreasing order of goodness
        int end = s.indexOf("CHORDS");
        if (end<0) {
            end = s.indexOf("by");
        }
        if (end<0) {
            end = s.length();
        }
        s = s.substring(0,end);
        // Try to sort the default capitalisation
        s = s.toLowerCase(mainActivityInterface.getLocale());
        // Capitalise the first letter
        if (s.length()>1) {
            s = s.substring(0,1).toUpperCase(mainActivityInterface.getLocale()) + s.substring(1);
        }
        s = s.trim();
        s = fixHTMLStuff(mainActivityInterface,s);
        return s;
    }
    private String getAuthor(MainActivityInterface mainActivityInterface, String s) {
        // Likely to be sent something like <title>{TitleInCaps} CHORDS by {Author} @ Ultimate-Guitar.Com</title>
        // Give options in decreasing order of goodness
        int end = s.indexOf("by ");
        if (end>0) {
            s = s.substring(end+3);
        } else {
            s = "";
        }
        s = s.trim();
        s = fixHTMLStuff(mainActivityInterface,s);
        return s;
    }
    private String getKey(String s) {
        return getMetaData(s, "<div class=\"label\">Key</div>");
    }
    private String getCapo(String s) {
        return getMetaData(s,"<div class=\"label\">Capo</div>");
    }
    private Song fixChordsForCapo(Context c, MainActivityInterface mainActivityInterface, Song newSong) {
        // If there is a capo, we have to transpose the song to match
        // UG shows the capo chords and the key but they don't match!
        // We want the actual chords to be stored in the file
        // The app takes care of showing capo/native chords
        String capo = newSong.getCapo();
        if (!capo.isEmpty()) {
            // Try to get a number
            capo = capo.replaceAll("[^\\d]", "");
            if (!capo.isEmpty()) {
                String key = newSong.getKey();
                String lyrics = newSong.getLyrics();
                try {
                    int transpnum = Integer.parseInt(capo);
                    newSong.setCapo(""+transpnum);
                    // So far so good.  Transpose the song
                    // Keep the original key ref

                    newSong = mainActivityInterface.getTranspose().
                            doTranspose(c,mainActivityInterface,newSong,
                                    "+1",transpnum,true);
                    // Put the original key back as we only want transposed lyrics
                    newSong.setKey(key);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Repair anything that was broken
                    newSong.setKey(key);
                    newSong.setCapo("");
                    newSong.setLyrics(lyrics);
                }
            }
        }
        return newSong;
    }

    private String clearOutRubbish(String s) {
        s = s.replace("@ Ultimate-Guitar.Com","");
        s = s.trim();
        return s;
    }

    private String stripOutTags(String s) {
        for (String bit:bitsToClear) {
            s = s.replace(bit,"");
        }
        return s;
    }

    private String getMetaData(String s, String identifer) {
        if (s.contains(identifer) && s.contains("<div class=\"tag\">") && s.contains("</div>")) {
            int pos1 = s.indexOf(identifer);
            int pos2 = s.indexOf("<div class=\"tag\">",pos1);
            pos2 = s.indexOf(">",pos2)+1;
            int pos3 = s.indexOf("</div>",pos2+1);
            if (pos2 > 0 && pos3 > pos2) {
                return s.substring(pos2, pos3).trim();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
    private String fixHTMLStuff(MainActivityInterface mainActivityInterface, String s) {
        // Fix html entities to more user friendly
        s = mainActivityInterface.getProcessSong().parseHTML(s);
        // Make it xml friendly though (no <,> or &)
        s = mainActivityInterface.getProcessSong().makeXMLSafeEncoding(s);
        return s;
    }

}
