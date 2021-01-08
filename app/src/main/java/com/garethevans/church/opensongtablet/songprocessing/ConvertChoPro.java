package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.io.OutputStream;
import java.util.ArrayList;

public class ConvertChoPro {
    // Declare the variables;
    private String title;
    private String author;
    private String key;
    private String copyright;
    private String ccli;
    private String tempo;
    private String time_sig;
    private String lyrics;
    private String oldSongFileName;
    private String newSongFileName;
    private String songSubFolder;
    private String[] lines;
    private StringBuilder parsedLines;

    public Song convertTextToTags(Context c, StorageAccess storageAccess, Preferences preferences,
                                               ProcessSong processSong, SQLiteHelper sqLiteHelper,
                                               CommonSQL commonSQL, Uri uri, Song song) {

        initialiseTheVariables();

        lyrics = song.getLyrics();

        // Fix line breaks and slashes
        lyrics = processSong.fixLineBreaksAndSlashes(lyrics);

        // Make tag lines common
        lyrics = makeTagsCommon(lyrics);

        // Fix content we recognise
        lyrics = fixRecognisedContent(lyrics);

        // Now that we have the basics in place, we will go back through the song and extract headings
        // We have to do this separately as [] were previously identifying chords, not tags.
        // Chords have now been extracted to chord lines
        lyrics = removeOtherTags(lyrics);

        // Get rid of multilple line breaks (max of 3 together)
        lyrics = getRidOfExtraLines(lyrics);

        // Add spaces to beginnings of lines that aren't comments, chords or tags
        lyrics = addSpacesToLines(lyrics);

        // Get the filename and subfolder (if any) that the original song was in by parsing the uri
        oldSongFileName = getOldSongFileName(uri);
        songSubFolder = getSongFolderLocation(storageAccess, uri, oldSongFileName);

        // Prepare the new song filename
        newSongFileName = getNewSongFileName(storageAccess, uri, title, processSong);

        // Set the correct values
        setCorrectXMLValues(song);

        // Now prepare the new songXML file
        String newXML = processSong.getXML(song);

        // Get a unique uri for the new song
        Uri newUri = getNewSongUri(c, storageAccess, preferences, songSubFolder, newSongFileName);

        // Now write the modified song
        writeTheImprovedSong(c, storageAccess, preferences, sqLiteHelper, commonSQL, song, oldSongFileName, newSongFileName,
                songSubFolder, newUri, uri, newXML);

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
        copyright = "";
        ccli = "";
        tempo = "";
        time_sig = "";
        oldSongFileName = "";
        newSongFileName = "";
        songSubFolder = "";
        lines = null;
        parsedLines = new StringBuilder();
    }

    String makeTagsCommon(String s) {
        s = s.replace("{ns", "{new_song");
        s = s.replace("{title :", "{title:");
        s = s.replace("{Title:", "{title:");
        s = s.replace("{t:", "{title:");
        s = s.replace("{t :", "{title:");
        s = s.replace("{T:", "{title:");
        s = s.replace("{subtitle :", "{subtitle:");
        s = s.replace("{Subtitle:", "{subtitle:");
        s = s.replace("{St:", "{subtitle:");
        s = s.replace("{st:", "{subtitle:");
        s = s.replace("{st :", "{subtitle:");
        s = s.replace("{su:", "{subtitle:");
        s = s.replace("{su :", "{subtitle:");
        s = s.replace("{comments :", "{comments:");
        s = s.replace("{c:", "{comments:");
        s = s.replace("{c :", "{comments:");
        s = s.replace("{sot", "{start_of_tab");
        s = s.replace("{sob", "{start_of_bridge");
        s = s.replace("{eot", "{end_of_tab");
        s = s.replace("{eob", "{end_of_bridge");
        s = s.replace("{soc", "{start_of_chorus");
        s = s.replace("{eoc", "{end_of_chorus");
        s = s.replace("{comment_italic :", "{comments:");
        s = s.replace("{ci:", "{comments:");
        s = s.replace("{ci :", "{comments:");
        s = s.replace("{textfont :", "{textfont:");
        s = s.replace("{textsize :", "{textsize:");
        s = s.replace("{chordfont :", "{chordfont:");
        s = s.replace("{chordsize :", "{chordsize:");
        s = s.replace("{ng", "{nogrid");
        s = s.replace("{g}", "{grid}");
        s = s.replace("{d:", "{define:");
        s = s.replace("{titles :", "{titles:");
        s = s.replace("{npp", "{new_physical_page");
        s = s.replace("{np", "{new_page");
        s = s.replace("{columns :", "{columns:");
        s = s.replace("{col:", "{columns:");
        s = s.replace("{col :", "{columns:");
        s = s.replace("{column_break :", "{column_break:");
        s = s.replace("{colb:", "{column_break:");
        s = s.replace("{colb :", "{column_break:");
        s = s.replace("{pagetype :", "{pagetype:");
        return s;
    }

