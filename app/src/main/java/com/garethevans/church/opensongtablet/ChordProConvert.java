package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

class ChordProConvert {

    static boolean doExtract() throws IOException {

        // This is called when a ChordPro format song has been loaded.
        // This tries to extract the relevant stuff and reformat the
        // <lyrics>...</lyrics>
        String temp = FullscreenActivity.myXML;
        String parsedlines;
        // Initialise all the xml tags a song should have
        FullscreenActivity.mTitle = FullscreenActivity.songfilename;
        LoadXML.initialiseSongTags();

        // Break the temp variable into an array split by line
        // Check line endings are \n

        temp = fixLineBreaksAndSlashes(temp);

        String[] line = temp.split("\n");

        int numlines = line.length;
        if (numlines < 0) {
            numlines = 1;
        }

        String temptitle = "";
        String tempsubtitle;
        String tempccli = "";
        String tempauthor = "";
        String tempcopyright = "";
        String tempkey = "";
        String temptimesig = "";
        String temptempo = "";

        // Go through individual lines and fix simple stuff
        for (int x = 0; x < numlines; x++) {
            // Get rid of any extra whitespace
            Log.d("LineTrimBefore","~"+line[x]);
            line[x] = line[x].trim();
            Log.d("LineTrimAfter","~"+line[x]);
            // Make tag lines common
            line[x] = makeTagsCommon(line[x]);

            // Remove directive lines we don't need
            line[x] = removeObsolete(line[x]);



            // Extract the title
            if (line[x].contains("{title:")) {
                temptitle = removeTags(line[x],"{title:");
                line[x] = "";
            }

            // Extract the author
            if (line[x].contains("{artist:")) {
                tempauthor =  removeTags(line[x],"{artist:");
                line[x] = "";
            }

            // Extract the copyright
            if (line[x].contains("{copyright:")) {
                tempcopyright =  removeTags(line[x],"{copyright:");
                line[x] = "";
            }

            // Extract the subtitles
            if (line[x].contains("{subtitle:")) {
                tempsubtitle =  removeTags(line[x],"{subtitle:");
                if (tempauthor.equals("")) {
                    tempauthor = tempsubtitle;
                }
                if (tempcopyright.equals("")) {
                    tempcopyright = tempsubtitle;
                }
                line[x] = ";" + tempsubtitle;
            }

            // Extract the ccli (not really a chordpro tag, but works for songselect and worship together
            if (line[x].contains("{ccli:")) {
                tempccli =  removeTags(line[x],"{ccli::");
                line[x] = "";
            }

            // Extract the key
            if (line[x].contains("{key:")) {
                tempkey =  removeTags(line[x],"{key:");
                line[x] = "";
            }

            // Extract the tempo
            if (line[x].contains("{tempo:")) {
                temptempo =  removeTags(line[x],"{tempo:");
                line[x] = "";
            }

            // Extract the timesig
            if (line[x].contains("{time:")) {
                temptimesig =  removeTags(line[x],"{time:");
                line[x] = "";
            }

            // Change lines that start with # into comment lines
            if (line[x].startsWith("#")) {
                line[x] = line[x].replaceFirst("#", ";");
            }

            // Change comment lines
            if (line[x].contains("{comments:")) {
                line[x] = ";" +  removeTags(line[x],"{comments:");
            }

            // Change comment lines
            if (line[x].contains("{comment:")) {
                line[x] = ";" +  removeTags(line[x],"{comment:");
            }

        }

        // Go through each line and try to fix chord lines
        for (int x = 0; x < numlines; x++) {
            line[x] = extractChordLines(line[x]);
        }

        // Join the individual lines back up
        parsedlines = "";
        for (int x = 0; x < numlines; x++) {
            // Try to guess tags used
            line[x] = guessTags(line[x]);

            //Added to complete guess tags operation
            line[x] = fixHeadings(line[x]);
            parsedlines = parsedlines + line[x] + "\n";
        }

        // Remove start and end of tabs
        while (parsedlines.contains("{start_of_tab") && parsedlines.contains("{end_of_tab")) {
            int startoftabpos;
            int endoftabpos;
            startoftabpos = parsedlines.indexOf("{start_of_tab");
            endoftabpos = parsedlines.indexOf("{end_of_tab") + 12;

            if (endoftabpos > 13 && startoftabpos > -1 && endoftabpos > startoftabpos) {
                String startbit = parsedlines.substring(0, startoftabpos);
                String endbit = parsedlines.substring(endoftabpos);
                parsedlines = startbit + endbit;
            }
        }

        // Change start and end of chorus
        while (parsedlines.contains("{start_of_chorus")) {

            parsedlines = parsedlines.replace("{start_of_chorus}","[C]");
            parsedlines = parsedlines.replace("{start_of_chorus:}","[C]");
            parsedlines = parsedlines.replace("{start_of_chorus :}","[C]");
            parsedlines = parsedlines.replace("{start_of_chorus","[C]");

            parsedlines = parsedlines.replace(":","");
            parsedlines = parsedlines.replace("}","");
        }

        while (parsedlines.contains("{end_of_chorus")) {

            parsedlines = parsedlines.replace("{end_of_chorus}","[]");
            parsedlines = parsedlines.replace("{end_of_chorus:}","[]");
            parsedlines = parsedlines.replace("{end_of_chorus :}","[]");
            parsedlines = parsedlines.replace("{end_of_chorus","[]");
            parsedlines = parsedlines.replace(":","");
            parsedlines = parsedlines.replace("}","");
        }
        /*Added to support the parsing of bridge in ChordPro Format
         * {start_of_bridge}
         * {end_of_bridge}
         * {sob}
         * {eob}
         * */
        while (parsedlines.contains("{start_of_bridge")) {

            parsedlines = parsedlines.replace("{start_of_bridge}","[B]");
            parsedlines = parsedlines.replace("{start_of_bridge:}","[B]");
            parsedlines = parsedlines.replace("{start_of_bridge :}","[B]");
            parsedlines = parsedlines.replace("{start_of_bridge","[B]");

            parsedlines = parsedlines.replace(":","");
            parsedlines = parsedlines.replace("}","");
        }

        while (parsedlines.contains("{end_of_bridge")) {

            parsedlines = parsedlines.replace("{end_of_bridge}","[]");
            parsedlines = parsedlines.replace("{end_of_bridge:}","[]");
            parsedlines = parsedlines.replace("{end_of_bridge :}","[]");
            parsedlines = parsedlines.replace("{end_of_bridge","[]");
            parsedlines = parsedlines.replace(":","");
            parsedlines = parsedlines.replace("}","");
        }

        // Get rid of double line breaks
        while (parsedlines.contains("\n\n\n")) {
            parsedlines = parsedlines.replace("\n\n\n","\n\n");
        }

        while (parsedlines.contains(";\n\n;")) {
            parsedlines = parsedlines.replace(";\n\n;",";\n");
        }

        // Ok, go back through the parsed lines and add spaces to the beginning
        // of lines that aren't comments, chords or tags
        String[] line2 = parsedlines.split("\n");
        int numlines2 = line2.length;
        if (numlines2 < 0) {
            numlines2 = 1;
        }
        // Reset the parsed lines
        parsedlines = "";

        // Go through the lines one at a time
        // Add the fixed bit back together
        for (int x = 0; x < numlines2; x++) {

            if (line2[x].length() > 1) {

                //Take the line and eliminates spaces before the first letter of symbol
                //to avoid taking chorus or bridge tags as lyrics


                String lineAux =line2[x].trim();
                /*Also could change the 0 in the if to index 1*/
                if (lineAux.indexOf("[") != 0 && lineAux.indexOf(";") != 0
                        && lineAux.indexOf(".") != 0 ) {
                    /*the line contains lyrics*/

                    line2[x] = " " + line2[x];


                }


                else{
                    //Keep the line without spaces before a letter of symbol
                    line2[x] = lineAux;

                }

            }
            parsedlines = parsedlines + line2[x] + "\n";

        }



        FullscreenActivity.myXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<song>\n"
                + "  <title>" + temptitle.trim() + "</title>\n"
                + "  <author>" + tempauthor.trim() + "</author>\n"
                + "  <copyright>" + tempcopyright.trim() + "</copyright>\n"
                + "  <presentation></presentation>\n"
                + "  <hymn_number></hymn_number>\n"
                + "  <capo print=\"false\"></capo>\n"
                + "  <tempo>" + temptempo.trim() + "</tempo>\n"
                + "  <time_sig>" + temptimesig.trim() + "</time_sig>\n"
                + "  <duration></duration>\n"
                + "  <ccli>" + tempccli.trim() + "</ccli>\n"
                + "  <theme></theme>\n"
                + "  <alttheme></alttheme>\n"
                + "  <user1></user1>\n"
                + "  <user2></user2>\n"
                + "  <user3></user3>\n"
                + "  <key>" + tempkey + "</key>\n"
                + "  <aka></aka>\n"
                + "  <key_line></key_line>\n"
                + "  <books></books>\n"
                + "  <midi></midi>\n"
                + "  <midi_index></midi_index>\n"
                + "  <pitch></pitch>\n"
                + "  <restrictions></restrictions>\n"
                + "  <notes></notes>\n"
                + "  <lyrics>" + parsedlines.trim() + "</lyrics>\n"
                + "  <linked_songs></linked_songs>\n"
                + "  <pad_file></pad_file>\n"
                + "  <custom_chords></custom_chords>\n"
                + "  <link_youtube></link_youtube>\n"
                + "  <link_web></link_web>\n"
                + "  <link_audio></link_audio>\n"
                + "  <link_other></link_other>\n"
                + "</song>";

        // Save this song in the right format!
        // Makes sure all & are replaced with &amp;
        FullscreenActivity.myXML = FullscreenActivity.myXML.replace("&amp;",
                "&");
        FullscreenActivity.myXML = FullscreenActivity.myXML.replace("&",
                "&amp;");

        FullscreenActivity.myXML = FullscreenActivity.myXML.replace("\'","'");
        FullscreenActivity.myXML = FullscreenActivity.myXML.replace("Õ","'");
        FullscreenActivity.myXML = FullscreenActivity.myXML.replace("Ó","'");
        FullscreenActivity.myXML = FullscreenActivity.myXML.replace("Ò","'");
        // Save the file
        Preferences.savePreferences();

        // Now write the modified song
        FileOutputStream overWrite;

        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            overWrite = new FileOutputStream(FullscreenActivity.dir + "/"
                    + FullscreenActivity.songfilename, false);
        } else {
            overWrite = new FileOutputStream(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                    + FullscreenActivity.songfilename, false);
        }
        overWrite.write(FullscreenActivity.myXML.getBytes());
        overWrite.flush();
        overWrite.close();

