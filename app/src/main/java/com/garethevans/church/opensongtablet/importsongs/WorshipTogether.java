package com.garethevans.church.opensongtablet.importsongs;

import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class WorshipTogether {

    private final String TAG = "WorshipTogether";
    public Song processContent(MainActivityInterface mainActivityInterface,
                               Song newSong, String s) {

        // Get the title
        int start = s.indexOf("<h2");
        start = s.indexOf(">",start)+1;
        int end = s.indexOf("</h2>",start);
        String title = newSong.getTitle();
        if (start>1 && end>start) {
            title = s.substring(start,end).trim();
            Log.d(TAG,"Found title: "+title);
            newSong.setTitle(title);
        }
        newSong.setFilename(title);

        // Get the author
        start = s.indexOf("<span>Writer");
        start = s.indexOf("<p>",start)+3;
        end = s.indexOf("</p>",start);
        String author = "";
        if (start>3 && end>start) {
            author = s.substring(start,end).trim();
            Log.d(TAG,"Found author: "+author);
        }
        newSong.setAuthor(author);

        // Get the tempo
        start = s.indexOf("<span>BPM:");
        start = s.indexOf("<p>",start)+3;
        end = s.indexOf("</p>",start);
        String bpm = "";
        if (start>3 && end>start) {
            bpm = s.substring(start,end).trim();
            Log.d(TAG,"Found tempo: "+bpm);
        }
        newSong.setTempo(bpm);

        // Get the key
        start = s.indexOf("<span>Original Key:");
        start = s.indexOf("<a",start);
        start = s.indexOf(">",start)+1;
        end = s.indexOf("</a>",start);
        String key = "";
        if (start>1 && end>start) {
            key = s.substring(start,end).trim();
            Log.d(TAG,"Found key: "+key);
        }
        newSong.setKey(key);

        // Get the ccli
        start = s.indexOf("<span>CCLI #:");
        start = s.indexOf("<p>",start)+3;
        end = s.indexOf("</p>",start);
        String ccli = "";
        if (start>3 && end>start) {
            ccli = s.substring(start,end).trim();
            Log.d(TAG,"Found ccli: "+ccli);
        }
        newSong.setCcli(ccli);

        // Now to find the chordpro lyrics
        String chopro = "";
        start = s.indexOf("<div class=\"chord-pro-line\">")+28;
        end = s.indexOf("<div class=\"col-sm-6\">",start);
        if (start>28 && end>start) {
            chopro = s.substring(start,end);
        }
        chopro = mainActivityInterface.getProcessSong().parseHTML(chopro);
        chopro = removeTags(chopro);


        // Send the stuff off to the ConvertChoPro class for processing
        // Send anull uri to stop it trying to save the file
        // This will return the improved song!
        newSong.setLyrics(chopro);
        newSong = mainActivityInterface.getConvertChoPro().convertTextToTags(null,newSong);

        return newSong;
    }

    private String removeTags(String s) {
        // Split into chordPro lines
        String[] sections = s.split("<div class=\"chord-pro-line\">");
        StringBuilder newLines = new StringBuilder();

        for (String section:sections) {
            String[] lines = section.split("\n");

            StringBuilder newLine = new StringBuilder();
            for (String line:lines) {
                line = line.trim();
                line = line.replace("<!--", "");
                line = line.replace("-->", "");
                line = line.replace("<div class=\"chord-pro-segment\">", "");
                line = line.replace("<div class=\"chord-pro-note\">", "");
                line = line.replace("<span class=\"matchedChordContainer\">","[");
                line = line.replace("</span></div>","]");
                line = line.replace("&nbsp;","");
                line = line.replace("<span class=\"suffix\">&nbsp;</span></span>","]");
                line = line.replace("<span class=\"suffix\"></span></span>","]");
                line = line.replace("</span></span>","]");
                line = line.replace(" ]","]");
                line = line.replace("<span class=\"matchedChordContainer\">", "");
                line = line.replace("<span class=\"matchedChord\" chordnumber=", "");
                line = line.replace("\"???\">", "");
                for (int i = 0; i < 13; i++) {
                    line = line.replace("\"" + i + "\">", "");
                    line = line.replace("\"#" + i + "\">","");
                    line = line.replace("\"b" + i + "\">","");
                }
                line = line.replace("<span class=\"suffix\">", "");
                line = line.replace("</span>", "");
                line = line.replace("</div>", "");
                line = line.replace("<div class=\"chord-pro-lyric\">", "");
                line = line.replace("<div class='chord-pro-br'>", "");
                line = line.replace("]/[","/");
                line = line.replace(" ]","]");
                line = line.replace("[ ]","\n");
                line = line.replace("[&nbsp;]","\n");
                line = line.replace("[]","\n");

                if (!line.trim().isEmpty()) {
                    newLine.append(line);
                }
            }
            newLines.append(newLine).append("\n");
        }

        Log.d(TAG,"newLines: "+newLines);

        return newLines.toString();
    }
}