    private String fixRecognisedContent(String l) {
        // Break the filecontents into lines
        lines = l.split("\n");

        // This will be the new lyrics lines
        parsedLines = new StringBuilder();
        for (String line : lines) {
            // Get rid of any extra whitespace
            line = line.trim();

            // IV - SongSelect chordpro files end with a CCLI info block. The english start is 'CCLI Song # ....'.
            // IV - It is localised with most starting 'CCLI ' (following space is important as onsong files may have CCLI:)
            // IV - When we find an info block, stop adding lines as we are at the end of the lyrics
            if (line.startsWith("CCLI ") || line.startsWith("Número de la canción CCLI") || line.startsWith("Música CCLI")) {break;}

            // Remove directive lines we don't need
            line = removeObsolete(line);

            if (line.contains("{title:")) {
                // Extract the title and empty the line (don't need to keep it)
                title = removeTags(line, "{title:");
                line = "";

            } else if (line.contains("{artist:")) {
                // Extract the author and empty the line (don't need to keep it)
                author = removeTags(line, "{artist:");
                line = "";

            } else if (line.contains("{copyright:")) {
                // Extract the copyright and empty the line (don't need to keep it)
                // IV - Replace use of " |" or ";" as copyright holder separator with ,
                copyright = removeTags(line, "{copyright:").replace(" |",",").replace(";",",");
                line = "";

            } else if (line.contains("{subtitle:")) {
                // Extract the subtitles.  Add it back as a comment line
                String subtitle = removeTags(line, "{subtitle:");
                if (author.equals("")) {
                    author = subtitle;
                }
                if (copyright.equals("")) {
                    copyright = subtitle;
                }
                line = ";" + subtitle;

            } else if (line.contains("{ccli:")) {
                // Extract the ccli (not really a chordpro tag, but works for songselect and worship together
                ccli = removeTags(line, "{ccli:");
                line = "";

            } else if (line.contains("{key:")) {
                // Extract the key
                key = removeTags(line, "{key:");
                line = "";

            } else if (line.contains("{tempo:")) {
                // Extract the tempo
                tempo = removeTags(line, "{tempo:");
                line = "";

            } else if (line.contains("{time:")) {
                // Extract the timesig
                time_sig = removeTags(line, "{time:");
                line = "";

            } else if (line.startsWith("#")) {
                // Change lines that start with # into comment lines
                line = line.replaceFirst("#", ";");

            } else if (line.contains("{comments:") || line.contains("{comment:")) {
                // Change comment lines
                line = ";" + removeTags(line, "{comments:");
                line = ";" + removeTags(line, "{comment:");
            }

            // Get rid of GuitarTapp text
            if (line.contains("GuitarTapp")) {
                line = getRidOfGuitarTapp(line);
            }

            // Fix guitar tab so it fits OpenSongApp formatting ;e |
            line = tryToFixTabLine(line);

            if (line.startsWith(";;")) {
                line = line.replace(";;", ";");
            }

            // Now split lines with chords in them into two lines of chords then lyrics
            line = extractChordLines(line);

            parsedLines.append(line).append("\n");
        }
        return parsedLines.toString();
    }

    String removeObsolete(String s) {
        // IV - Added removal of SongSelect license and footer tag
        if (s.contains("{new_song")
                || s.contains("{ccli_license")
                || s.contains("{footer")
                || s.contains("{inline")
                || s.contains("{define")
                || s.contains("{textfont")
                || s.contains("{textsize")
                || s.contains("{chordfont")
                || s.contains("{chordsize")
                || s.contains("{nogrid")
                || s.contains("{grid")
                || s.contains("{titles")
                || s.contains("{new_physical_page")
                || s.contains("{new_page")
                || s.contains("{columns")
                || s.contains("{column_break")
                || s.contains("{pagetype")) {
            s = "";
        }
        if (s.startsWith("&") && s.contains(":") && s.indexOf(":") < 10) {
            // Remove colour tags (mostly onsong stuff)
            while (s.contains("&") && s.contains(":") && s.indexOf("&") < s.indexOf(":")) {
                String bittoremove = s.substring(s.indexOf("&"), s.indexOf(":") + 1);
                s = s.replace(bittoremove, "");
            }
        }
        s = s.replace(">gray:", "");

        return s;
    }

