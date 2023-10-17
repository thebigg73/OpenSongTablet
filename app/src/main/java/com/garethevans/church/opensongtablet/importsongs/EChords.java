package com.garethevans.church.opensongtablet.importsongs;

import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class EChords {

    private final String TAG = "EChords";
    private MainActivityInterface mainActivityInterface;

    public Song processContent(MainActivityInterface mainActivityInterface, Song song, String webString) {
        this.mainActivityInterface = mainActivityInterface;

        webString = webString.replace("</div><div","</div>\n<div");
        for (String line:webString.split("\n")) {
            Log.d(TAG,"line:"+line);
        }

        // Get the title
        song.setTitle(getTitle(webString));
        song.setFilename(song.getTitle());

        // Get the author
        song.setAuthor(getArtist(webString));

        // Get the key
        song.setKey(getKey(webString));
        song.setCapo(getCapo(webString));

        // Get the lyrics
        String lyrics = getSubstring(webString,"<pre id=\"core\"","</pre>",false);
        // Get the start
        if (lyrics.contains(">")) {
            lyrics = lyrics.substring(lyrics.indexOf(">") + 1);
        }

        // Parse them line by line
        String chordIndicator = "<u><span class=\"showchord\"";
        StringBuilder stringBuilder = new StringBuilder();
        String[] lines = lyrics.split("\n");
        for (String line:lines) {
            if (line.contains(chordIndicator)) {
                // This is a chord line
                if (line.trim().startsWith(chordIndicator)) {
                    // Just add the period to the start and strip out the tags
                    stringBuilder.append(".").append(fixHTMLStuff(stripOutTags(line))).append("\n");
                } else {
                    // This line has something else first, so add a new line then period
                    String bitBefore = line.substring(0,line.indexOf(chordIndicator)).trim();
                    line = line.replace(bitBefore,bitBefore+"\n.");
                    line = fixHTMLStuff(stripOutTags(line));
                    stringBuilder.append(stripOutTags(line)).append("\n");
                }
            } else {
                if (!line.startsWith(" ")) {
                    line = " " + line;
                }
                line = fixHTMLStuff(stripOutTags(line));
                stringBuilder.append(line).append("\n");
            }
        }

        song.setLyrics(mainActivityInterface.getConvertTextSong().convertText(stringBuilder.toString()));
        Log.d(TAG,"lyrics: "+song.getLyrics());
        Log.d(TAG,"title: "+song.getTitle());
        Log.d(TAG, "author: "+song.getAuthor());
        Log.d(TAG, "key:" + song.getKey());

        return song;
    }

    private String getTitle(String webString) {
        String title1 = getSubstring(webString,"var title = \"","\";",true);
        String title2 = getSubstring(webString,"<h1>", "</h1>",true);
        if (title1.isEmpty() && !title2.isEmpty()) {
            title2 = title2.replace("(Chords)","").trim();
            title1 = title2;
        }
        return title1;
    }

    private String getArtist(String webString) {
        String artist1 = getSubstring(webString,"var artist = \"","\";",true);
        String artist2 = getSubstring(webString,"<h2>", "</h2>",true);
        if (artist1.isEmpty() && !artist2.isEmpty()) {
            artist1 = artist2;
        }
        return artist1;
    }

    private String getKey(String webString) {
        String key1 = getSubstring(webString,"var strKey = \"", "\";", true);
        String key2 = getSubstring(webString,"<div>Key:","<span class=\"caret\">",true).trim();
        if (key1.isEmpty() && !key2.isEmpty()) {
            key1 = key2;
        }
        return key1;
    }

    private String getCapo(String webString) {
        String capo = getSubstring(webString, "var keycapo = ",";",true);
        if (capo!=null && capo.equals("0")) {
            capo = "";
        }
        return capo;
    }

    private String getSubstring(String from, String startText, String endText, boolean stripTags) {
        int start = from.indexOf(startText);
        int end = from.indexOf(endText,start);
        Log.d(TAG,"search ("+startText+","+endText+") start:"+start+"  end:"+end);
        if (start>-1 && end>start) {
            from = from.substring(start+startText.length(),end);
            Log.d(TAG,"substring: "+from);
            if (stripTags) {
                return stripOutTags(from);
            } else {
                return from;
            }
        } else {
            return "";
        }
    }

    private String stripOutTags(String s) {
        s = s.replaceAll("<(.*?)>", "");
        return s;
    }

    private String fixHTMLStuff(String s) {
        // Fix html entities to more user friendly
        s = mainActivityInterface.getProcessSong().parseHTML(s);
        // Make it xml friendly though (no <,> or &)
        s = mainActivityInterface.getProcessSong().parseToHTMLEntities(s);
        return s;
    }
}
