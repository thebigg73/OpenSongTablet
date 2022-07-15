package com.garethevans.church.opensongtablet.importsongs;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class WorshipTogether {

    public Song processContent(MainActivityInterface mainActivityInterface, Song newSong, String s) {
        // From Worship Together

        String filename = "WT Song";
        String author = "";
        String copyright = "";
        String theme = "";
        String key = "";
        String bpm = "";
        String ccli = "";

        // Get the title
        String title = mainActivityInterface.getProcessSong().removeHTMLTags(
                mainActivityInterface.getProcessSong().getSubstring(
                        "<h2",">","</h2>",s).trim());
        if (!title.isEmpty()) {
            filename = title;
        }
        newSong.setTitle(title);
        newSong.setFilename(filename);

        String songTaxonomy = mainActivityInterface.getProcessSong().getSubstring(
                "<div class=\"song_taxonomy\">",null,"<div class=\"p-song-tile g-content-tile\">",s).trim();

        if (!songTaxonomy.isEmpty()) {
            author = mainActivityInterface.getProcessSong().removeHTMLTags(
                    mainActivityInterface.getProcessSong().getSubstring(
                            "Writer(s):",null,"</div>",songTaxonomy).trim());

            copyright = mainActivityInterface.getProcessSong().removeHTMLTags(
                    mainActivityInterface.getProcessSong().getSubstring(
                            "Ministry(s):",null,"</div>",songTaxonomy).trim());

            if (copyright.isEmpty()) {
                copyright = author;
            }

            theme = mainActivityInterface.getProcessSong().removeHTMLTags(
                    mainActivityInterface.getProcessSong().getSubstring(
                            "Theme(s):",null,"</div>",songTaxonomy).trim()).replace(", ",";");

            bpm = mainActivityInterface.getProcessSong().removeHTMLTags(
                    mainActivityInterface.getProcessSong().getSubstring(
                            "BPM:",null,"</div>",songTaxonomy).trim());

            ccli = mainActivityInterface.getProcessSong().removeHTMLTags(
                    mainActivityInterface.getProcessSong().getSubstring(
                            "CCLI #:",null,"</div>",songTaxonomy).trim());

            key = mainActivityInterface.getProcessSong().removeHTMLTags(
                    mainActivityInterface.getProcessSong().getSubstring(
                            "Original Key(s):",null,"</div>",songTaxonomy).trim());

            if (key.isEmpty()) {
                key = mainActivityInterface.getProcessSong().removeHTMLTags(
                        mainActivityInterface.getProcessSong().getSubstring(
                                "Original Key:",null,"</div>",songTaxonomy).trim());

            }
        }

        // Now try to get the lyrics split into lines
        String songProContent = mainActivityInterface.getProcessSong().getSubstring(
                "<div class=\"chord-pro-line\">",null,"<div class=\"col-sm-6\">",s);
        // Try to make sure tags are consistent with attributes using "..." rather than '...'
        songProContent = songProContent.replace("='","=\"");
        songProContent = songProContent.replace("'>","\">");
        songProContent = songProContent.replace("'/>","\"/>");

        String[] lines = songProContent.split("\n");

        // Process each line at at time
        StringBuilder lyrics = new StringBuilder();
        StringBuilder newline = new StringBuilder();
        for (String line:lines) {
            line = line.trim();

            // Determine if the line is just empty <html> declarations
            boolean emptystuff = line.equals("</div") || line.contains("<div class=\"chord-pro-br\">") ||
                    line.contains("<div class=\"chord-pro-segment\">") || line.contains("<div class=\"inner_col\"");

            // Now decide what to do with the line
            if (!emptystuff && line.contains("<div class=\"chord-pro-disp\"")) {
                // Start section, so initialise the newline and lyrics
                lyrics = new StringBuilder();
                newline = new StringBuilder();

            } else if (!emptystuff && line.contains("<div class=\"chord-pro-line\">")) {
                // Starting a new line, so add the previous newline to the lyrics text
                lyrics.append("\n").append(newline);
                newline = new StringBuilder();

            } else if (!emptystuff && line.contains("<div class=\"chord-pro-note\">")) {
                // This is a chord
                String chordbit = mainActivityInterface.getProcessSong().getSubstring(
                        "<div class=\"chord-pro-note\">","'>","</div>",line);
                chordbit = chordbit.replace("&nbsp;"," ");
                if (!chordbit.trim().isEmpty()) {
                    newline.append("[").append(chordbit.trim()).append("]");
                }

            } else if (!emptystuff && line.contains("<div class=\"chord-pro-lyric\">")) {
                // This is lyrics
                String lyricbit = mainActivityInterface.getProcessSong().getSubstring(
                        "<div class=\"chord-pro-lyric\">","'>","</div>",line);
                if (!lyricbit.isEmpty()) {
                    newline.append(lyricbit);
                }
            }
        }

        // Now process the chordpro lyrics into OpenSong format
        String lyricBits = mainActivityInterface.getProcessSong().removeHTMLTags(
                lyrics.toString().trim().replace("\n","_NEWLINE_"));
        lyricBits = lyricBits.replace("_NEWLINE_","\n");
        newSong.setLyrics(mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(lyricBits.trim()));

        // Add the other info the the song
        newSong.setFilename(filename);
        newSong.setTitle(title);
        newSong.setAuthor(author);
        newSong.setCopyright(copyright);
        newSong.setKey(key);
        newSong.setCcli(ccli);
        newSong.setTempo(bpm);
        newSong.setTheme(theme);

        return newSong;
    }
}