    String removeTags(String s, String tagstart) {
        s = s.replace(tagstart, "");
        s = s.replace("}", "");
        s = s.trim();
        return s;
    }

    String tryToFixTabLine(String l) {
        if (l.startsWith("e|") && l.contains("--")) {
            l = l.replace("e|", ";e |");
        } else if (l.startsWith("B|") && l.contains("--")) {
            l = l.replace("B|", ";B |");
        } else if (l.startsWith("G|") && l.contains("--")) {
            l = l.replace("G|", ";G |");
        } else if (l.startsWith("D|") && l.contains("--")) {
            l = l.replace("D|", ";D |");
        } else if (l.startsWith("A|") && l.contains("--")) {
            l = l.replace("A|", ";A |");
        } else if (l.startsWith("E|") && l.contains("--")) {
            l = l.replace("E|", ";E |");
        }
        return l;
    }

    String extractChordLines(String s) {
        StringBuilder tempchordline = new StringBuilder();
        if (!s.startsWith("#") && !s.startsWith(";")) {
            // IV - Add a leading space - the effect is to fix a chord mis-alignment
            s = " " + s;
            // Look for [ and ] signifying a chord
            while (s.contains("[") && s.contains("]")) {
                // Find chord start and end pos
                int chordstart = s.indexOf("[");
                int chordend = s.indexOf("]");
                String chord;
                if (chordend > chordstart) {
                    chord = s.substring(chordstart, chordend + 1);
                    String substart = s.substring(0, chordstart);
                    String subsend = s.substring(chordend + 1);
                    s = substart + subsend;
                } else {
                    chord = "";
                    s = s.replace("[", "");
                    s = s.replace("]", "");
                }
                chord = chord.replace("[", "");
                chord = chord.replace("]", "");

                // Add the chord to the tempchordline
                if (tempchordline.length() > chordstart) {
                    // We need to put the chord at the end stuff already there
                    // Don't overwrite - This is because the length of the
                    // previous chord is bigger than the lyrics following it
                    chordstart = tempchordline.length();
                }
                for (int z = tempchordline.length(); z < (chordstart-1); z++) {
                    tempchordline.append(" ");
                }
                // Now add the chord
                tempchordline.append(chord);
            }
            // All chords should be gone now, so remove any remaining [ and ]
            s = s.replace("[", "");
            s = s.replace("]", "");
            // IV - fix for missing space removed as now handled before loop
            if (tempchordline.length() > 0) {
                s = "." + tempchordline + "\n" + s;
            }
        }
        return s;
    }

    String removeOtherTags(String l) {
        // Break it apart again
        lines = l.split("\n");
        parsedLines = new StringBuilder();

        for (String line : lines) {
            // Try to guess tags used
            if (!line.equals("")) {
                line = guessTags(line);
            }

            //Removes comment tags in front of heading identifier [
            line = fixHeadings(line);

            // Remove inline
            line = removeInline(line);

            // Remove {start_of and {end_of tags for choruses, verses, bridges, etc.
            line = removeStartOfEndOfTags(line);

            parsedLines.append(line).append("\n");
        }

        return parsedLines.toString();
    }

    String getRidOfExtraLines(String s) {
        // Get rid of double/triple line breaks
        // Fix spaces between line breaks
        s = s.replace("; ", ";");
        s = s.replace(";\n", "\n");
        s = s.replace("\n \n", "\n\n");

        while (s.contains("\n\n\n")) {
            s = s.replace("\n\n\n", "\n\n");
        }

        while (s.contains(";\n\n;")) {
            s = s.replace(";\n\n;", ";\n");
        }

        while (s.contains("]\n[]\n")) {
            s = s.replace("]\n[]\n", "]\n");
        }
        while (s.contains("]\n\n[]\n")) {
            s = s.replace("]\n\n[]\n", "]\n");
        }

        while (s.contains("[]\n[")) {
            s = s.replace("[]\n[", "[");
        }

        while (s.contains("[]\n\n[")) {
            s = s.replace("[]\n\n[", "\n[");
        }

        // IV - Trim and remove any trailing [] empty section
        s = s.trim().replaceAll("\n\\Q[]\\E$","");

        return s;
    }

