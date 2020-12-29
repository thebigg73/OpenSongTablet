package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

public class ConvertOnSong {

    // This is virtually the same as convertChoPro, but with a few extra tags
    // To simplify this, we will extract the specific OnSongStuff first and then pass it to convertChoPro

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
    private String theme;
    private String oldSongFileName;
    private String newSongFileName;
    private String songSubFolder;
    private String[] lines;
    private StringBuilder parsedLines;

    public Song convertTextToTags(Context c, StorageAccess storageAccess, Preferences preferences,
                                               ProcessSong processSong, ConvertChoPro convertChoPro,
                                               SQLiteHelper sqLiteHelper, CommonSQL commonSQL, Uri uri, Song song) {

        initialiseTheVariables();

        lyrics = song.getLyrics();

        // Fix line breaks and slashes
        lyrics = processSong.fixLineBreaksAndSlashes(lyrics);

        // Fix specific OnSong tags
        lyrics = fixOnSongTags(lyrics);

        // Make tag lines common
        lyrics = convertChoPro.makeTagsCommon(lyrics);

        // Fix content we recognise as OnSongTags
        lyrics = fixRecognisedContent(lyrics, convertChoPro);

        // Now that we have the basics in place, we will go back through the song and extract headings
        // We have to do this separately as [] were previously identifying chords, not tags.
        // Chords have now been extracted to chord lines
        lyrics = convertChoPro.removeOtherTags(lyrics);

        // Get rid of multilple line breaks (max of 3 together)
        lyrics = convertChoPro.getRidOfExtraLines(lyrics);

        // Add spaces to beginnings of lines that aren't comments, chords or tags
        lyrics = convertChoPro.addSpacesToLines(lyrics);

        // Get the filename and subfolder (if any) that the original song was in by parsing the uri
        oldSongFileName = convertChoPro.getOldSongFileName(uri);
        songSubFolder = convertChoPro.getSongFolderLocation(storageAccess, uri, oldSongFileName);

        // Prepare the new song filename
        newSongFileName = convertChoPro.getNewSongFileName(storageAccess, uri, title, processSong);

        // By default, set the title to the new filename
        StaticVariables.songfilename = newSongFileName;


        // Set the correct values
        setCorrectXMLValues(song);

        // Now prepare the new songXML file
        String myNewXML = song.getXML(song,processSong);

        // Get a unique uri for the new song
        Uri newUri = convertChoPro.getNewSongUri(c, storageAccess, preferences, songSubFolder, newSongFileName);
        newSongFileName = newUri.getLastPathSegment();
        // Just in case it had _ appended due to name conflict.
        // Get rid of the rubbish...
        if (newSongFileName!=null && newSongFileName.contains("/")) {
            newSongFileName = newSongFileName.substring(newSongFileName.lastIndexOf("/"));
            newSongFileName = newSongFileName.replace("/","");
        }

        song.setFilename(commonSQL.escapedSQL(newSongFileName));

        // Now write the modified song
        convertChoPro.writeTheImprovedSong(c, storageAccess, preferences, sqLiteHelper, commonSQL, song, oldSongFileName, newSongFileName,
                songSubFolder, newUri, uri, myNewXML);

        // Add it to the database
        song.setFilename(newSongFileName);
        song.setTitle(title);
        song.setAuthor(author);
        song.setCopyright(copyright);
        song.setKey(key);
        song.setTimesig(time_sig);
        song.setCcli(ccli);
        song.setLyrics(lyrics);

        return song;
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

    private String fixRecognisedContent(String l, ConvertChoPro convertChoPro) {
        // Break the filecontents into lines
        lines = l.split("\n");

        // IV - Handle tagless 1st and 2nd lines as Title and Artist
        if ((lines.length > 0) && (!lines[0].contains(":"))) {
            title = lines[0].trim();
            lines[0] = "";
        }
        if ((lines.length > 1) && (!lines[1].contains(":"))) {
            // IV - Change ';' to ',' - the separator used by CCLI
            author = lines[1].trim().replace(";",",");
            lines[1] = "";
        }

        // This will be the new lyrics lines
        parsedLines = new StringBuilder();
        for (String line : lines) {
            // Get rid of any extra whitespace
            line = line.trim();

            // Remove directive lines we don't need
            line = convertChoPro.removeObsolete(line);

            if (line.contains("{title:") || line.contains("Title:")) {
                // Extract the title and empty the line (don't need to keep it)
                line = convertChoPro.removeTags(line, "{title:");
                line = convertChoPro.removeTags(line, "Title:");
                title = line.trim();
                line = "";

            } else if (line.contains("{artist:") || line.contains("Artist:") || line.contains("Author:")) {
                // Extract the author and empty the line (don't need to keep it)
                line = convertChoPro.removeTags(line, "{artist:");
                line = convertChoPro.removeTags(line, "Artist:");
                line = convertChoPro.removeTags(line, "Author:");
                author = line.trim();
                line = "";

            } else if (line.contains("{copyright:") || line.contains("Copyright:") || line.contains("Footer:")) {
                line = convertChoPro.removeTags(line, "{copyright:");
                line = convertChoPro.removeTags(line, "Copyright:");
                line = convertChoPro.removeTags(line, "Footer:");
                copyright = line.trim();
                line = "";

            } else if (line.contains("{subtitle:")) {
                // Extract the subtitles.  Add it back as a comment line
                String subtitle = convertChoPro.removeTags(line, "{subtitle:");
                if (author.equals("")) {
                    author = subtitle;
                }
                if (copyright.equals("")) {
                    copyright = subtitle;
                }
                line = ";" + subtitle;

            } else if (line.contains("{ccli:") || line.contains("CCLI:")) {
                // Extract the ccli (not really a chordpro tag, but works for songselect and worship together
                line = convertChoPro.removeTags(line, "{ccli:").trim();
                line = convertChoPro.removeTags(line, "CCLI:").trim();
                ccli = line.trim();
                line = "";

            } else if (line.contains("{key:") || line.contains("Key:")) {
                // Extract the key
                line = convertChoPro.removeTags(line, "{key:");
                line = convertChoPro.removeTags(line, "Key:");
                line = line.replace("[", "");
                line = line.replace("]", "");
                key = line.trim();
                line = "";

            } else if (line.contains("{capo:") || line.contains("Capo:")) {
                line = convertChoPro.removeTags(line, "{capo:");
                line = convertChoPro.removeTags(line, "Capo:");
                capo = line.trim();
                capoprint = "true";

            } else if (line.contains("{tempo:") || line.contains("Tempo:")) {
                line = convertChoPro.removeTags(line, "{tempo:");
                line = convertChoPro.removeTags(line, "Tempo:");
                tempo = line.trim();
                line = "";

            } else if (line.contains("{time:") || line.contains("Time:")) {
                // Extract the timesig
                line = convertChoPro.removeTags(line, "{time:");
                line = convertChoPro.removeTags(line, "Time:");
                time_sig = line.trim();
                line = "";

            } else if (line.contains("{duration:") || line.contains("Duration:")) {
                line = convertChoPro.removeTags(line, "{duration:");
                line = convertChoPro.removeTags(line, "Duration:");
                duration = line.trim();
                line = "";

            } else if (line.contains("{number:") || line.contains("Number:")) {
                line = convertChoPro.removeTags(line, "{number:");
                line = convertChoPro.removeTags(line, "Number:");
                number = line.trim();
                line = "";

            } else if (line.contains("{flow:") || line.contains("Flow:")) {
                line = convertChoPro.removeTags(line, "{flow:");
                line = convertChoPro.removeTags(line, "Flow:");
                flow = line.trim();
                line = "";

            } else if (line.contains("{keywords:") || line.contains("Keywords:") || line.contains("Topic:")) {
                line = convertChoPro.removeTags(line, "{keywords:");
                line = convertChoPro.removeTags(line, "Keywords:");
                line = convertChoPro.removeTags(line, "Topic:");
                theme = line.trim();
                line = "";

            } else if (line.contains("{midi:") || line.contains("MIDI:")) {
                line = convertChoPro.removeTags(line, "{midi:");
                line = convertChoPro.removeTags(line, "MIDI:");
                midi = line.trim();
                line = "";

            } else if (line.contains("{midi-index:") || line.contains("MIDI-Index:")) {
                line = convertChoPro.removeTags(line, "{midi-index:");
                line = convertChoPro.removeTags(line, "MIDI-Index:");
                midiindex = line.trim();
                line = "";

            } else if (line.startsWith("#")) {
                // Change lines that start with # into comment lines
                line = line.replaceFirst("#", ";");

            } else if (line.contains("{comments:") || line.contains("{comment:")) {
                // Change comment lines
                line = ";" + convertChoPro.removeTags(line, "{comments:").trim();
                line = ";" + convertChoPro.removeTags(line, "{comment:").trim();

            }

            // Fix guitar tab so it fits OpenSongApp formatting ;e |
            line = convertChoPro.tryToFixTabLine(line);

            if (line.startsWith(";;")) {
                line = line.replace(";;", ";");
            }

            // Now split lines with chords in them into two lines of chords then lyrics
            line = convertChoPro.extractChordLines(line);

            line = line.trim() + "\n";
            parsedLines.append(line);

        }
        return parsedLines.toString();
    }

    private Song setCorrectXMLValues(Song song) {
        if (title == null || title.isEmpty()) {
            song.setTitle(newSongFileName);
        } else {
            song.setTitle(title.trim());
        }
        song.setAuthor(author.trim());
        song.setCopyright(copyright.trim());
        song.setMetronomebpm(tempo.trim());
        song.setTimesig(time_sig.trim());
        song.setCcli(ccli.trim());
        song.setKey(key.trim());
        song.setLyrics(lyrics.trim());
        song.setCapo(capo.trim());
        song.setCapoprint(capoprint.trim());
        song.setMidi(midi.trim());
        song.setMidiindex(midiindex.trim());
        song.setAutoscrolllength(duration.trim());
        song.setPresentationorder(flow.trim());
        song.setHymnnum(number.trim());
        song.setTheme(theme.trim());

        return song;
    }

}
