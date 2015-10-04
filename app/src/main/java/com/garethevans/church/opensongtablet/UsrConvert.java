package com.garethevans.church.opensongtablet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;

public class UsrConvert extends Activity {

    public static boolean doExtract() throws IOException {

        // This is called when a usr format song has been loaded.
        // This tries to extract the relevant stuff and reformat the
        // <lyrics>...</lyrics>
        String temp = FullscreenActivity.myXML;

        // Initialise all the xml tags a song should have
        FullscreenActivity.mTitle = FullscreenActivity.songfilename;
        LoadXML.initialiseSongTags();

        // Break the temp variable into an array split by line
        // Check line endings are \n
        temp = temp.replace("\r\n", "\n");
        temp = temp.replace("\r", "\n");
        temp = temp.replace("\n\n\n", "\n\n");
        temp = temp.replace("\'", "'");
        temp = temp.replace("&quot;", "\"");
        temp = temp.replace("\\'", "'");
        temp = temp.replace("&quot;", "\"");

        String[] line = temp.split("\n");
        int numlines = line.length;
        if (numlines < 0) {
            numlines = 1;
        }

        String temptitle = "";
        String tempauthor = "";
        String tempcopy = "";
        String tempccli = "";
        String temptheme = "";
        String tempkey = "";
        String tempfields = "";
        String tempwords = "";

        // Go through individual lines and fix simple stuff
        for (int x = 0; x < numlines; x++) {
            // Get rid of any extra whitespace
            line[x] = line[x].trim();

            // If line has title, grab it
            if (line[x].contains("Title=")) {
                temptitle = line[x].replace("Title=","");
            }

            // If line has author, grab it
            if (line[x].contains("Author=")) {
                tempauthor = line[x].replace("Author=","");
                tempauthor = tempauthor.replace("|", ",");
            }

            // If line has ccli, grab it
            if (line[x].contains("[S A")) {
                tempccli = line[x].replace("[S A","");
                tempccli = tempccli.replace("]", "");
            }

            // If line has copyright, grab it
            if (line[x].contains("Copyright=")) {
                tempcopy = line[x].replace("Copyright=", "");
                tempcopy = tempcopy.replace("|",",");
            }

            // If line has theme, grab it
            if (line[x].contains("Themes=")) {
                temptheme = line[x].replace("Themes=","");
                temptheme = temptheme.replace("/t","; ");
            }

            // If line has key, grab it
            if (line[x].contains("Keys=")) {
                tempkey = line[x].replace("Keys=","");
            }

            // If line has fields, grab it
            if (line[x].contains("Fields=")) {
                tempfields = line[x].replace("Fields=","");
                // Replace known fields
                tempfields = tempfields.replace(FullscreenActivity.tag_bridge+" ","B");
                tempfields = tempfields.replace(FullscreenActivity.tag_bridge,"B");
                tempfields = tempfields.replace(FullscreenActivity.tag_prechorus+" ","P");
                tempfields = tempfields.replace(FullscreenActivity.tag_prechorus,"B");
                tempfields = tempfields.replace(FullscreenActivity.tag_chorus+" ","C");
                tempfields = tempfields.replace(FullscreenActivity.tag_chorus,"C");
                tempfields = tempfields.replace(FullscreenActivity.tag_verse+" ","V");
                tempfields = tempfields.replace(FullscreenActivity.tag_verse,"V");
                tempfields = tempfields.replace(FullscreenActivity.tag_tag+" ","T");
                tempfields = tempfields.replace(FullscreenActivity.tag_tag,"T");
            }

            // If line has words, grab it
            if (line[x].contains("Words=")) {
                tempwords = line[x].replace("Words=","");
            }

            // Change | separator
            line[x] = line[x].replace("|",",");
        }

        // Fix the newline tag for words
        tempwords = tempwords.replace("/n","\n");

        // Split the words up by sections
        String[] sections = tempwords.split("/t");

        // Split the section titles up
        String[] sectiontitles = tempfields.split("/t");

        String templyrics = "";

        // Go through the sections and add the appropriate tag
        for (int s=0;s<sections.length;s++) {
            if (sections[s].indexOf("(")==0) {
                sections[s] = sections[s].replace("(", "[");
                sections[s] = sections[s].replace(")", "]");
                int tagstart = sections[s].indexOf("[");
                int tagend = sections[s].indexOf("]");
                String customtag = "";
                if (tagstart>-1 && tagend>1) {
                    customtag = sections[s].substring(tagstart+1,tagend-1);
                }
                String newtag = customtag;
                // Replace any know custom tags
                newtag = newtag.replace(FullscreenActivity.tag_bridge+" ","B");
                newtag = newtag.replace(FullscreenActivity.tag_bridge,"B");
                newtag = newtag.replace(FullscreenActivity.tag_prechorus+" ","P");
                newtag = newtag.replace(FullscreenActivity.tag_prechorus,"B");
                newtag = newtag.replace(FullscreenActivity.tag_chorus+" ","C");
                newtag = newtag.replace(FullscreenActivity.tag_chorus,"C");
                newtag = newtag.replace(FullscreenActivity.tag_verse+" ","V");
                newtag = newtag.replace(FullscreenActivity.tag_verse,"V");
                newtag = newtag.replace(FullscreenActivity.tag_tag+" ","T");
                newtag = newtag.replace(FullscreenActivity.tag_tag,"T");
                sections[s] = sections[s].replace(customtag, newtag);

            } else {
                if (sectiontitles[s]!=null) {
                    sections[s] = "[" + sectiontitles[s] + "]\n" + sections[s];
                }
            }
            // Fix all line breaks
            sections[s] = sections[s].replace("/n", "\n ");

            templyrics = templyrics + sections[s] + "\n";

        }


        // Get rid of double line breaks
        while (templyrics.contains("\n\n\n")) {
            templyrics = templyrics.replace("\n\n\n","\n\n");
        }


        FullscreenActivity.myXML = "<song>\r\n"
                + "<title>" + temptitle.trim() + "</title>\r\n"
                + "<author>" + tempauthor.trim() + "</author>\r\n"
                + "<copyright>" + tempcopy.trim() + "</copyright>\r\n"
                + "  <presentation></presentation>\r\n"
                + "  <hymn_number></hymn_number>\r\n"
                + "  <capo print=\"false\"></capo>\r\n"
                + "  <tempo></tempo>\r\n"
                + "  <time_sig></time_sig>\r\n"
                + "  <duration></duration>\r\n"
                + "  <ccli>" + tempccli.trim() + "</ccli>\r\n"
                + "  <theme>" + temptheme.trim() + "</theme>\r\n"
                + "  <alttheme></alttheme>\r\n"
                + "  <user1></user1>\r\n"
                + "  <user2></user2>\r\n"
                + "  <user3></user3>\r\n"
                + "  <key>" + tempkey + "</key>\r\n"
                + "  <aka></aka>\r\n"
                + "  <key_line></key_line>\r\n"
                + "  <books></books>\r\n"
                + "  <midi></midi>\r\n"
                + "  <midi_index></midi_index>\r\n"
                + "  <pitch></pitch>\r\n"
                + "  <restrictions></restrictions>\r\n"
                + "  <notes></notes>\r\n"
                + "  <lyrics>" + templyrics.trim() + "</lyrics>\r\n"
                + "  <linked_songs></linked_songs>\n"
                + "  <pad_file></pad_file>\n"
                + "  <custom_chords></custom_chords>\n"
                + "  <link_youtube></custom_chords>\n"
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

        // Change the name of the song to remove usr file extension
        // (not needed)
        String newSongTitle = FullscreenActivity.songfilename;

        // Decide if a better song title is in the file
        if (temptitle.length() > 0) {
            newSongTitle = temptitle;
        }

        newSongTitle = newSongTitle.replace(".usr", "");
        newSongTitle = newSongTitle.replace(".USR", "");
        newSongTitle = newSongTitle.replace(".txt", "");

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
        from.renameTo(to);
        FullscreenActivity.songfilename = newSongTitle;

        // Load the songs
        ListSongFiles.listSongs();

        // Get the song indexes
        ListSongFiles.getCurrentSongIndex();
        Preferences.savePreferences();

        // Prepare the app to fix the song menu with the new file
        FullscreenActivity.converting = true;

        return true;
    }
}