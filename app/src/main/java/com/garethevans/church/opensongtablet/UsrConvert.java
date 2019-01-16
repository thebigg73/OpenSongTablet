package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;

import java.util.ArrayList;

class UsrConvert {

    // Declare the variables;
    private String title;
    private String author;
    private String key;
    private String copyright;
    private String ccli;
    private String time_sig;
    private String lyrics;
    private String theme;
    private String oldSongFileName;
    private String newSongFileName;
    private String songSubFolder;
    private String tags;
    private String[] lines;
    private StringBuilder parsedLines;

    ArrayList<String> convertTextToTags(Context c, StorageAccess storageAccess, Preferences preferences,
                                        SongXML songXML, ChordProConvert chordProConvert, Uri uri, String l, int pos) {

        initialiseTheVariables();

        lyrics = l;

        // Fix line breaks and slashes
        lyrics = chordProConvert.fixLineBreaksAndSlashes(lyrics);

        // Fix content we recognise
        lyrics = fixRecognisedContent(c, lyrics);

        // Get rid of multilple line breaks (max of 3 together)
        lyrics = chordProConvert.getRidOfExtraLines(lyrics);

        // Add spaces to beginnings of lines that aren't comments, chords or tags
        lyrics = chordProConvert.addSpacesToLines(lyrics);

        // Initialise the song tags
        songXML.initialiseSongTags();

        // Prepare the correct values
        setCorrectXMLValues();

        // Now prepare the new songXML file
        FullscreenActivity.myXML = songXML.getXML();

        // Get the filename and subfolder (if any) that the original song was in by parsing the uri
        oldSongFileName = chordProConvert.getOldSongFileName(uri);
        songSubFolder = chordProConvert.getSongFolderLocation(storageAccess, uri);

        // Prepare the new song filename
        newSongFileName = chordProConvert.getNewSongFileName(uri, title);

        // Get a unique uri for the new song
        Uri newUri = chordProConvert.getNewSongUri(c, storageAccess, preferences, songSubFolder, newSongFileName);

        // Now write the modified song
        chordProConvert.writeTheImprovedSong(c, storageAccess, preferences, oldSongFileName, newSongFileName,
                songSubFolder, newUri, uri, pos);

        return chordProConvert.bitsForIndexing(newSongFileName, title, author, copyright, key, time_sig, ccli, lyrics);

    }

    private void initialiseTheVariables() {
        title = "";
        author = "";
        key = "";
        copyright = "";
        ccli = "";
        time_sig = "";
        oldSongFileName = "";
        newSongFileName = "";
        songSubFolder = "";
        lines = null;
        theme = "";

        parsedLines = new StringBuilder();
    }

