package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;

import java.util.ArrayList;

class OnSongConvert {

    // This is virtually the same as ChordProConvert, but with a few extra tags
    // To simplify this, we will extract the specific OnSongStuff first and then pass it to ChordProConvert

    // Declare the variables;
    private String title;
    private String author;
    private String key;
    private String capo;
    private String capoprint;
    private String copyright;
    private String ccli;
    private String tempo;
    private String time_sig;
    private String lyrics;
    private String midi;
    private String midiindex;
    private String duration;
    private String number;
    private String flow;
    private String pitch;
    private String restrictions;
    private String book;
    private String theme;
    private String oldSongFileName;
    private String newSongFileName;
    private String songSubFolder;
    private String[] lines;
    private StringBuilder parsedLines;

    ArrayList<String> convertTextToTags(Context c, StorageAccess storageAccess, Preferences preferences,
                                        SongXML songXML, ChordProConvert chordProConvert, Uri uri, String l) {

        initialiseTheVariables();

        lyrics = l;

        // Fix line breaks and slashes
        lyrics = chordProConvert.fixLineBreaksAndSlashes(lyrics);

        // Fix specific OnSong tags
        lyrics = fixOnSongTags(lyrics);

        // Make tag lines common
        lyrics = chordProConvert.makeTagsCommon(lyrics);

        // Fix content we recognise as OnSongTags
        lyrics = fixRecognisedContent(lyrics, chordProConvert);

        // Now that we have the basics in place, we will go back through the song and extract headings
        // We have to do this separately as [] were previously identifying chords, not tags.
        // Chords have now been extracted to chord lines
        lyrics = chordProConvert.removeOtherTags(lyrics);

        // Get rid of multilple line breaks (max of 3 together)
        lyrics = chordProConvert.getRidOfExtraLines(lyrics);

        // Add spaces to beginnings of lines that aren't comments, chords or tags
        lyrics = chordProConvert.addSpacesToLines(lyrics);

        // Get the filename and subfolder (if any) that the original song was in by parsing the uri
        oldSongFileName = chordProConvert.getOldSongFileName(uri);
        songSubFolder = chordProConvert.getSongFolderLocation(storageAccess, uri, oldSongFileName);

        // Prepare the new song filename
        newSongFileName = chordProConvert.getNewSongFileName(storageAccess, uri, title);

        // By default, set the title to the new filename
        StaticVariables.songfilename = newSongFileName;

        // Initialise the variables
        songXML.initialiseSongTags();

        // Set the correct values
        setCorrectXMLValues();

        // Now prepare the new songXML file
        FullscreenActivity.myXML = songXML.getXML();

        // Get a unique uri for the new song
        Uri newUri = chordProConvert.getNewSongUri(c, storageAccess, preferences, songSubFolder, newSongFileName);
        // IV - Adjusted to handle files at root of drive
        newSongFileName = newUri.getLastPathSegment().replace(":","/");
        // Just in case it had _ appended due to name conflict.
        // Get rid of the rubbish...
        if (newSongFileName.contains("/")) {
            newSongFileName = newSongFileName.substring(newSongFileName.lastIndexOf("/"));
            newSongFileName = newSongFileName.replace("/","");
        }

        // Now write the modified song
        chordProConvert.writeTheImprovedSong(c, storageAccess, preferences, oldSongFileName, newSongFileName,
                songSubFolder, newUri, uri);

        // Add it to the database
        return chordProConvert.bitsForIndexing(newSongFileName, title, author, copyright, key, time_sig, ccli, lyrics);
    }

    private void initialiseTheVariables() {
        title = "";
        author = "";
        key = "";
        capo = "";
        capoprint = "";
        copyright = "";
        ccli = "";
        tempo = "";
        time_sig = "";
        oldSongFileName = "";
        newSongFileName = "";
        songSubFolder = "";
        lines = null;
        midi = "";
        midiindex = "";
        duration = "";
        number = "";
        flow = "";
        pitch = "";
        restrictions = "";
        book = "";
        theme = "";

        parsedLines = new StringBuilder();
    }

