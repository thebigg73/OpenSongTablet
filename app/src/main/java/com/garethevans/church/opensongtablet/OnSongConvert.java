package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
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
                                        SongXML songXML, ChordProConvert chordProConvert, Uri uri, String l, int pos) {

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
        songSubFolder = chordProConvert.getSongFolderLocation(storageAccess, uri);

        // Prepare the new song filename
        newSongFileName = chordProConvert.getNewSongFileName(uri, title);

        // Initialise the variables
        songXML.initialiseSongTags();

        // Set the correct values
        setCorrectXMLValues();

        // Now prepare the new songXML file
        FullscreenActivity.myXML = songXML.getXML();

        // Get a unique uri for the new song
        Uri newUri = chordProConvert.getNewSongUri(c, storageAccess, preferences, songSubFolder, newSongFileName);

        Log.d("ChordProConvert", "newUri=" + newUri);

        // Now write the modified song
        chordProConvert.writeTheImprovedSong(c, storageAccess, preferences, oldSongFileName, newSongFileName,
                songSubFolder, newUri, uri, pos);

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
        FullscreenActivity.mTitle = title.trim();
        FullscreenActivity.mAuthor = author.trim();
        FullscreenActivity.mCopyright = copyright.trim();
        FullscreenActivity.mTempo = tempo.trim();
        FullscreenActivity.mTimeSig = time_sig.trim();
        FullscreenActivity.mCCLI = ccli.trim();
        FullscreenActivity.mKey = key.trim();
        FullscreenActivity.mLyrics = lyrics.trim();
        FullscreenActivity.mCapo = capo.trim();
        FullscreenActivity.mCapoPrint = capoprint.trim();
        FullscreenActivity.mMidi = midi.trim();
        FullscreenActivity.mMidiIndex = midiindex.trim();
        FullscreenActivity.mDuration = duration.trim();
        FullscreenActivity.mPresentation = flow.trim();
        FullscreenActivity.mHymnNumber = number.trim();
        FullscreenActivity.mPitch = pitch.trim();
        FullscreenActivity.mRestrictions = restrictions.trim();
        FullscreenActivity.mBooks = book.trim();
        FullscreenActivity.mTheme = theme.trim();
    }


    //TODO
    // All old  stuff below I think

    public interface MyInterface {
        void prepareSongMenu();
        void showToastMessage(String message);
    }

    private static MyInterface mListener;

    static String message = "";
    private static boolean isbatch = false;

    boolean doExtract(Context c, Preferences preferences) {

		// This is called when a OnSong format song has been loaded.
		// This tries to extract the relevant stuff and reformat the
		// <lyrics>...</lyrics>
		String temp = FullscreenActivity.myXML;
		StringBuilder parsedlines;
		
        // Initialise the variables
        SongXML songXML = new SongXML();
        songXML.initialiseSongTags();
        
        // Break the temp variable into an array split by line
		// Check line endings are \n
        //temp = temp.replaceAll("[\\x0-\\x9]", "");
        //temp = temp.replace("\\x0","");
        temp = temp.replace("\0", "");
        while (temp.contains("\r\n") || temp.contains("\r") || temp.contains("\n\n\n")) {
            temp = temp.replace("\r\n","\n");
            temp = temp.replace("\r", "\n");
            temp = temp.replace("\n\n\n", "\n\n");
        }
        temp = temp.replace("\t", "    ");
		temp = temp.replace("\'", "'");
		temp = temp.replace("&quot;", "\"");
		temp = temp.replace("\\'", "'");
		temp = temp.replace("&quot;", "\"");
		String[] line = temp.split("\n");
		int numlines = line.length;

        //Go through the lines and get rid of rubbish
        for (int y=0;y<numlines;y++) {
			line[y] = line[y].replace("&#39;","'");
            line[y] = line[y].replace("&#145","'");
            line[y] = line[y].replace("&#146;","'");
            line[y] = line[y].replace("&#147;","'");
            line[y] = line[y].replace("&#148;","'");
            line[y] = line[y].replace("тАЩ","'");
            line[y] = line[y].replace("\u0027","'");
            line[y] = line[y].replace("\u0028","'");
            line[y] = line[y].replace("\u0029","'");
            line[y] = line[y].replace("\u0211", "'");
            line[y] = line[y].replace("\u0212", "'");
            line[y] = line[y].replace("\u0213", "'");
            line[y] = line[y].replace("\u00D5", "'");
            line[y] = line[y].replace("\u0442\u0410\u0429", "'");
            line[y] = line[y].replace("\u0429", "");
            line[y] = line[y].replace("\u0410", "");
            line[y] = line[y].replace("\u0429", "'");
            line[y] = line[y].replace("\u0060", "'");
            line[y] = line[y].replace("\u00B4", "'");
            line[y] = line[y].replace("\u2018", "'");
            line[y] = line[y].replace("\u2019", "'");
            //line[y] = line[y].replaceAll("[^\\x20-\\x7e]", "");
        }

		// Extract the metadata
		// This is all the lines before the first blank line
		int metadatastart = 0;
		int metadataend = 0;
		
		for (int z = metadatastart; z < numlines; z++) {
			if (line[z].isEmpty()) {
				// This is the end of the metadata
				metadataend = z;
				break;
			}
		}

		// Go through all lines and homogenise all tags - Make tag lines common
		for (int x = 0; x < numlines; x++) {

			// Meta data tags at the top
			line[x] = line[x].replace("{ns", "{new_song");
			line[x] = line[x].replace("{title :", "{title:");
			line[x] = line[x].replace("{Title:", "{title:");
			line[x] = line[x].replace("{t:", "{title:");
			line[x] = line[x].replace("{t :", "{title:");
			line[x] = line[x].replace("{T:", "{title:");
			line[x] = line[x].replace("{subtitle :", "{artist:");
			line[x] = line[x].replace("{Subtitle:", "{artist:");
			line[x] = line[x].replace("{St:", "{artist:");
			line[x] = line[x].replace("{st:", "{artist:");
			line[x] = line[x].replace("{st :", "{artist:");
			line[x] = line[x].replace("{su:", "{artist:");
			line[x] = line[x].replace("{su :", "{artist:");
			line[x] = line[x].replace("{artist :", "{artist:");
			line[x] = line[x].replace("{a:", "{artist:");
			line[x] = line[x].replace("{author :", "{author:");
			line[x] = line[x].replace("{copyright :", "{copyright:");
			line[x] = line[x].replace("{footer:", "{copyright:");
			line[x] = line[x].replace("{footer :", "{copyright:");
			line[x] = line[x].replace("{key :", "{key:");
			line[x] = line[x].replace("{k:", "{key:");
			line[x] = line[x].replace("{k :", "{key:");
			line[x] = line[x].replace("{capo :", "{capo:");
			line[x] = line[x].replace("{time :", "{time:");
			line[x] = line[x].replace("{tempo :", "{tempo:");
			line[x] = line[x].replace("{duration :", "{duration:");
			line[x] = line[x].replace("{number :", "{number:");
			line[x] = line[x].replace("{flow :", "{flow:");
			line[x] = line[x].replace("{ccli :", "{ccli:");
			line[x] = line[x].replace("{keywords :", "{keywords:");
			line[x] = line[x].replace("{topic:", "{keywords:");
			line[x] = line[x].replace("{topic :", "{keywords:");
			line[x] = line[x].replace("{book :", "{book:");
			line[x] = line[x].replace("{midi :", "{midi:");
			line[x] = line[x].replace("{midi-index :", "{midi-index:");
			line[x] = line[x].replace("{pitch :", "{pitch:");
			line[x] = line[x].replace("{restrictions :", "{restrictions:");

			// In line
			line[x] = line[x].replace("{comments :", "{comments:");
			line[x] = line[x].replace("{c:", "{comments:");
			line[x] = line[x].replace("{c :", "{comments:");
			line[x] = line[x].replace("{guitar_comment :", "{guitar_comment:");
			line[x] = line[x].replace("{gc:", "{guitar_comment:");
			line[x] = line[x].replace("{gc :", "{guitar_comment:");
			line[x] = line[x].replace("{sot", "{start_of_tab");
			line[x] = line[x].replace("{sob", "{start_of_bridge");
			line[x] = line[x].replace("{eot", "{end_of_tab");
			line[x] = line[x].replace("{eob", "{end_of_bridge");
			line[x] = line[x].replace("{soc", "{start_of_chorus");
			line[x] = line[x].replace("{eoc", "{end_of_chorus");
			line[x] = line[x].replace("{comment_italic :", "{comments:");
			line[x] = line[x].replace("{ci:", "{comments:");
			line[x] = line[x].replace("{ci :", "{comments:");
			line[x] = line[x].replace("{textfont :", "{textfont:");
			line[x] = line[x].replace("{textsize :", "{textsize:");
			line[x] = line[x].replace("{chordfont :", "{chordfont:");
			line[x] = line[x].replace("{chordsize :", "{chordsize:");
			line[x] = line[x].replace("{ng", "{nogrid");
			line[x] = line[x].replace("{g}", "{grid}");
			line[x] = line[x].replace("{d:", "{define:");
			line[x] = line[x].replace("{titles :", "{titles:");
			line[x] = line[x].replace("{npp", "{new_physical_page");
			line[x] = line[x].replace("{np", "{new_page");
			line[x] = line[x].replace("{columns :", "{columns:");
			line[x] = line[x].replace("{col:", "{columns:");
			line[x] = line[x].replace("{col :", "{columns:");
			line[x] = line[x].replace("{column_break :", "{column_break:");
			line[x] = line[x].replace("{colb:", "{column_break:");
			line[x] = line[x].replace("{colb :", "{column_break:");
			line[x] = line[x].replace("{pagetype :", "{pagetype:");
		}

		// Go through the metadata lines and try to extract any stuff we can
		for (int x = metadatastart; x < metadataend; x++) {

			if (line[x].indexOf("{title:")==0) {
				// extract the title
				line[x] = line[x].replace("{title:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTitle = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Title:")==0) {
				// extract the title
				line[x] = line[x].replace("Title:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTitle = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{artist:")==0) {
				// extract the artist
				line[x] = line[x].replace("{artist:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mAuthor = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Artist:")==0) {
				// extract the artist
				line[x] = line[x].replace("Artist:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mAuthor = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{author:")==0) {
				// extract the author (of chart)
				line[x] = line[x].replace("{author:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mUser1 = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Author:")==0) {
				// extract the author (of chart)
				line[x] = line[x].replace("Author:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mUser1 = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{copyright:")==0) {
				// extract the copyright
				line[x] = line[x].replace("{copyright:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCopyright = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Copyright:")==0 || line[x].indexOf("Footer:")==0) {
				// extract the copyright info
				line[x] = line[x].replace("Copyright:", "");
				line[x] = line[x].replace("Footer:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCopyright = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{key:")==0) {
				// extract the key
				line[x] = line[x].replace("{key:", "");
				line[x] = line[x].replace("}", "");
                line[x] = line[x].replace("[", "");
                line[x] = line[x].replace("]", "");
				line[x] = line[x].trim();
				FullscreenActivity.mKey = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Key:")==0) {
				line[x] = line[x].replace("Key:", "");
                line[x] = line[x].replace("[", "");
                line[x] = line[x].replace("]", "");
				line[x] = line[x].trim();
				FullscreenActivity.mKey = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{capo:")==0) {
				// extract the capo
				line[x] = line[x].replace("{capo:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCapo = line[x];
				FullscreenActivity.mCapoPrint = "true";
				line[x] = "";
			}
			if (line[x].indexOf("Capo:")==0) {
				// extract the capo fret
				line[x] = line[x].replace("Capo:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCapo = line[x];
				FullscreenActivity.mCapoPrint = "true";
				line[x] = "";
			}


			if (line[x].indexOf("{time:")==0) {
				// extract the time signature
				line[x] = line[x].replace("{time:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTimeSig = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Time:")==0) {
				line[x] = line[x].replace("Time:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTimeSig = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{tempo:")==0) {
				// extract the tempo
				line[x] = line[x].replace("{tempo:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTempo = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Tempo:")==0) {
				line[x] = line[x].replace("Tempo:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTempo = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{duration:")==0) {
				// extract the duration
				line[x] = line[x].replace("{duration:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mDuration = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Duration:")==0) {
				line[x] = line[x].replace("Duration:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mDuration = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{number:")==0) {
				// extract the Hymn number
				line[x] = line[x].replace("{number:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mHymnNumber = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Number:")==0) {
				line[x] = line[x].replace("Number:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mHymnNumber = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{flow:")==0) {
				// extract the Presentation sequence
				line[x] = line[x].replace("{flow:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mPresentation = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Flow:")==0) {
				line[x] = line[x].replace("Flow:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mPresentation = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{ccli:")==0) {
				// extract the CCLI
				line[x] = line[x].replace("{ccli:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCCLI = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("CCLI:")==0) {
				line[x] = line[x].replace("CCLI:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCCLI = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{keywords:")==0) {
				// extract the Themes
				line[x] = line[x].replace("{keywords:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTheme = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Keywords:")==0 || line[x].indexOf("Topic:")==0) {
				line[x] = line[x].replace("Keywords:", "");
				line[x] = line[x].replace("Topic:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTheme = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{book:")==0) {
				// extract the books
				line[x] = line[x].replace("{book:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mBooks = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Book:")==0) {
				line[x] = line[x].replace("Book:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mBooks = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{midi:")==0) {
				// extract the midi
				line[x] = line[x].replace("{midi:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mMidi = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("MIDI:")==0) {
				line[x] = line[x].replace("MIDI:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mMidi = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{midi-index:")==0) {
				// extract the midi index
				line[x] = line[x].replace("{midi-index:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mMidiIndex = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("MIDI-Index:")==0) {
				line[x] = line[x].replace("MIDI-Index:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mMidiIndex = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{pitch:")==0) {
				// extract the Pitch
				line[x] = line[x].replace("{pitch:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mPitch = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Pitch:")==0) {
				line[x] = line[x].replace("Pitch:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mPitch = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{restrictions:")==0) {
				// extract the restrictions
				line[x] = line[x].replace("{restrictions:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mRestrictions = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Restrictions:")==0) {
				line[x] = line[x].replace("Restrictions:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mRestrictions = line[x];
				line[x] = "";
			}
		}

        //First line is the title
        if (!line[0].isEmpty()) {
            FullscreenActivity.mTitle = line[0];
            line[0] = "";
        }
        if (metadataend>=1) {
		// Second line is the author	
		if (!line[1].isEmpty()) {
			FullscreenActivity.mAuthor = line[1];
			line[1] = "";
		}
	}
	
	// Now look for the rest of the song stuff
	for (int x=0;x<numlines;x++) {
		// Change lines that start with # into comment lines
		if (line[x].indexOf("#") == 0) {
			line[x] = line[x].replaceFirst("#", ";");
			// Get rid of any extra whitespace
			line[x] = line[x].trim();			
		}
		
		// Change lines that start with backticks into chord line
		if (line[x].indexOf("`") == 0) {
			line[x] = line[x].replaceFirst("`", ".");
		}
		
		// If a line has more space than characters it is likely to be a chord line
		int getlengthofline = line[x].length();
		int linewithoutspaces = line[x].replace(" ", "").length();
		int amountofspace = getlengthofline - linewithoutspaces;
		if (amountofspace>linewithoutspaces && line[x].indexOf(".")!=0) {
			line[x] = "." + line[x];
		}

		// Change guitar comments into comment lines
		if (line[x].indexOf("{guitar_comment:") == 0) {
//			line[x] = line[x].replaceFirst("{guitar_comment:", ";");
			line[x] = line[x].replaceFirst("\\{guitar_comment:", ";");
			line[x] = line[x].replaceFirst("}", "");
			// Get rid of any extra whitespace
			line[x] = line[x].trim();			
		}

		// Change musical instructions into comments
		String line_t = line[x].trim();
		if (line_t.startsWith("(") && line_t.endsWith(")")) {
			line[x] = ";" + line_t.substring(1,line_t.length()-1 );
		}

		// Change comment lines
		if (line[x].contains("{comments:")) {
			line[x] = line[x].replace("{comments:","");
			line[x] = line[x].replace("}", "");
			line[x] = line[x].trim();
			line[x] = ";" + line[x].trim();
		}

		// Get rid of formatting tags we won't use
		if (line[x].contains("{textsize") || line[x].contains("{textfont")
				|| line[x].contains("{chordsize") || line[x].contains("{chordfont")) {
			line[x] = "";
		}
		
		if (line[x].indexOf("*")==0 || line[x].indexOf("/")==0 || line[x].indexOf("!")==0) {
			if (line[x].length()>1) {
				line[x] = line[x].substring(1);
			}
		}
		
		if (line[x].indexOf("&")==0 || line[x].indexOf(">")==0) {
			int endofformattingtag = line[x].indexOf(":");
			if (endofformattingtag==-1) {
				endofformattingtag = 0;
			}
			if ((endofformattingtag+1) <= line[x].length()) {
				line[x] = line[x].substring(endofformattingtag+1);
			}
		}

		// Remove < > tags
		line[x] = line[x].replace("<", "(");
		line[x] = line[x].replace(">", ")");
		

		// Get rid of any extra whitespace
		line[x] = line[x].trim();			
	}
	
		// Go through each line and try to fix chord lines
		for (int x = metadataend; x < numlines; x++) {
			line[x] = line[x].trim();
			StringBuilder tempchordline = new StringBuilder();

			// Look for [ and ] signifying a chord
			while (line[x].contains("[") && line[x].contains("]")) {

				// Find chord start and end pos
				int chordstart = line[x].indexOf("[");
				int chordend = line[x].indexOf("]");
				String chord;
				if (chordend > chordstart) {
					chord = line[x].substring(chordstart, chordend + 1);
					String substart = line[x].substring(0, chordstart);
					String subsend = line[x].substring(chordend + 1);
					line[x] = substart + subsend;
				} else {
					chord = "";
					line[x] = line[x].replace("[", "");
					line[x] = line[x].replace("]", "");
				}
				chord = chord.replace("[", "");
				chord = chord.replace("]", "");

				// Add the chord to the tempchordline
				if (tempchordline.length() > chordstart) {
					// We need to put the chord at the end stuff already there
					// Don't overwrite - This is because the length of the 
					// previous chord is bigger than the lyrics following it
					chordstart = tempchordline.length() + 1;
				}
				for (int z = tempchordline.length(); z < chordstart; z++) {
					tempchordline.append(" ");
				}
				// Now add the chord
				tempchordline.append(chord);

			}
			// All chords should be gone now, so remove any remaining [ and ]
			line[x] = line[x].replace("[", "");
			line[x] = line[x].replace("]", "");
			if (tempchordline.length() > 0) {
				line[x] = "." + tempchordline + "\n" + line[x];
			}
        }

		// Join the individual lines back up
		parsedlines = new StringBuilder();
		for (int x = 0; x < numlines; x++) {
			// Try to guess tags used
			if (line[x].indexOf(";")!=0) {
				line[x] = line[x].replace(" Intro:", "[Intro]");
				line[x] = line[x].replace(" Outro:", "[Outro]");
				line[x] = line[x].replace(" V:", "[V]");
				line[x] = line[x].replace(" V1:", "[V1]");
				line[x] = line[x].replace(" V2:", "[V2]");
				line[x] = line[x].replace(" V3:", "[V3]");
				line[x] = line[x].replace(" V4:", "[V4]");
				line[x] = line[x].replace(" V5:", "[V5]");
				line[x] = line[x].replace(" V6:", "[V6]");
				line[x] = line[x].replace(" V7:", "[V7]");
				line[x] = line[x].replace(" V8:", "[V8]");
				line[x] = line[x].replace(" V9:", "[V9]");
				line[x] = line[x].replace(" Verse:", "[V]");
				line[x] = line[x].replace(" Verse 1:", "[V1]");
				line[x] = line[x].replace(" Verse 2:", "[V2]");
				line[x] = line[x].replace(" Verse 3:", "[V3]");
				line[x] = line[x].replace(" Verse 4:", "[V4]");
				line[x] = line[x].replace(" (Verse)", "[V]");
				line[x] = line[x].replace(" (Verse 1)", "[V1]");
				line[x] = line[x].replace(" (Verse 2)", "[V2]");
				line[x] = line[x].replace(" (Verse 3)", "[V3]");
				line[x] = line[x].replace(" (Chorus)", "[y]");
                line[x] = line[x].replace(" Chorus", "[y]");
				line[x] = line[x].replace(" C:", "[y]");
				line[x] = line[x].replace(" C1:", "[C1]");
				line[x] = line[x].replace(" C2:", "[C2]");
				line[x] = line[x].replace(" C3:", "[C3]");
				line[x] = line[x].replace(" C4:", "[C4]");
				line[x] = line[x].replace(" C5:", "[C5]");
				line[x] = line[x].replace(" C6:", "[C6]");
				line[x] = line[x].replace(" C7:", "[C7]");
				line[x] = line[x].replace(" C8:", "[C8]");
				line[x] = line[x].replace(" C9:", "[C9]");
				line[x] = line[x].replace(" Chorus:", "[y]");
				line[x] = line[x].replace(" Chorus 1:", "[C1]");
				line[x] = line[x].replace(" Chorus 2:", "[C2]");
				line[x] = line[x].replace(" Chorus 3:", "[C3]");
				line[x] = line[x].replace(" Prechorus:", "[P]");
				line[x] = line[x].replace(" Prechorus 1:", "[P1]");
				line[x] = line[x].replace(" Prechorus 2:", "[P2]");
				line[x] = line[x].replace(" Prechorus 3:", "[P3]");
				line[x] = line[x].replace(" Bridge:", "[B]");
				line[x] = line[x].replace(" Tag:", "[T]");
				line[x] = line[x].replace("Intro:", "[Intro]");
				line[x] = line[x].replace("Outro:", "[Outro]");
				line[x] = line[x].replace("V:", "[V]");
				line[x] = line[x].replace("V1:", "[V1]");
				line[x] = line[x].replace("V2:", "[V2]");
				line[x] = line[x].replace("V3:", "[V3]");
				line[x] = line[x].replace("V4:", "[V4]");
				line[x] = line[x].replace("V5:", "[V5]");
				line[x] = line[x].replace("V6:", "[V6]");
				line[x] = line[x].replace("V7:", "[V7]");
				line[x] = line[x].replace("V8:", "[V8]");
				line[x] = line[x].replace("V9:", "[V9]");
				line[x] = line[x].replace("Verse:", "[V]");
				line[x] = line[x].replace("Verse 1:", "[V1]");
				line[x] = line[x].replace("Verse 2:", "[V2]");
				line[x] = line[x].replace("Verse 3:", "[V3]");
				line[x] = line[x].replace("Verse 4:", "[V4]");
				line[x] = line[x].replace("(Verse)", "[V]");
				line[x] = line[x].replace("(Verse 1)", "[V1]");
				line[x] = line[x].replace("(Verse 2)", "[V2]");
				line[x] = line[x].replace("(Verse 3)", "[V3]");				
				line[x] = line[x].replace("(Chorus)", "[y]");
				line[x] = line[x].replace("C:", "[y]");
				line[x] = line[x].replace("C1:", "[C1]");
				line[x] = line[x].replace("C2:", "[C2]");
				line[x] = line[x].replace("C3:", "[C3]");
				line[x] = line[x].replace("C4:", "[C4]");
				line[x] = line[x].replace("C5:", "[C5]");
				line[x] = line[x].replace("C6:", "[C6]");
				line[x] = line[x].replace("C7:", "[C7]");
				line[x] = line[x].replace("C8:", "[C8]");
				line[x] = line[x].replace("C9:", "[C9]");
				line[x] = line[x].replace("Chorus:", "[y]");
				line[x] = line[x].replace("Chorus 1:", "[C1]");
				line[x] = line[x].replace("Chorus 2:", "[C2]");
				line[x] = line[x].replace("Chorus 3:", "[C3]");
				line[x] = line[x].replace("Prechorus:", "[P]");
				line[x] = line[x].replace("Prechorus 1:", "[P1]");
				line[x] = line[x].replace("Prechorus 2:", "[P2]");
				line[x] = line[x].replace("Prechorus 3:", "[P3]");
				line[x] = line[x].replace("Bridge:", "[B]");
				line[x] = line[x].replace("Tag:", "[T]");

				// Guess custom tags - will be a short line with :
				if (line[x].length()<15 && line[x].contains(":") && line[x].indexOf("{")!=0) {
					line[x] = "[" + line[x].replace(":","") + "]";
				}
			}
			parsedlines.append(line[x]).append("\n");
		}

		
		// Remove start and end of tabs
		while (parsedlines.toString().contains("{start_of_tab") && parsedlines.toString().contains("{end_of_tab")) {
			int startoftabpos;
			int endoftabpos;
			startoftabpos = parsedlines.indexOf("{start_of_tab");
			endoftabpos = parsedlines.indexOf("{end_of_tab") + 12;
			
			if (endoftabpos > 13 && startoftabpos > -1 && endoftabpos > startoftabpos) {
				String startbit = parsedlines.substring(0, startoftabpos);
				String endbit = parsedlines.substring(endoftabpos);
				parsedlines = new StringBuilder(startbit + endbit);
			}
		}
		
		// Change start and end of chorus
		while (parsedlines.toString().contains("{start_of_chorus")) {
			parsedlines = new StringBuilder(parsedlines.toString().replace("{start_of_chorus}", "[y]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{start_of_chorus:}", "[y]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{start_of_chorus :}", "[y]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{start_of_chorus", "[y]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace(":", ""));
			parsedlines = new StringBuilder(parsedlines.toString().replace("}", ""));
		}

		while (parsedlines.toString().contains("{end_of_chorus")) {
			parsedlines = new StringBuilder(parsedlines.toString().replace("{end_of_chorus}", "[]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{end_of_chorus:}", "[]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{end_of_chorus :}", "[]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{end_of_chorus", "[]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace(":", ""));
			parsedlines = new StringBuilder(parsedlines.toString().replace("}", ""));
		}

		// Change start and end of bridge
		while (parsedlines.toString().contains("{start_of_bridge")) {
			parsedlines = new StringBuilder(parsedlines.toString().replace("{start_of_bridge}", "[B]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{start_of_bridge:}", "[B]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{start_of_bridge :}", "[B]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{start_of_bridge", "[B]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace(":", ""));
			parsedlines = new StringBuilder(parsedlines.toString().replace("}", ""));
		}

		while (parsedlines.toString().contains("{end_of_bridge")) {
			parsedlines = new StringBuilder(parsedlines.toString().replace("{end_of_bridge}", "[]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{end_of_bridge:}", "[]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{end_of_bridge :}", "[]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace("{end_of_bridge", "[]"));
			parsedlines = new StringBuilder(parsedlines.toString().replace(":", ""));
			parsedlines = new StringBuilder(parsedlines.toString().replace("}", ""));
		}
		
		// Get rid of double line breaks
		while (parsedlines.toString().contains("\n\n\n")) {
			parsedlines = new StringBuilder(parsedlines.toString().replace("\n\n\n", "\n\n"));
		}

		while (parsedlines.toString().contains(";\n\n;")) {
			parsedlines = new StringBuilder(parsedlines.toString().replace(";\n\n;", ";\n"));
		}

		// Ok, go back through the parsed lines and add spaces to the beginning
		// of lines that aren't comments, chords or tags
        if (!parsedlines.toString().contains("\n")) {
            parsedlines.append("\n");
        }

		String[] line2 = parsedlines.toString().split("\n");
		int numlines2 = line2.length;
        // Reset the parsed lines
		parsedlines = new StringBuilder();

		// Go through the lines one at a time
		// Add the fixed bit back together
		for (int x = 0; x < numlines2; x++) {
			if (line2[x].length() > 1) {
				if (line2[x].indexOf("[") != 0 && line2[x].indexOf(";") != 0
						&& line2[x].indexOf(".") != 0) {
					line2[x] = " " + line2[x];
				}
			}

			parsedlines.append(line2[x]).append("\n");
		}

		if (parsedlines.toString().equals("")) {
            parsedlines = new StringBuilder(FullscreenActivity.songfilename);
        }

        // Set the correct values
        FullscreenActivity.mLyrics = parsedlines.toString().trim();

        FullscreenActivity.myXML = songXML.getXML();

        // Change the name of the song to remove chordpro file extension
        String newSongTitle = FullscreenActivity.songfilename;

        // Decide if a better song title is in the file
        if (FullscreenActivity.mTitle!=null && FullscreenActivity.mTitle.length() > 0) {
            newSongTitle = FullscreenActivity.mTitle.toString();
        }

        newSongTitle = songXML.parseToHTMLEntities(newSongTitle);
        newSongTitle = newSongTitle.replace(".onsong", "");
        newSongTitle = newSongTitle.replace(".ONSONG", "");
        newSongTitle = newSongTitle.replace(".OnSong", "");
        
        // Now write the modified song
        StorageAccess storageAccess = new StorageAccess();
        String newfilename = newSongTitle.replace("/","_");   // incase filename has path separator
        Uri uri = storageAccess.getUriForItem(c, preferences, "Songs", FullscreenActivity.whichSongFolder, newfilename);

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null, "Songs", FullscreenActivity.whichSongFolder, newfilename);

        OutputStream outputStream = storageAccess.getOutputStream(c, uri);
        if (storageAccess.writeFileFromString(FullscreenActivity.myXML,outputStream)) {
            // Writing was successful, so delete the original
            Uri originalfile = storageAccess.getUriForItem(c, preferences, "Songs", FullscreenActivity.whichSongFolder, FullscreenActivity.songfilename);
            storageAccess.deleteFile(c,originalfile);
        }

        FullscreenActivity.songfilename = newSongTitle;

		FullscreenActivity.needtorefreshsongmenu = true;
        if (!isbatch) {

			// Rebuild the song list
            storageAccess.listSongs(c, preferences);
			ListSongFiles listSongFiles = new ListSongFiles();
            listSongFiles.songUrisInFolder(c, preferences);

            // Load the songs
            listSongFiles.getAllSongFiles(c, preferences, storageAccess);

            // Get the song indexes
            listSongFiles.getCurrentSongIndex();
            Preferences.savePreferences();
            // Prepare the app to fix the song menu with the new file
            FullscreenActivity.converting = true;
        }
        
		return true;
	}

	void doBatchConvert(Context contxt) {
		DoBatchConvert dobatch = new DoBatchConvert(contxt);
		dobatch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	@SuppressLint("StaticFieldLeak")
    private class DoBatchConvert extends AsyncTask<String, Void, String> {

		StorageAccess storageAccess;
        Preferences preferences;

        @SuppressLint("StaticFieldLeak")
        Context context;

        DoBatchConvert(Context c) {
            context = c;
            mListener = (MyInterface) c;
            storageAccess = new StorageAccess();
            preferences = new Preferences();
        }

        @Override
        public void onPreExecute() {
			try {
				if (mListener != null) {
					String mText = context.getResources().getString(R.string.processing) + " - " + context.getResources().getString(R.string.takeawhile);
					mListener.showToastMessage(mText);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
            isbatch = true;
        }

		@Override
		protected String doInBackground(String... strings) {
			try {
				// Go through each file in the OnSong folder that ends with .onsong and convert it
				StringBuilder sb = new StringBuilder();
				FullscreenActivity.whichSongFolder = "OnSong";
				// Check if the OnSongFolder exists
                storageAccess.createFile(context, preferences, DocumentsContract.Document.MIME_TYPE_DIR, "Songs", "OnSong", "");
                ArrayList<String> allfiles = storageAccess.listFilesInFolder(context, preferences, "Songs", "OnSong");
					for (String thisfile : allfiles) {
                        Uri uri = storageAccess.getUriForItem(context, preferences, "Songs", "OnSong", thisfile);
                        if (thisfile.endsWith(".onsong")) {
							FullscreenActivity.songfilename = thisfile;
							try {
							    InputStream inputStream = storageAccess.getInputStream(context, uri);
								FullscreenActivity.myXML = storageAccess.readTextFileToString(inputStream);

                                if (!doExtract(context, preferences)) {
                                    Log.d("OnSongConvert", "Problem extracting OnSong");
								}
							} catch (Exception e) {
								// file doesn't exist
								FullscreenActivity.myXML = "<title>ERROR</title>\n<author></author>\n<lyrics>"
										+ context.getResources().getString(R.string.songdoesntexist) + "\n\n" + "</lyrics>";
								FullscreenActivity.myLyrics = "ERROR!";
								e.printStackTrace();
								sb.append(thisfile).append(" - ").append(context.getResources().getString(R.string.error));
							}
						} else if (thisfile.endsWith(".sqlite3") || thisfile.endsWith(".preferences") ||
								thisfile.endsWith(".doc") || thisfile.endsWith(".docx")) {
							if (!storageAccess.deleteFile(context,uri)) {
								sb.append(thisfile).append(" - ").append(context.getResources().getString(R.string.deleteerror_start));
							}
						}
					}
					message = sb.toString();
					if (message.equals("")) {
						message = "OK";
					}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return message;
		}

		@Override
		public void onPostExecute(String s) {
			try {
				if (mListener != null) {
					String mText = context.getResources().getString(R.string.processing) + " - " + context.getResources().getString(R.string.success);
					mListener.showToastMessage(mText);
					mListener.prepareSongMenu();
				}
				Preferences.savePreferences();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}