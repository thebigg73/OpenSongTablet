package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class UltimateGuitar {

    private final MainActivityInterface mainActivityInterface;

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "UltimateGuitar";

    // Song is effectively written in <pre> formatting with chords above lyrics.
    // Chord lines will have the chord identifier in them.  That can be removed
    // Text is htmlentitied - i.e. " is shown as &quot;, ' is shown as &#039;

    public UltimateGuitar(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }

    private final String[] bitsToClear = new String[] {"</span>", "(Chords)"};

    // New lines are identified as new lines

    public Song processContent(Song newSong, String s) {
        // First up separate the content from the headers
        String headerTitle = getHeaderTitle(s);

        // Get the title and author from this
        newSong.setFiletype("XML");
        newSong.setTitle(getTitle(headerTitle));
        newSong.setFilename(newSong.getTitle());
        newSong.setAuthor(getAuthor(headerTitle));

        // Get the key which might be in a div
        newSong.setKey(getKey(s));
        newSong.setCapo(getCapo(s));

        Log.d(TAG,"title: "+newSong.getTitle());
        Log.d(TAG,"author: "+newSong.getAuthor());
        Log.d(TAG,"filname: "+newSong.getFilename());
        Log.d(TAG,"key: "+newSong.getKey());
        Log.d(TAG,"capo: "+newSong.getCapo());

        // Trim out everything around the lyrics/content
        String lyricsText = mainActivityInterface.getProcessSong().getSubstring(
                "<div class=\"ugm-b-tab--content js-tab-content\">","<pre","</pre>",s);

        // Alternative (newer method)
        if (lyricsText.isEmpty()) {
            lyricsText = mainActivityInterface.getProcessSong().getSubstring(
                    "<div class=\"js-page js-global-wrapper","<span class=\"y68er\">","<div class=\"LJhrL\">",s);
        }

        StringBuilder trimmedLyrics = new StringBuilder();
        for (String lyr:lyricsText.split("\n")) {
            if (!lyr.trim().isEmpty()) {
                trimmedLyrics.append(lyr).append("\n");
            }
        }

        // Split the content into lines
        String[] lines = trimmedLyrics.toString().split("\n");
        StringBuilder lyrics = new StringBuilder();
        for (String line:lines) {
            String chordIdentifier1 = "<span class=\"text-chord js-tab-ch js-tapped\">";
            String chordIdentifier2 = "<span class=\"_2jIGi\">";
            String chordIdentifier3 = "<span class=\"fciXY";
            String chordIdentifier4 = "data-name=\"";
            if (line.contains(chordIdentifier1) || line.contains(chordIdentifier2) ||
            line.contains(chordIdentifier3) || line.contains(chordIdentifier4)) {
                // Make it a chord line
                line = "." + line;
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
            line = mainActivityInterface.getProcessSong().removeHTMLTags(line);
            Log.d(TAG,"processed line:"+line);
            //line = stripOutTags(line);
            line = fixHTMLStuff(line);
            lyrics.append(line).append("\n");
        }

        // Get rid of extra [[ and ]]
        String finalLyrics = lyrics.toString().replace("[[","[");
        finalLyrics = finalLyrics.replace("]]","]");

        newSong.setLyrics(finalLyrics);
        for (String lyr:finalLyrics.split("\n")) {
            Log.d(TAG,"lyr: "+lyr);
        }

        // If we have a capo (which means the key and the chords won't match in UG)
        // We will need to transpose the lyrics to match
        newSong = fixChordsForCapo(newSong);
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
    private String getTitle(String s) {
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
        s = fixHTMLStuff(s);
        return s;
    }
    private String getAuthor(String s) {
        // Likely to be sent something like <title>{TitleInCaps} CHORDS by {Author} @ Ultimate-Guitar.Com</title>
        // Give options in decreasing order of goodness
        int end = s.indexOf("by ");
        if (end>0) {
            s = s.substring(end+3);
        } else {
            s = "";
        }
        s = s.trim();
        s = fixHTMLStuff(s);
        return s;
    }
    private String getKey(String s) {
        String key = getMetaData(s, "<div class=\"label\">Key</div>");
        // Try new method looking for line: "musicalKey": "XX"
        String bit = "\"musicalKey\":";
        if (key.isEmpty() && s.contains(bit)) {
            int startpos = s.indexOf(bit);
            int endpos = s.indexOf("\n",startpos);
            if (endpos>startpos && endpos-startpos<8) {
                key = s.substring(startpos,endpos);
                key = key.replace(bit,"").replace("\"","").trim();
            }
        }
        // Try final methods
        String bit2 = "Key:";
        if (key.isEmpty() && s.contains(bit2)) {
            int startpos = s.indexOf(bit2);
            int endpos = s.indexOf("\n",startpos);
            if (endpos>startpos && endpos-startpos<5) {
                key = s.substring(startpos,endpos);
                key = key.replace(bit2,"").replace(":","").trim();
            }
        }
        String bit3 = "Key :";
        if (key.isEmpty() && s.contains(bit3)) {
            int startpos = s.indexOf(bit3);
            int endpos = s.indexOf("\n",startpos);
            if (endpos>startpos && endpos-startpos<5) {
                key = s.substring(startpos,endpos);
                key = key.replace(bit3,"").replace(":","").trim();
            }
        }
        return key;
    }
    private String getCapo(String s) {
        return getMetaData(s,"<div class=\"label\">Capo</div>");
    }
    private Song fixChordsForCapo(Song newSong) {
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
                            doTranspose(newSong, "+1",transpnum,
                                    newSong.getDetectedChordFormat(),
                                    newSong.getDesiredChordFormat());
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
        s = s.replaceAll("<(.*?)>", "");
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
    private String fixHTMLStuff(String s) {
        // Fix html entities to more user friendly
        s = mainActivityInterface.getProcessSong().parseHTML(s);
        // Make it xml friendly though (no <,> or &)
        s = mainActivityInterface.getProcessSong().parseToHTMLEntities(s);
        return s;
    }

}
