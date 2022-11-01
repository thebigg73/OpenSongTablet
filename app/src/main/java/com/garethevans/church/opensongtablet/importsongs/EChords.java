package com.garethevans.church.opensongtablet.importsongs;

import android.util.Log;

import com.garethevans.church.opensongtablet.songprocessing.Song;

public class EChords {

    private final String TAG = "EChords";

    public Song processContent(Song song, String webString) {

        // Get the title
        String title = getSubstring(webString,"<h1>", "</h1>",true);
        title = title.replace("(Chords)","").trim();
        song.setTitle(title);
        song.setFilename(title);


        // Get the author
        song.setAuthor(getSubstring(webString,"<h2>", "</h2>",true).trim());

        // Get the key
        song.setKey(getSubstring(webString,"<div>Key:","<span class=\"caret\">",true).trim());

        // Get the lyrics
        String lyrics = getSubstring(webString,"<pre id=\"core\">","</pre>",false);

        // Parse them line by line
        String chordIndicator = "<u><span class=\"showchord\"";
        StringBuilder stringBuilder = new StringBuilder();
        String[] lines = lyrics.split("\n");
        for (String line:lines) {
            if (line.contains(chordIndicator)) {
                // This is a chord line
                if (line.trim().startsWith(chordIndicator)) {
                    // Just add the period to the start and strip out the tags
                    stringBuilder.append(".").append(stripOutTags(line)).append("\n");
                } else {
                    // This line has something else first, so add a new line then period
                    String bitBefore = line.substring(0,line.indexOf(chordIndicator)).trim();
                    line = line.replace(bitBefore,bitBefore+"\n.");
                    stringBuilder.append(stripOutTags(line)).append("\n");
                }
            } else {
                if (!line.startsWith(" ")) {
                    line = " " + stripOutTags(line);
                }
                stringBuilder.append(line).append("\n");
            }
        }

        song.setLyrics(stringBuilder.toString());
        Log.d(TAG,"lyrics: "+song.getLyrics());
        Log.d(TAG,"title: "+song.getTitle());
        Log.d(TAG, "author: "+song.getAuthor());
        Log.d(TAG, "key:" + song.getKey());

        return song;
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
}
