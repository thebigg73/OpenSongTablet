package com.garethevans.church.opensongtablet.importsongs;

import android.util.Log;

import com.garethevans.church.opensongtablet.songprocessing.Song;

public class HolyChords {

    // This extracts the song from the HolyChords website

    private final String TAG = "HolyChords";
    private final String[] bitsToClear = new String[] {"<span class=\"chopds\">","<a class=\"tooltip\">",
            "<span class=\"tooltip-content\">","<span class=\"c\">","<span class=\"text\">\"","</a>",
            "</span>","-->","<!--"};


    public Song processContent(Song newSong, String webString) {

        // Get the headers
        newSong.setTitle(getSubstring(webString,"<meta property=\"music:song\" content=\"","\">").trim());
        newSong.setAuthor(getSubstring(webString,"<meta property=\"music:musician\" content=\"","\">").trim());
        newSong.setFilename(newSong.getTitle());

        // Get the actual song content - this leaves the key as a class
        webString = getSubstring(webString,"<pre id=\"music_text\" style=\"font-size: 14px; position: relative;\" ","</pre>");
        newSong.setKey(getSubstring(webString,"class=\"", "\">"));
        webString = webString.replace("class=\""+newSong.getKey()+"\">","");

        // Remove the image tags
        webString = removeImageTags(webString);

        // Split into lines and fix
        String[] lines = webString.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        for (String line:lines) {
            if (line.contains("<b class=\"videlit_line\">")) {
                // Section header
                line = line.replace("<b class=\"videlit_line\">","[");
                line = line.replace("</b>","]");
                line = line.replace(":]","]");

            } else if (line.contains("<span class=\"c\">")) {
                // Chord line
                line = "." + line;
            } else if (line.contains("<span class=\"text\">")) {
                line = " " + line;
            }
            stringBuilder.append(line).append("\n");
        }

        newSong.setLyrics(stripOutTags(stringBuilder.toString()));

        return newSong;
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

    private String removeImageTags(String s) {
        boolean keepGoing = true;
        while (keepGoing) {
            int start = s.indexOf("<img src=\"");
            int end = s.indexOf(">",start);
            if (start>-1 && end>start) {
                String bitToRemove = s.substring(start,end+1);
                Log.d(TAG,"bitToRemove="+bitToRemove);
                s = s.replace(bitToRemove,"");
            } else {
                keepGoing = false;
            }
        }
        return s;
    }
}