    String addSpacesToLines(String s) {
        lines = s.split("\n");

        // Reset the parsed lines
        parsedLines = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (!line.startsWith("[") && !line.startsWith(".") && !line.startsWith(";") && !s.equals("")) {
                // Must be a lyric line, so add a space
                line = " " + line;
            }
            line = line + "\n";
            parsedLines.append(line);
        }

        return parsedLines.toString();
    }

    Uri getNewSongUri(Context c, StorageAccess storageAccess, Preferences preferences, String songSubFolder, String nsf) {
        // Prepare a new uri based on the best filename, but make it unique so as not to overwrite existing files
        newSongFileName = nsf;
        Uri n = storageAccess.getUriForItem(c, preferences, "Songs", songSubFolder, newSongFileName);
        int attempts = 0;
        while (storageAccess.uriExists(c, n) && attempts < 4) {
            // Append _ to the end of the name until the filename is unique, or give up after 5 attempts
            newSongFileName = newSongFileName + "_";
            n = storageAccess.getUriForItem(c, preferences, "Songs", songSubFolder, newSongFileName);
            attempts = attempts + 1;
            Log.d("d", "attempt:" + attempts + " newSongFileName=" + newSongFileName);
        }
        return n;
    }

    String getOldSongFileName(Uri uri) {
        String fn = "_";
        if (uri != null && uri.getLastPathSegment() != null) {
            fn = uri.getLastPathSegment();
            // Since this is likely to be from a treeUri, look again (last bit after the final /)
            if (fn.contains("/")) {
                fn = fn.substring(fn.lastIndexOf("/"));
                fn = fn.replace("/", "");
            }
        }
        return fn;
    }

    String getNewSongFileName(StorageAccess storageAccess, Uri uri, String title, ProcessSong processSong) {
        String fn = uri.getLastPathSegment();
        if (fn == null) {
            fn = "";
        }
        // Since the file is likely a treeUri, get the last bit again
        if (fn.contains("/")) {
            fn = fn.substring(fn.lastIndexOf("/") + 1);
        }
        if (title != null && !title.equals("")) {
            fn = title;
        } else {
            // No title found, so use the filename and get rid of extensions
            fn = fn.replace(".pro", "");
            fn = fn.replace(".PRO", "");
            fn = fn.replace(".chopro", "");
            fn = fn.replace(".chordpro", "");
            fn = fn.replace(".CHOPRO", "");
            fn = fn.replace(".CHORDPRO", "");
            fn = fn.replace(".cho", "");
            fn = fn.replace(".CHO", "");
            fn = fn.replace(".crd", "");
            fn = fn.replace(".CRD", "");
            fn = fn.replace(".txt", "");
            fn = fn.replace(".TXT", "");
            fn = fn.replace(".onsong", "");
            fn = fn.replace(".ONSONG", "");
            fn = fn.replace(".usr", "");
            fn = fn.replace(".US", "");
        }
        fn = processSong.fixLineBreaksAndSlashes(fn);
        fn = storageAccess.safeFilename(fn);
        return fn;
    }

    String getSongFolderLocation(StorageAccess storageAccess, Uri uri, String oldSongFileName) {
        String sf = storageAccess.getPartOfUri(uri);
        sf = sf.replace("OpenSong/Songs/", "");
        sf = sf.replace(oldSongFileName, "");
        sf = sf.replace("//", "/");
        if (sf.startsWith("/")) {
            sf = sf.replaceFirst("/", "");
        }
        if (sf.endsWith("/")) {
            sf = sf.substring(0, sf.lastIndexOf("/"));
        }
        return sf;
    }

    void writeTheImprovedSong(Context c, StorageAccess storageAccess, Preferences preferences,
                              SQLiteHelper sqLiteHelper, CommonSQL commonSQL, Song song, String oldSongFileName, String nsf,
                              String songSubFolder, Uri newUri, Uri oldUri, String newXML) {

        newSongFileName = nsf;
        // Only do this for songs that exist!
        Log.d("ChordProConvert","oldSongFileName="+oldSongFileName);
        Log.d("ChordProConvert","newSongFileName="+newSongFileName);
        Log.d("ChordProConvert","oldUri="+oldUri);
        Log.d("ChordProConvert","newUri="+newUri);
        Log.d("ChordProConvert","storageAccess.uriExists(c, oldUri)="+storageAccess.uriExists(c, oldUri));

        if (oldSongFileName != null && !oldSongFileName.equals("") && newSongFileName != null && !newSongFileName.equals("")
                && oldUri != null && newUri != null && storageAccess.uriExists(c, oldUri)) {
            storageAccess.lollipopCreateFileForOutputStream(c,preferences,newUri,null,"Songs",songSubFolder,newSongFileName);
            OutputStream outputStream = storageAccess.getOutputStream(c, newUri);

            Log.d("ChordProConvert","outputStream="+outputStream);

            if (outputStream != null) {
                // Change the songId (references to the uri)
                // Now remove the old chordpro file
                storageAccess.writeFileFromString(newXML, outputStream);
                Log.d("ChordProConvert","attempt to deletefile="+storageAccess.deleteFile(c, oldUri));

                // Remove old song from database
                sqLiteHelper.deleteSong(c,commonSQL,songSubFolder,oldSongFileName);
            }

            // Update the song filename
            song.setFilename(newSongFileName);
            preferences.setMyPreferenceString(c,"songfilename",newSongFileName);

            // Now change the database references
            if (songSubFolder==null || songSubFolder.isEmpty()) {
                songSubFolder = c.getString(R.string.mainfoldername);
            }

            if (!sqLiteHelper.songExists(c,commonSQL,songSubFolder,newSongFileName)) {
                sqLiteHelper.createSong(c,storageAccess,commonSQL,songSubFolder,newSongFileName);
            }

            // Write the song object (full details) back to the database;
            sqLiteHelper.updateSong(c,commonSQL,song);
        }
    }

    private void setCorrectXMLValues(Song song) {
        if (title==null || title.isEmpty()) {
            title = newSongFileName;
        }
        song.setTitle(title.trim());
        song.setAuthor(author.trim());
        song.setCopyright(copyright.trim());
        song.setMetronomebpm(tempo.trim());
        song.setTimesig(time_sig.trim());
        song.setCcli(ccli.trim());
        song.setKey(key.trim());
        song.setLyrics(lyrics.trim());
    }

    private String getRidOfGuitarTapp(String s) {
        s = s.replace("ChordPro file generated by GuitarTapp", "");
        s = s.replace("For more info about ChordPro syntax available in GuitarTapp, check our website:", "");
        s = s.replace("http://www.845tools.com/GuitarTapp", "");
        s = s.replace("Follow us on Facebook to stay updated on what's happening with GuitarTapp:", "");
        s = s.replace("http://facebook.com/GuitarTapp", "");
        s = s.trim();
        return s;
    }

    private String guessTags(String s) {
        // Only check for definite comment lines on lines that are short enough to maybe be headings
        if (s.startsWith(";") || s.startsWith("#") | s.length() < 16) {
            // IV - First deal with special cases that use brackets
            s = s.replace("(VERSE)", "[V]");
            s = s.replace("(Verse 1)", "[V1]");
            s = s.replace("(Verse 2)", "[V2]");
            s = s.replace("(Verse 3)", "[V3]");
            s = s.replace("(Verse 4)", "[V4]");
            s = s.replace("(Chorus)", "[C]");


            // IV - Process lines except those that start ;( which we will treat as 'as-is' comments
            // IV - Further commonly used 'Tags' have been added
            if (!(s.startsWith(";") && (s.substring(1).replace(" ","").startsWith("(")))) {
                // IV - Sub-routine supports number variatons of each tags
                s = guessTagsReplaces(s, "Intro", "Intro");
                s = guessTagsReplaces(s, "Outro", "Outro");
                s = guessTagsReplaces(s, "Ending", "Ending");
                s = guessTagsReplaces(s, "Turnaround", "Turnaround");
                s = guessTagsReplaces(s, "Instrumental", "Instrumental");
                s = guessTagsReplaces(s, "Interlude", "Interlude");
                s = guessTagsReplaces(s, "Verse", "V");
                s = guessTagsReplaces(s, "Prechorus", "P");
                s = guessTagsReplaces(s, "Pre-chorus", "P");
                s = guessTagsReplaces(s, "Chorus", "C");
                s = guessTagsReplaces(s, "Bridge", "B");
                s = guessTagsReplaces(s, "Tag", "T");
                if (s.endsWith(":") && s.length() < 12) {
                    // Likely to be another custom tag
                    s = "[" + s.replace(":", "") + "]";
                }
                s = s.replace("[[", "[");
                s = s.replace("]]", "]");
            }
        }
        return s;
    }

    private String guessTagsReplaces (String s, String m, String r) {
        // IV - s incoming string, m matched word, r start of returned heading
        String wm = m;
        // IV - Lots of variations are considered!
        s = s.replace(wm + ":", "[¬¹").
                replace(wm + " 1:", "[¬ 1¹").
                replace(wm + " 2:", "[¬ 2¹").
                replace(wm + " 3:", "[¬ 3¹").
                replace(wm + " 4:", "[¬ 4¹").
                replace(wm + " 1", "[¬ 1¹").
                replace(wm + " 2", "[¬ 2¹").
                replace(wm + " 3", "[¬ 3¹").
                replace(wm + " 4", "[¬ 4¹").
                replace(wm, "[¬¹");
        // IV - And when uppercase
        wm = wm.toUpperCase();
        s = s.replace(wm + ":", "[¬¹").
                replace(wm + " 1:", "[¬ 1¹").
                replace(wm + " 2:", "[¬ 2¹").
                replace(wm + " 3:", "[¬ 3¹").
                replace(wm + " 4:", "[¬ 4¹").
                replace(wm + " 1", "[¬ 1¹").
                replace(wm + " 2", "[¬ 2¹").
                replace(wm + " 3", "[¬ 3¹").
                replace(wm + " 4", "[¬ 4¹").
                replace(wm, "[¬¹");

        // IV - A complete replace has · at the end.
        if (s.endsWith("¹")) {
            // IV - Single character 'start of returned heading' (V etc.) have no following space
            if (r.length() == 1) {
                s = s.replace("¬ ", "¬");
            }
            s = s.replace("¬", r).replace("¹","]");
        } else {
            // IV - For any partial replace, re-work to only replace the matched word
            // IV - For example 'CHORUS1a:' ->  '[¬¹1a:' -> 'Chorus1a:'
            s = s.replace("[¬",m).replace("¹","");
        }
        return s;
    }

    private String fixHeadings(String s) {
        s = s.replace(";[","[");
        s = s.replace("#[","[");
        s = s.replace("; [", "[");
        s = s.replace(";- [", "[");
        s = s.replace("-[", "[");
        s = s.replace("- [", "[");
        s = s.trim();
        if (s.startsWith("[") && s.contains("]") && !s.endsWith("]")) {
            s = s.replace("]", "]\n");
        }
        if (s.contains("[") && s.contains("]") && !s.startsWith("[")) {
            s = s.replace("[", "\n[");
        }
        return s.trim();
    }

    private String removeStartOfEndOfTags(String s) {
        if (s.contains("{start_of_tab")) {
            // Remove tab tags and replace them
            s = "[GuitarTab]";

        } else if (s.contains("{start_of_chorus")) {
            // Remove chorus tags and replace them
            s = "[C]";

        } else if (s.contains("{start_of_bridge") || s.contains("{sob")) {
            // Remove bridge tags and replace them
            s = "[B]";

        } else if (s.contains("{start_of_part")) {
            // Try to make this a custom tag
            s = "[" + s.replace("{start_of_part", "").trim();
            s = s.replace("[ ", "[");
            s = s.replace("[:", "[");
            s = s.replace("}", "]");

        } else if (s.contains("{end_of") || s.contains("{eoc") || s.contains("{eob")) {
            // Replace end tags with blank tag - to keep sections separate (verses not always specified)
            s = "[]";
        }
        return s.trim();
    }

    private String removeInline(String l) {
        if (l.contains("{inline")) {
            l = l.replace("{inline:", "");
            l = l.replace("{inline :", "");
            l = l.replace("{inline", "");
            l = l.replace("}", "");
        }
        return l;
    }

    public String fromOpenSongToChordPro(Context c, ProcessSong processSong, String lyrics) {
        // This receives the text from the edit song lyrics editor and changes the format
        // Allows users to enter their song as chordpro/onsong format
        // The app will convert it into OpenSong before saving.
        StringBuilder newlyrics = new StringBuilder();

        // IV - Protect any chord repeat barlines. Split the lyrics into separate lines
        String[] lines = lyrics.replace("||:","··>").replace(":||", "<··").split("\n");
        ArrayList<String> type = new ArrayList<>();
        int linenums = lines.length;

        // Determine the line types
        for (String l:lines) {
            type.add(processSong.determineLineTypes(l,c));
        }

        boolean dealingwithchorus = false;

        // Go through each line and add the appropriate lyrics with chords in them
        for (int y = 0; y < linenums; y++) {
            // Go through the section a line at a time
            String thislinetype;
            String nextlinetype = "";
            String previouslinetype = "";
            thislinetype = type.get(y);
            if (y < linenums - 1) {
                nextlinetype = type.get(y+1);
            }
            if (y > 0) {
                previouslinetype = type.get(y-1);
            }

            String[] positions_returned;
            String[] chords_returned;
            String[] lyrics_returned;

            switch (processSong.howToProcessLines(y, linenums, thislinetype, nextlinetype, previouslinetype)) {
                // If this is a chord line followed by a lyric line.
                case "chord_then_lyric":
                    if (lines[y].length() > lines[y+1].length()) {
                        lines[y+1] = processSong.fixLineLength(lines[y+1], lines[y].length());
                    }
                    positions_returned = processSong.getChordPositions(lines[y]);
                    // Remove the . at the start of the line
                    if (lines[y].startsWith(".")) {
                        lines[y] = lines[y].replaceFirst("."," ");
                    }
                    chords_returned = processSong.getChordSections(lines[y], positions_returned);
                    lyrics_returned = processSong.getLyricSections(lines[y + 1], positions_returned);

                    // Mark the beginning of the line
                    newlyrics.append("¬");
                    for (int w = 0; w < lyrics_returned.length; w++) {
                        String chord_to_add = "";
                        if (w<chords_returned.length) {
                            if (chords_returned[w] != null && !chords_returned[w].trim().equals("")) {
                                if (chords_returned[w].trim().startsWith(".") || chords_returned[w].trim().startsWith("|") ||
                                        chords_returned[w].trim().startsWith(":")) {
                                    chord_to_add = chords_returned[w];
                                } else {
                                    chord_to_add = "[" + chords_returned[w].trim() + "]";
                                }
                            }
                        }
                        newlyrics.append(chord_to_add).append(lyrics_returned[w]);
                    }
                    break;

                case "chord_only":
                    // Use same logic as chord_then_lyric to guarantee consistency
                    String tempString = processSong.fixLineLength("", lines[y].length());
                    positions_returned = processSong.getChordPositions(lines[y]);
                    // Remove the . at the start of the line
                    if (lines[y].startsWith(".")) {
                        lines[y] = lines[y].replaceFirst("."," ");
                    }
                    chords_returned = processSong.getChordSections(lines[y], positions_returned);
                    lyrics_returned = processSong.getLyricSections(tempString, positions_returned);

                    // Mark the beginning of the line
                    newlyrics.append("¬");
                    for (int w = 0; w < lyrics_returned.length; w++) {
                        String chord_to_add = "";
                        if (w<chords_returned.length) {
                            if (chords_returned[w] != null && !chords_returned[w].trim().equals("")) {
                                if (chords_returned[w].trim().startsWith(".") || chords_returned[w].trim().startsWith("|") ||
                                        chords_returned[w].trim().startsWith(":")) {
                                    chord_to_add = chords_returned[w];
                                } else {
                                    chord_to_add = "[" + chords_returned[w].trim() + "]";
                                }
                            }
                        }
                        newlyrics.append(chord_to_add).append(lyrics_returned[w]);
                    }
                    break;

                case "lyric_no_chord":
                    // Another place to end a Chorus
                    if (lines[y].replace(" ","").equals("")) {
                        if (dealingwithchorus) {
                            // We've finished with the chorus,
                            dealingwithchorus = false;
                            newlyrics.append("{eoc}\n");
                        }
                    }
                    // Mark the beginning of the line
                    newlyrics.append("¬");
                    newlyrics.append(lines[y]);
                    break;

                case "comment_no_chord":
                    newlyrics.append("{c:").append(lines[y].replaceFirst(";","")).append("}");
                    break;

                case "heading":
                    // If this is a chorus, deal with it appropriately
                    // Add the heading as a comment with hash
                    if (lines[y].startsWith("[C")) {
                        dealingwithchorus = true;
                        newlyrics.append("{soc}\n#").append(lines[y]);
                    } else {
                        if (dealingwithchorus) {
                            // We've finished with the chorus,
                            dealingwithchorus = false;
                            newlyrics.append("{eoc}\n" + "#").append(lines[y]);
                        } else {
                            newlyrics.append("#").append(lines[y]);
                        }
                    }
                    break;

                default:
                    // If a line is blank we need to add it and consider an end of a Chorus
                    if (lines[y].trim().equals("")) {
                        // Add just a line beginning (trim the line)
                        newlyrics.append("¬");
                        if (dealingwithchorus) {
                            // We've finished with the chorus,
                            dealingwithchorus = false;
                            newlyrics.append("{eoc}\n");
                        }
                    }
                    break;
            }
            newlyrics.append("\n");

        }
        // We end any active chorus processing at the end of the song
        if (dealingwithchorus) {
            newlyrics.append("{eoc}");
        }
        newlyrics.append("\n");

        // Tidy up
        // IV - Removed some replaces as the code changes (probably) make them redundant
        newlyrics = new StringBuilder(newlyrics.toString().replace("\n\n", "\n"));
        // Remove one space from the start of chord/lyric lines
        newlyrics = new StringBuilder(newlyrics.toString().replace("¬ ", "¬"));
        // Remove beginning of line marker, it has done the job
        newlyrics = new StringBuilder(newlyrics.toString().replace("¬", ""));

        if (newlyrics.toString().startsWith("\n")) {
            newlyrics = new StringBuilder(newlyrics.toString().replaceFirst("\n", ""));
        }

        //IV - Reset any chord repeat bar lines
        newlyrics = new StringBuilder(newlyrics.toString().replace("··>","||:").replace("<··", ":||"));

        return newlyrics.toString();
    }

    private String extractCommentLines(String line) {
        if (line.startsWith("{comments") || line.startsWith("{c:")) {
            line = line.replace("{comments:","");
            line = line.replace("{comments :","");
            line = line.replace("{c:","");
            line = line.replace("{c :","");
            line = line.replace("}","");
            line = ";"+line;
        }
        return line;
    }

    public String fromChordProToOpenSong(String lyrics) {
        // This receives the text from the edit song lyrics editor and changes the format
        // This changes ChordPro formatted songs back to OpenSong format
        // The app will convert it into OpenSong before saving.
        StringBuilder newlyrics = new StringBuilder();

        // Split the lyrics into separate lines
        String[] line = lyrics.split("\n");
        int numlines = line.length;

        for (int x = 0; x < numlines; x++) {
            // Get rid of any extra whitespace and fix the lines
            if (line[x].trim().equals("")) {
                newlyrics.append("\n");
            } else {
                line[x] = makeTagsCommon(line[x]);
                line[x] = removeObsolete(line[x]);
                line[x] = extractChordLines(line[x]);
                line[x] = fixHeadings(line[x]);
                line[x] = guessTags(line[x]);
                line[x] = extractCommentLines(line[x]);

                // IV - Treat start of chorus as a comment - allows song autofix to fix when it fixes comments
                line[x] = line[x].replace("{start_of_chorus}", ";Chorus");

                // IV - For unprocessed lines add a leading space - a fix for mis-aligned lyric only lines
                if (line[x].length() > 0) {
                    String test = ";. {";
                    if (!(test.contains(line[x].substring(0, 1)) || (line[x].contains("[")))) {
                        line[x] = " " + line[x];
                    }
                }
                // Join the individual lines back up (unless they are end of chorus or empty
                if (!line[x].contains("{end_of_chorus}") && !line[x].contains("{eoc}") && !line[x].equals("")) {
                    newlyrics.append(line[x]).append("\n");
                }
            }
        }

        // Final polish
        // Remove double reporting of a Chorus
        newlyrics = new StringBuilder(newlyrics.toString().replace(";Chorus\n[C", "[C"));
        // Imports can leave fields which need a leading space
        newlyrics = new StringBuilder(newlyrics.toString().replace("{", " {"));

        return newlyrics.toString();
    }

}
