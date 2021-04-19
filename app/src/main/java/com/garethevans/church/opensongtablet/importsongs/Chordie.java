package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class Chordie {

    // Song should be a fully processed ChoPro format
    // The contents are contained in a textarea: <textarea id="chordproContent" name="chopro" style="display: none;">..</textarea>
    // This means we just need to get the best title, author, etc. and then process the chopro

    public Song processContent(Context c, MainActivityInterface mainActivityInterface,
                               Song newSong, String s) {

        // Get the chopro bit
        String chopro = "";
        int firstid = s.indexOf("<textarea id=\"chordproContent\"");
        int start = s.indexOf(">",firstid)+1;
        int end = s.indexOf("</textarea>",start);
        if (start>0 && end>start) {
            chopro = s.substring(start,end);
        }
        chopro = mainActivityInterface.getProcessSong().parseHTML(chopro);

        // Send the stuff off to the ConvertChoPro class for processing
        // Send anull uri to stop it trying to save the file
        // This will return the improved song!
        newSong.setLyrics(chopro);
        newSong = mainActivityInterface.getConvertChoPro().convertTextToTags(c,mainActivityInterface,null,newSong);

        // If the title is null/empty (not specified in chopro {t:), use the title from the webpage
        if (newSong.getTitle()==null || newSong.getTitle().isEmpty()) {
            start = s.indexOf("<title>")+7;
            end = s.indexOf("</title>",start);
            if (start>0 && end>start) {
                String title = s.substring(start,end);
                newSong.setFilename(title);
                newSong.setTitle(title);
            }
        }

    return newSong;
    }

}