    private String fixOnSongTags(String l) {
        l = l.replace("{artist :", "{artist:");
        l = l.replace("{a:", "{artist:");
        l = l.replace("{author :", "{author:");
        l = l.replace("{copyright :", "{copyright:");
        l = l.replace("{footer:", "{copyright:");
        l = l.replace("{footer :", "{copyright:");
        l = l.replace("{key :", "{key:");
        l = l.replace("{k:", "{key:");
        l = l.replace("{k :", "{key:");
        l = l.replace("{capo :", "{capo:");
        l = l.replace("{time :", "{time:");
        l = l.replace("{tempo :", "{tempo:");
        l = l.replace("{duration :", "{duration:");
        l = l.replace("{number :", "{number:");
        l = l.replace("{flow :", "{flow:");
        l = l.replace("{ccli :", "{ccli:");
        l = l.replace("{keywords :", "{keywords:");
        l = l.replace("{topic:", "{keywords:");
        l = l.replace("{topic :", "{keywords:");
        l = l.replace("{book :", "{book:");
        l = l.replace("{midi :", "{midi:");
        l = l.replace("{midi-index :", "{midi-index:");
        l = l.replace("{pitch :", "{pitch:");
        l = l.replace("{restrictions :", "{restrictions:");

        return l;
    }