    private String fixRecognisedContent(Context c, String l) {
        // Break the filecontents into lines
        lines = l.split("\n");

        // This will be the new lyrics lines
        parsedLines = new StringBuilder();
        for (String line : lines) {
            // Get rid of any extra whitespace
            line = line.trim();

            String words;
            if (line.contains("Title=")) {
                // Extract the title and empty the line (don't need to keep it)
                title = removeTags(line, "Title=").trim();
                line = "";

            } else if (line.contains("Author=")) {
                // Extract the author and empty the line (don't need to keep it)
                author = removeTags(line, "Author=").trim();
                line = "";

            } else if (line.contains("Copyright=")) {
                // Extract the copyright and empty the line (don't need to keep it)
                copyright = removeTags(line, "Copyright=");
                line = "";

            } else if (line.contains("[S A")) {
                ccli = removeTags(line, "[S A");
                ccli = removeTags(ccli, "]");
                line = "";

            } else if (line.contains("Theme=")) {
                theme = removeTags(line, "Theme=");
                line = "";

            } else if (line.contains("Keys=")) {
                key = removeTags(line, "Keys=");
                line = "";

            } else if (line.contains("Fields=")) {
                tags = line.replace("Fields=", "");
                // Replace known fields
                tags = tags.replace(c.getResources().getString(R.string.tag_bridge) + " ", "B");
                tags = tags.replace(c.getResources().getString(R.string.tag_bridge), "B");
                tags = tags.replace(c.getResources().getString(R.string.tag_prechorus) + " ", "P");
                tags = tags.replace(c.getResources().getString(R.string.tag_prechorus), "B");
                tags = tags.replace(c.getResources().getString(R.string.tag_chorus) + " ", "C");
                tags = tags.replace(c.getResources().getString(R.string.tag_chorus), "C");
                tags = tags.replace(c.getResources().getString(R.string.tag_verse) + " ", "V");
                tags = tags.replace(c.getResources().getString(R.string.tag_verse), "V");
                tags = tags.replace(c.getResources().getString(R.string.tag_tag) + " ", "T");
                tags = tags.replace(c.getResources().getString(R.string.tag_tag), "T");
                line = "";

            } else if (line.contains("Words=")) {
                words = removeTags(line, "Words=");

            }

            // Fix the line separator
            words = line.replace("|", ",");

            // Fix the newline tag for words
            words = words.replace("/n", "\n");

            // Split the words up by sections
            String[] sections = words.split("/t");

            // Split the section titles up
            String[] sectiontitles = tags.split("/t");

            // Go through the sections and add the appropriate tag
            for (int w = 0; w < sections.length; w++) {
                if (sections[w].startsWith("(")) {
                    sections[w] = sections[w].replace("(", "[");
                    sections[w] = sections[w].replace(")", "]");
                    int tagstart = sections[w].indexOf("[");
                    int tagend = sections[w].indexOf("]");
                    String customtag = "";
                    if (tagstart > -1 && tagend > 1) {
                        customtag = sections[w].substring(tagstart + 1, tagend - 1);
                    }
                    String newtag = customtag;
                    // Replace any know custom tags
                    newtag = newtag.replace(c.getResources().getString(R.string.tag_bridge) + " ", "B");
                    newtag = newtag.replace(c.getResources().getString(R.string.tag_bridge), "B");
                    newtag = newtag.replace(c.getResources().getString(R.string.tag_prechorus) + " ", "P");
                    newtag = newtag.replace(c.getResources().getString(R.string.tag_prechorus), "B");
                    newtag = newtag.replace(c.getResources().getString(R.string.tag_chorus) + " ", "C");
                    newtag = newtag.replace(c.getResources().getString(R.string.tag_chorus), "C");
                    newtag = newtag.replace(c.getResources().getString(R.string.tag_verse) + " ", "V");
                    newtag = newtag.replace(c.getResources().getString(R.string.tag_verse), "V");
                    newtag = newtag.replace(c.getResources().getString(R.string.tag_tag) + " ", "T");
                    newtag = newtag.replace(c.getResources().getString(R.string.tag_tag), "T");
                    sections[w] = sections[w].replace(customtag, newtag);

                } else {
                    if (sectiontitles[w] != null) {
                        sections[w] = "[" + sectiontitles[w] + "]\n" + sections[w];
                    }
                }
                // Fix all line breaks
                sections[w] = sections[w].replace("/n", "\n ");

                parsedLines.append(sections[w]).append("\n");
            }
        }

        lyrics = parsedLines.toString();
        // Get rid of double line breaks
        while (lyrics.contains("\n\n\n")) {
            lyrics = lyrics.replace("\n\n\n", "\n\n");
        }

        return lyrics;
    }

    private String removeTags(String s, String bit) {
        return s.replace(bit, "");
    }

    private void setCorrectXMLValues() {
        // Set the correct values
        FullscreenActivity.mTitle = title.trim();
        FullscreenActivity.mAuthor = author.trim();
        FullscreenActivity.mCopyright = copyright.trim();
        FullscreenActivity.mCCLI = ccli.trim();
        FullscreenActivity.mTheme = theme.trim();
        FullscreenActivity.mKey = key.trim();
        FullscreenActivity.mLyrics = lyrics.trim();
    }


    // TODO remove this
    boolean doExtract(Context c, Preferences preferences) {

        return true;
    }
}