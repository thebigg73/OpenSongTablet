package com.garethevans.church.opensongtablet.importsongs;

import com.garethevans.church.opensongtablet.songprocessing.Song;

public class UkuTabs {

    // This is triggered from the import online option from UkuTabs

    private final String[] bitsToClear = new String[] {"</a>","<strong>","</strong>",
    "<a target=\"_blank\" class=\"ukutabschord\" href=\"https://ukuchords.com/\">",};

    public Song processContent(Song newSong, String webString) {

        // Get the headers
        newSong.setTitle(getSubstring(webString,"song: \"","\","));
        newSong.setAuthor(getSubstring(webString, "artist: \"", "\","));
        newSong.setFilename(newSong.getTitle());

        // Get to the song content
        webString = getSubstring(webString,"<pre class=\"qoate-code fontsize spacing chordcolor\" id=\"ukutabs-song\">","</pre>");

        // Split the song into lines
        String[] lines = webString.split("\n");

        // Now sort heading lines that might also have chords or lyrics
        StringBuilder stringBuilder = new StringBuilder();
        for (String line:lines) {
            if (line.contains("<strong>")) {
                if (!line.startsWith("<strong>")) {
                    line = line.replace("<strong>", "\n<strong>");
                }
                if (!line.endsWith("</strong>")) {
                    line = line.replace("</strong>", "</strong>\n");
                }
            }
            stringBuilder.append(line).append("\n");
        }

        // Now go back through the line
        lines = stringBuilder.toString().split("\n");
        stringBuilder = new StringBuilder();

        // Go through each line and fix the lines
        for (String line:lines) {
            if (line.contains("<strong>")) {
                line = line.replace("<strong>","[");
                line = line.replace("</strong>","]");
                line = line.replace(":]","]");

            } else if (line.contains("class=\"ukutabschord\"")) {
                line = "." + line;
            } else if (!line.isEmpty()) {
                line = " " + line;
            }

            // Just in case the text already had the [..]
            line = line.replace("[[","[");
            line = line.replace("]]","]");

            if (!line.equals(" :")) {
                stringBuilder.append(line).append("\n");
            }
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

}