        // Change the name of the song to remove chordpro file extension
        // (not needed)
        String newSongTitle = FullscreenActivity.songfilename;

        // Decide if a better song title is in the file
        if (temptitle.length() > 0) {
            newSongTitle = temptitle;
        }

        newSongTitle = newSongTitle.replace(".pro", "");
        newSongTitle = newSongTitle.replace(".PRO", "");
        newSongTitle = newSongTitle.replace(".chopro", "");
        newSongTitle = newSongTitle.replace(".chordpro", "");
        newSongTitle = newSongTitle.replace(".CHOPRO", "");
        newSongTitle = newSongTitle.replace(".CHORDPRO", "");
        newSongTitle = newSongTitle.replace(".cho", "");
        newSongTitle = newSongTitle.replace(".CHO", "");
        newSongTitle = newSongTitle.replace(".txt", "");
        newSongTitle = newSongTitle.replace(".TXT", "");

        File from;
        File to;

        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            from = new File(FullscreenActivity.dir + "/"
                    + FullscreenActivity.songfilename);
            to = new File(FullscreenActivity.dir + "/" + newSongTitle);
        } else {
            from = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                    + FullscreenActivity.songfilename);
            to = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                    + newSongTitle);
        }

        // IF THE FILENAME ALREADY EXISTS, REALLY SHOULD ASK THE USER FOR A NEW FILENAME
        // OR append _ to the end - STILL TO DO!!!!!
        while(to.exists()) {
            newSongTitle = newSongTitle+"_";
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                to = new File(FullscreenActivity.dir + "/" + newSongTitle);
            } else {
                to = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + newSongTitle);
            }
        }

        // Do the renaming
        if(!from.renameTo(to)) {
            Log.d("d","Couldn't rename");
        }
        FullscreenActivity.songfilename = newSongTitle;

        // Load the songs
        ListSongFiles.getAllSongFiles();

        // Get the song indexes
        ListSongFiles.getCurrentSongIndex();
        Preferences.savePreferences();

        // Prepare the app to fix the song menu with the new file
        FullscreenActivity.converting = true;

        return true;
    }

    private static String fixLineBreaksAndSlashes(String s) {
        s = s.replace("\r\n", "\n");
        s = s.replace("\r", "\n");
        s = s.replace("\n\n\n", "\n\n");
        s = s.replace("\'", "'");
        s = s.replace("&quot;", "\"");
        s = s.replace("\\'", "'");
        s = s.replace("&quot;", "\"");
        s = s.replace("<", "(");
        s = s.replace(">", ")");
        s = s.replace("&#39;", "'");
        return s;
    }

    private static String makeTagsCommon(String s) {
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

    private static String removeObsolete(String s) {
        if (s.contains("{new_song")
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
        return s;
    }

    private static String removeTags(String s, String tagstart) {
        s = s.replace(tagstart, "");
        s = s.replace("}", "");
        return s;
    }

    private static String extractChordLines(String s) {
        String tempchordline = "";
        if (!s.startsWith("#") && !s.startsWith(";")) {
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
                    tempchordline = tempchordline + " ";
                }
                // Now add the chord
                tempchordline = tempchordline + chord;
            }
            // All chords should be gone now, so remove any remaining [ and ]
            s = s.replace("[", "");
            s = s.replace("]", "");
            if (!s.startsWith(" ")) {
                s = " " + s;
            }
            if (tempchordline.length() > 0) {
                s = "." + tempchordline + "\n" + s;
            }
        }
        return s;
    }

    private static String guessTags(String s) {
        if (s.startsWith(";") || s.startsWith("#")) {
            s = s.replace("Intro:", "[Intro]");
            s = s.replace("Intro", "[Intro]");
            s = s.replace("Outro:", "[Outro]");
            s = s.replace("Outro", "[Outro]");
            s = s.replace("Verse:", "[V]");
            s = s.replace("VERSE:", "[V]");
            s = s.replace("Verse 1:", "[V1]");
            s = s.replace("Verse 2:", "[V2]");
            s = s.replace("Verse 3:", "[V3]");
            s = s.replace("Verse 4:", "[V4]");
            s = s.replace("VERSE 1:", "[V1]");
            s = s.replace("VERSE 2:", "[V2]");
            s = s.replace("VERSE 3:", "[V3]");
            s = s.replace("VERSE 4:", "[V4]");
            s = s.replace("(VERSE)", "[V]");
            s = s.replace("(Verse 1)", "[V1]");
            s = s.replace("(Verse 2)", "[V2]");
            s = s.replace("(Verse 3)", "[V3]");
            s = s.replace("Verse 1", "[V1]");
            s = s.replace("Verse 2", "[V2]");
            s = s.replace("Verse 3", "[V3]");
            s = s.replace("Verse 4", "[V4]");
            s = s.replace("VERSE 1", "[V1]");
            s = s.replace("VERSE 2", "[V2]");
            s = s.replace("VERSE 3", "[V3]");
            s = s.replace("VERSE 4", "[V4]");
            s = s.replace("Verse", "[V]");
            s = s.replace("VERSE", "[V]");
            s = s.replace("Prechorus:", "[P]");
            s = s.replace("Pre-chorus:", "[P]");
            s = s.replace("PreChorus:", "[P]");
            s = s.replace("Pre-Chorus:", "[P]");
            s = s.replace("PRECHORUS:", "[P]");
            s = s.replace("Prechorus 1:", "[P1]");
            s = s.replace("Prechorus 2:", "[P2]");
            s = s.replace("Prechorus 3:", "[P3]");
            s = s.replace("PreChorus 1:", "[P1]");
            s = s.replace("PreChorus 2:", "[P2]");
            s = s.replace("PreChorus 3:", "[P3]");
            s = s.replace("Pre-Chorus 1:", "[P1]");
            s = s.replace("Pre-Chorus 2:", "[P2]");
            s = s.replace("Pre-Chorus 3:", "[P3]");
            s = s.replace("(Chorus)", "[C]");
            s = s.replace("Chorus:", "[C]");
            s = s.replace("CHORUS:", "[C]");
            s = s.replace("Chorus 1:", "[C1]");
            s = s.replace("Chorus 2:", "[C2]");
            s = s.replace("Chorus 3:", "[C3]");
            s = s.replace("CHORUS 1:", "[C1]");
            s = s.replace("CHORUS 2:", "[C2]");
            s = s.replace("CHORUS 3:", "[C3]");
            s = s.replace("Chorus 1", "[C1]");
            s = s.replace("Chorus 2", "[C2]");
            s = s.replace("Chorus 3", "[C3]");
            s = s.replace("CHORUS 1", "[C1]");
            s = s.replace("CHORUS 2", "[C2]");
            s = s.replace("CHORUS 3", "[C3]");
            s = s.replace("Chorus", "[C]");
            s = s.replace("CHORUS", "[C]");
            s = s.replace("Bridge:", "[B]");
            s = s.replace("BRIDGE:", "[B]");
            s = s.replace("Bridge 1:", "[B1]");
            s = s.replace("Bridge 2:", "[B2]");
            s = s.replace("Bridge 3:", "[B3]");
            s = s.replace("BRIDGE 1:", "[B1]");
            s = s.replace("BRIDGE 2:", "[B2]");
            s = s.replace("BRIDGE 3:", "[B3]");
            s = s.replace("Tag:", "[T]");
            if (s.endsWith(":") && s.length() < 12) {
                // Likely to be another custom tag
                s = "[" + s.replace(":", "") + "]";
            }
            s = s.replace("[[", "[");
            s = s.replace("]]", "]");
        }
        return s;
    }

    private static String fixHeadings(String s) {
        s = s.replace(";[","[");
        s = s.replace("#[","[");
        return s;
    }

    static String fromOpenSongToChordPro(String lyrics, Context c) {
        // This receives the text from the edit song lyrics editor and changes the format
        // Allows users to enter their song as chordpro/onsong format
        // The app will convert it into OpenSong before saving.
        String newlyrics = "";

        // Split the lyrics into separate lines
        String[] lines = lyrics.split("\n");
        ArrayList<String> type = new ArrayList<>();
        int linenums = lines.length;

        // Determine the line types
        for (String l:lines) {
            type.add(ProcessSong.determineLineTypes(l,c));
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

            switch (ProcessSong.howToProcessLines(y, linenums, thislinetype, nextlinetype, previouslinetype)) {
                // If this is a chord line followed by a lyric line.
                case "chord_then_lyric":
                    if (lines[y].length() > lines[y+1].length()) {
                        lines[y+1] = ProcessSong.fixLineLength(lines[y+1], lines[y].length());
                    }
                    positions_returned = ProcessSong.getChordPositions(lines[y]);
                    // Remove the . at the start of the line
                    if (lines[y].startsWith(".")) {
                        lines[y] = lines[y].replaceFirst("."," ");
                    }
                    chords_returned = ProcessSong.getChordSections(lines[y], positions_returned);
                    lyrics_returned = ProcessSong.getLyricSections(lines[y + 1], positions_returned);
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
                        } else {
                            chord_to_add = "";
                        }
                        newlyrics += chord_to_add + lyrics_returned[w];
                    }
                    break;

                case "chord_only":
                    positions_returned = ProcessSong.getChordPositions(lines[y]);
                    chords_returned = ProcessSong.getChordSections(lines[y], positions_returned);
                    for (String aChords_returned : chords_returned) {
                        String chord_to_add = "";
                        if (aChords_returned != null && !aChords_returned.trim().equals("")) {
                            chord_to_add = "[" + aChords_returned.trim() + "]";
                        }
                        newlyrics += chord_to_add;
                    }
                    break;

                case "lyric_no_chord":
                    newlyrics += lines[y];
                    break;

                case "comment_no_chord":
                    newlyrics += "{c:" + lines[y]+ "}";
                    break;

                case "heading":
                    // If this is a chorus, deal with it appropriately
                    // Add the heading as a comment with hash

                    if (lines[y].startsWith("[C")) {
                        dealingwithchorus = true;
                        newlyrics += "{soc}\n#"+lines[y];
                    } else {
                        if (dealingwithchorus) {
                            // We've finished with the chorus,
                            dealingwithchorus = false;
                            newlyrics += "{eoc}\n"+"#"+lines[y];
                        } else {
                            newlyrics += "#"+lines[y];
                        }
                    }
                    break;
            }
            newlyrics += "\n";

        }
        newlyrics += "\n";
        newlyrics = newlyrics.replace("\n\n{eoc}","\n{eoc}\n");
        newlyrics = newlyrics.replace("\n \n{eoc}","\n{eoc}\n");
        newlyrics = newlyrics.replace("\n\n{eoc}","\n{eoc}\n");
        newlyrics = newlyrics.replace("\n \n{eoc}","\n{eoc}\n");
        newlyrics = newlyrics.replace("][","]  [");
        newlyrics = newlyrics.replace("\n\n","\n");

        if (newlyrics.startsWith("\n")) {
            newlyrics = newlyrics.replaceFirst("\n","");
        }

        return newlyrics;
    }

    static String fromChordProToOpenSong(String lyrics) {
        // This receives the text from the edit song lyrics editor and changes the format
        // This changes ChordPro formatted songs back to OpenSong format
        // The app will convert it into OpenSong before saving.
        String newlyrics = "";

        // Split the lyrics into separate lines
        String[] line = lyrics.split("\n");
        int numlines = line.length;

        for (int x = 0; x < numlines; x++) {
            // Get rid of any extra whitespace and fix the lines
            line[x] = makeTagsCommon(line[x]);
            line[x] = removeObsolete(line[x]);
            line[x] = extractChordLines(line[x]);
            line[x] = fixHeadings(line[x]);
            line[x] = guessTags(line[x]);


            // Join the individual lines back up (unless they are start/end of chorus)
            if (!line[x].contains("{start_of_chorus}") && !line[x].contains("{soc}") &&
                    !line[x].contains("{end_of_chorus}") && !line[x].contains("{eoc}")) {
                newlyrics = newlyrics + line[x] + "\n";
            }
        }
        return newlyrics;
    }
}