    private String fixRecognisedContent(String l, ChordProConvert chordProConvert) {
        // Break the filecontents into lines
        lines = l.split("\n");

        // This will be the new lyrics lines
        parsedLines = new StringBuilder();
        for (String line : lines) {
            // Get rid of any extra whitespace
            line = line.trim();

            // Remove directive lines we don't need
            line = chordProConvert.removeObsolete(line);

            if (line.contains("{title:") || line.contains("Title:")) {
                // Extract the title and empty the line (don't need to keep it)
                line = chordProConvert.removeTags(line, "{title:");
                line = chordProConvert.removeTags(line, "Title:");
                title = line.trim();
                line = "";

            } else if (line.contains("{artist:") || line.contains("Artist:") || line.contains("Author:")) {
                // Extract the author and empty the line (don't need to keep it)
                line = chordProConvert.removeTags(line, "{artist:");
                line = chordProConvert.removeTags(line, "Artist:");
                line = chordProConvert.removeTags(line, "Author:");
                author = line.trim();
                line = "";

            } else if (line.contains("{copyright:") || line.contains("Copyright:") || line.contains("Footer:")) {
                line = chordProConvert.removeTags(line, "{copyright:");
                line = chordProConvert.removeTags(line, "Copyright::");
                line = chordProConvert.removeTags(line, "Footer:");
                copyright = line.trim();
                line = "";

            } else if (line.contains("{subtitle:")) {
                // Extract the subtitles.  Add it back as a comment line
                String subtitle = chordProConvert.removeTags(line, "{subtitle:");
                if (author.equals("")) {
                    author = subtitle;
                }
                if (copyright.equals("")) {
                    copyright = subtitle;
                }
                line = ";" + subtitle;

            } else if (line.contains("{ccli:") || line.contains("CCLI:")) {
                // Extract the ccli (not really a chordpro tag, but works for songselect and worship together
                line = chordProConvert.removeTags(line, "{ccli:").trim();
                line = chordProConvert.removeTags(line, "CCLI:").trim();
                ccli = line.trim();
                line = "";

            } else if (line.contains("{key:") || line.contains("Key:")) {
                // Extract the key
                line = chordProConvert.removeTags(line, "{key:");
                line = chordProConvert.removeTags(line, "Key:");
                line = line.replace("[", "");
                line = line.replace("]", "");
                key = line.trim();
                line = "";

            } else if (line.contains("{capo:") || line.contains("Capo:")) {
                line = chordProConvert.removeTags(line, "{capo:");
                line = chordProConvert.removeTags(line, "Capo:");
                capo = line.trim();
                capoprint = "true";

            } else if (line.contains("{tempo:") || line.contains("Tempo:")) {
                line = chordProConvert.removeTags(line, "{tempo:");
                line = chordProConvert.removeTags(line, "Tempo:");
                tempo = line.trim();
                line = "";

            } else if (line.contains("{time:") || line.contains("Time:")) {
                // Extract the timesig
                line = chordProConvert.removeTags(line, "{time:");
                line = chordProConvert.removeTags(line, "Time:");
                time_sig = line.trim();
                line = "";

            } else if (line.contains("{duration:") || line.contains("Duration:")) {
                line = chordProConvert.removeTags(line, "{duration:");
                line = chordProConvert.removeTags(line, "Duration:");
                duration = line.trim();
                line = "";

            } else if (line.contains("{number:") || line.contains("Number:")) {
                line = chordProConvert.removeTags(line, "{number:");
                line = chordProConvert.removeTags(line, "Number:");
                number = line.trim();
                line = "";

            } else if (line.contains("{flow:") || line.contains("Flow:")) {
                line = chordProConvert.removeTags(line, "{flow:");
                line = chordProConvert.removeTags(line, "Flow:");
                flow = line.trim();
                line = "";

            } else if (line.contains("{keywords:") || line.contains("Keywords:") || line.contains("Topic:")) {
                line = chordProConvert.removeTags(line, "{keywords:");
                line = chordProConvert.removeTags(line, "Keywords:");
                line = chordProConvert.removeTags(line, "Topic:");
                theme = line.trim();
                line = "";

            } else if (line.contains("{book:") || line.contains("Book:")) {
                line = chordProConvert.removeTags(line, "{book:");
                line = chordProConvert.removeTags(line, "Book:");
                book = line.trim();
                line = "";

            } else if (line.contains("{midi:") || line.contains("MIDI:")) {
                line = chordProConvert.removeTags(line, "{midi:");
                line = chordProConvert.removeTags(line, "MIDI:");
                midi = line.trim();
                line = "";

            } else if (line.contains("{midi-index:") || line.contains("MIDI-Index:")) {
                line = chordProConvert.removeTags(line, "{midi-index:");
                line = chordProConvert.removeTags(line, "MIDI-Index:");
                midiindex = line.trim();
                line = "";

            } else if (line.contains("{pitch:") || line.contains("Pitch:")) {
                line = chordProConvert.removeTags(line, "{pitch:");
                line = chordProConvert.removeTags(line, "Pitch:");
                pitch = line.trim();
                line = "";

            } else if (line.contains("{restrictions:") || line.contains("Restrictions:")) {
                line = chordProConvert.removeTags(line, "{restrictions:");
                line = chordProConvert.removeTags(line, "Restrictions:");
                restrictions = line.trim();
                line = "";

            } else if (line.startsWith("#")) {
                // Change lines that start with # into comment lines
                line = line.replaceFirst("#", ";");

            } else if (line.contains("{comments:") || line.contains("{comment:")) {
                // Change comment lines
                line = ";" + chordProConvert.removeTags(line, "{comments:").trim();
                line = ";" + chordProConvert.removeTags(line, "{comment:").trim();

            }

            // Fix guitar tab so it fits OpenSongApp formatting ;e |
            line = chordProConvert.tryToFixTabLine(line);

            if (line.startsWith(";;")) {
                line = line.replace(";;", ";");
            }

            // Now split lines with chords in them into two lines of chords then lyrics
            line = chordProConvert.extractChordLines(line);

            line = line.trim() + "\n";
            parsedLines.append(line);

        }
        return parsedLines.toString();
    }

    private void setCorrectXMLValues() {
        if (title == null || title.isEmpty()) {
            StaticVariables.mTitle = newSongFileName;
        } else {
            StaticVariables.mTitle = title.trim();
        }
        StaticVariables.mAuthor = author.trim();
        StaticVariables.mCopyright = copyright.trim();
        StaticVariables.mTempo = tempo.trim();
        StaticVariables.mTimeSig = time_sig.trim();
        StaticVariables.mCCLI = ccli.trim();
        StaticVariables.mKey = key.trim();
        StaticVariables.mLyrics = lyrics.trim();
        StaticVariables.mCapo = capo.trim();
        StaticVariables.mCapoPrint = capoprint.trim();
        StaticVariables.mMidi = midi.trim();
        StaticVariables.mMidiIndex = midiindex.trim();
        StaticVariables.mDuration = duration.trim();
        StaticVariables.mPresentation = flow.trim();
        StaticVariables.mHymnNumber = number.trim();
        StaticVariables.mPitch = pitch.trim();
        StaticVariables.mRestrictions = restrictions.trim();
        StaticVariables.mBooks = book.trim();
        StaticVariables.mTheme = theme.trim();
    }

}