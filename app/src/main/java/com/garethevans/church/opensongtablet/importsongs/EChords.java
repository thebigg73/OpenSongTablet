package com.garethevans.church.opensongtablet.importsongs;

import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SongParserInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class EChords implements SongParserInterface {

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
        String lyrics = "";
        String option1 = getSubstring(webString,"<pre","</pre>",false);
        // Get the start
        if (option1.contains(">")) {
            lyrics = option1.substring(option1.indexOf(">") + 1);
        }

        String option2 = getSubstring(webString,"window.SONG_METADATA = {","}", false);
        if (lyrics.isEmpty() && !option2.isEmpty()) {
            lyrics = getSubstring(option2,"CHORDS_CONTENT: \"","\",",false);
            lyrics = lyrics.replace("\\u003C","<");
            lyrics = lyrics.replace("\\u003E",">");
            lyrics = lyrics.replace("\r","\n");
        }

        // Parse them line by line
        String chordIndicator = "<span data-chord";
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
        String option1;
        String option2;
        String option3;
        String option4;
        String extracted = "";
        // Likely in a line like this:
        // <save-visited-chord :id="212517" title="10000 Reasons" artist="Rend Collective Experiment"
        // <ads-tonefuse id="100000048" artist="Rend Collective Experiment" song="10000 Reasons" >
        /*
        window.SONG_METADATA = {
            ...
            NAME: '10000 Reasons',
        }
         */
        /*
        "recordedAs": {
        ...
        "name": "10000 Reasons",
        ...
            {
         */
        option1 = getSubstring(webString,"<save-visited-chord",">",false);
        option2 = getSubstring(webString,"<ads-tonefuse",">", false);
        option3 = getSubstring(webString,"window.SONG_METADATA = {","}", false);
        option4 = getSubstring(webString,"\"recordedAs\": {","{", false);
        if (!option1.isEmpty()) {
            extracted = getSubstring(option1,"title=\"","\"",false);
        }
        if (extracted.isEmpty() && !option2.isEmpty()) {
            extracted = getSubstring(option2,"song=\"","\n", false);
        }
        if (extracted.isEmpty() && !option3.isEmpty()) {
            extracted = getSubstring(option3, "NAME: '", "'",false);
        }
        if (extracted.isEmpty() && !option4.isEmpty()) {
            extracted = getSubstring(option4, "\"name\": \"", "\"",false);
        }
        return extracted.trim();
    }

    private String getArtist(String webString) {
        String option1;
        String option2;
        String option3;
        String option4;
        String extracted = "";
        // Likely in a line like this:
        // <save-visited-chord :id="212517" title="10000 Reasons" artist="Rend Collective Experiment"
        // <ads-tonefuse id="100000048" artist="Rend Collective Experiment" song="10000 Reasons" >
        /*
        window.SONG_METADATA = {
            ...
            ARTIST_NAME: 'Rend Collective Experiment',
            ...
        }
         */
        /*
        "byArtist": {
        ...
        "name": "Rend Collective Experiment",
        ...
        }
         */
        option1 = getSubstring(webString,"<save-visited-chord",">",false);
        option2 = getSubstring(webString,"<ads-tonefuse",">", false);
        option3 = getSubstring(webString,"window.SONG_METADATA = {","}", false);
        option4 = getSubstring(webString,"\"byArtist\": {","}", false);
        if (!option1.isEmpty()) {
            extracted = getSubstring(option1,"artist=\"","\"",false);
        }
        if (extracted.isEmpty() && !option2.isEmpty()) {
            extracted = getSubstring(option2,"artist=\"","\n", false);
        }
        if (extracted.isEmpty() && !option3.isEmpty()) {
            option3 = option3.replace("ARTIST_NAME","THIS_ARTIST");
            extracted = getSubstring(option3, "THIS_ARTIST: '", "'",false);
        }
        if (extracted.isEmpty() && !option4.isEmpty()) {
            extracted = getSubstring(option4, "\"name\": \"", "\"",false);
        }
        return extracted.trim();
    }

    private String getKey(String webString) {
        String option1;
        String option2;
        String option3;
        String option4;
        String extracted = "";
        // Likely in a line like this:
        // <song-change-key original-key="G" :is-premium="false" >
        // <song-change-initial-key original-key="G" :is-premium="false" :int-key="1">
        /*
        window.SONG_METADATA = {
            ...
            CHORDS_KEY: 'G',
            ...
        }
         */
        /*
        "@context": "https://schema.org"
        ...
        "musicalKey": "G"
        ...
        </script>
         */
        option1 = getSubstring(webString,"<song-change-key",">",false);
        option2 = getSubstring(webString,"<song-change-initial-key",">", false);
        option3 = getSubstring(webString,"window.SONG_METADATA = {","}", false);
        option4 = getSubstring(webString,"\"@context\": \"https://schema.org\"","</script>", false);
        if (!option1.isEmpty()) {
            extracted = getSubstring(option1,"original-key=\"","\"",false);
        }
        if (extracted.isEmpty() && !option2.isEmpty()) {
            extracted = getSubstring(option2,"original-key=\"","\n", false);
        }
        if (extracted.isEmpty() && !option3.isEmpty()) {
            extracted = getSubstring(option3, "CHORDS_KEY: '", "'",false);
        }
        if (extracted.isEmpty() && !option4.isEmpty()) {
            extracted = getSubstring(option4, "\"musicalKey\": \"", "\"",false);
        }
        return extracted.trim();
    }

    private String getCapo(String webString) {
        String option1;
        String option2;
        String option3;
        String extracted = "";
        // Likely in a line like this:
        // <song-capo-display  :initial-capo="2"  >
        // <song-change-initial-key original-key="G" :is-premium="false" :int-key="1">
        /*
        window.SONG_METADATA = {
            ...
            CAPO: 0,
            ...
        }
         */
        option1 = getSubstring(webString,"<song-change-key",">",false);
        option2 = getSubstring(webString,"<song-change-initial-key",">", false);
        option3 = getSubstring(webString,"window.SONG_METADATA = {","}", false);
        if (!option1.isEmpty()) {
            extracted = getSubstring(option1,"original-key=\"","\"",false);
        }
        if (extracted.isEmpty() && !option2.isEmpty()) {
            extracted = getSubstring(option2,"original-key=\"","\n", false);
        }
        if (extracted.isEmpty() && !option3.isEmpty()) {
            extracted = getSubstring(option3, "CAPO: ", ",",false);
        }
        return extracted.trim();
    }

    private String getSubstring(String from, String startText, String endText, boolean stripTags) {
        int start = from.indexOf(startText);
        int end = from.indexOf(endText,start+startText.length());
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
