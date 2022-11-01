package com.garethevans.church.opensongtablet.importsongs;

import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class Boiteachansons {

    // Extracts from La boite a chansons

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Boitechansons";

    public Song processContent(MainActivityInterface mainActivityInterface, Song newSong, String webString) {

        Log.d(TAG,"webString:"+webString);

        // Get the headers
        newSong.setTitle(getSubstring(webString,"<div class=\"dTitreChanson\">","</div>").replace("&nbsp;"," ").trim());
        String author = getSubstring(webString,"<div id=\"dTitreNomArtiste\">","</div>");
        newSong.setAuthor(stripOutTags(author.replace("&nbsp;"," ").trim()));
        newSong.setFilename(newSong.getTitle());
        newSong.setKey(getSubstring(webString,"tonique=\"","\""));

        // Get to the song content
        webString = getSubstring(webString,"<div id=\"divPartition\" class=\"divPartition\">","<div id=\"divDroitePagePartition\"");

        // Replace chords with chopro identifiers
        webString = webString.replace("<span class=\"accLgnInstrumentale\">&nbsp;<a>","[");
        webString = webString.replace("<span class=\"interl\">&nbsp;<a>","[");
        webString = webString.replace("\n","");
        webString = webString.replace("<div class=\"pLgn\">","\n");
        webString = webString.replace("<div class=\"pLgnVide\">","\n");
        webString = webString.replace("<div class=\"pLgnSmpl\">","\n");
        webString = webString.replace("\n\n\n\n","\n\n");
        webString = webString.replace("\n\n\n","\n\n");
        webString = webString.replace("</a>&nbsp;</span>","]");
        webString = webString.replace("&nbsp;"," ");
        webString = webString.replace("<span class=\"interl\"> <a>","[");
        webString = webString.replace("<span class=\"interl\"><a>","[");
        webString = webString.replace("</a> </span>","]");
        webString = webString.replace("</a></span>","]");

        Log.d(TAG,"webString:"+webString);
        webString = stripOutTags(webString);

        Log.d(TAG,"webString:"+webString);

        // Send the lyrics off to chopro for processing
        webString = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(webString);
        Log.d(TAG,"webString:"+webString);

        newSong.setLyrics(webString);

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
        s = s.replaceAll("<(.*?)>", "");
        return s;
    }

}
