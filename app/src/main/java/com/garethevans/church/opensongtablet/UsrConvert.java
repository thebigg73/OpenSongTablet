package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;
import java.io.OutputStream;

class UsrConvert {

    boolean doExtract(Context c) {

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
                tempfields = tempfields.replace(c.getResources().getString(R.string.tag_bridge)+" ","B");
                tempfields = tempfields.replace(c.getResources().getString(R.string.tag_bridge),"B");
                tempfields = tempfields.replace(c.getResources().getString(R.string.tag_prechorus)+" ","P");
                tempfields = tempfields.replace(c.getResources().getString(R.string.tag_prechorus),"B");
                tempfields = tempfields.replace(c.getResources().getString(R.string.tag_chorus)+" ","C");
                tempfields = tempfields.replace(c.getResources().getString(R.string.tag_chorus),"C");
                tempfields = tempfields.replace(c.getResources().getString(R.string.tag_verse)+" ","V");
                tempfields = tempfields.replace(c.getResources().getString(R.string.tag_verse),"V");
                tempfields = tempfields.replace(c.getResources().getString(R.string.tag_tag)+" ","T");
                tempfields = tempfields.replace(c.getResources().getString(R.string.tag_tag),"T");
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

        StringBuilder templyrics = new StringBuilder();

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
                newtag = newtag.replace(c.getResources().getString(R.string.tag_bridge)+" ","B");
                newtag = newtag.replace(c.getResources().getString(R.string.tag_bridge),"B");
                newtag = newtag.replace(c.getResources().getString(R.string.tag_prechorus)+" ","P");
                newtag = newtag.replace(c.getResources().getString(R.string.tag_prechorus),"B");
                newtag = newtag.replace(c.getResources().getString(R.string.tag_chorus)+" ","C");
                newtag = newtag.replace(c.getResources().getString(R.string.tag_chorus),"C");
                newtag = newtag.replace(c.getResources().getString(R.string.tag_verse)+" ","V");
                newtag = newtag.replace(c.getResources().getString(R.string.tag_verse),"V");
                newtag = newtag.replace(c.getResources().getString(R.string.tag_tag)+" ","T");
                newtag = newtag.replace(c.getResources().getString(R.string.tag_tag),"T");
                sections[s] = sections[s].replace(customtag, newtag);

            } else {
                if (sectiontitles[s]!=null) {
                    sections[s] = "[" + sectiontitles[s] + "]\n" + sections[s];
                }
            }
            // Fix all line breaks
            sections[s] = sections[s].replace("/n", "\n ");

            templyrics.append(sections[s]).append("\n");

        }


        // Get rid of double line breaks
        while (templyrics.toString().contains("\n\n\n")) {
            templyrics = new StringBuilder(templyrics.toString().replace("\n\n\n", "\n\n"));
        }


        // Initialise the variables
        SongXML songXML = new SongXML();
        songXML.initialiseSongTags();

        // Set the correct values
        FullscreenActivity.mTitle = temptitle.trim();
        FullscreenActivity.mAuthor = tempauthor.trim();
        FullscreenActivity.mCopyright = tempcopy.trim();
        FullscreenActivity.mCCLI = tempccli.trim();
        FullscreenActivity.mTheme = temptheme.trim();
        FullscreenActivity.mKey = tempkey.trim();
        FullscreenActivity.mLyrics = templyrics.toString().trim();

        FullscreenActivity.myXML = songXML.getXML();

        // Change the name of the song to remove usr file extension
        String newSongTitle = FullscreenActivity.songfilename;

        // Decide if a better song title is in the file
        if (temptitle.length() > 0) {
            newSongTitle = temptitle;
        }
        newSongTitle = newSongTitle.replace(".usr", "");
        newSongTitle = newSongTitle.replace(".USR", "");
        newSongTitle = newSongTitle.replace(".txt", "");

        // Now write the modified song
        StorageAccess storageAccess = new StorageAccess();
        Uri uri = storageAccess.getUriForItem(c,"Songs", FullscreenActivity.whichSongFolder,newSongTitle);
        OutputStream outputStream = storageAccess.getOutputStream(c, uri);
        if (storageAccess.writeFileFromString(FullscreenActivity.myXML,outputStream)) {
            // Writing was successful, so delete the original
            Uri originalfile = storageAccess.getUriForItem(c,"Songs", FullscreenActivity.whichSongFolder,FullscreenActivity.songfilename);
            storageAccess.deleteFile(c,originalfile);
        }

        FullscreenActivity.songfilename = newSongTitle;

        // Load the songs
        ListSongFiles.getAllSongFiles(c,storageAccess);

        // Get the song indexes
        ListSongFiles.getCurrentSongIndex();
        Preferences.savePreferences();

        // Prepare the app to fix the song menu with the new file
        FullscreenActivity.converting = true;

        return true;
    }
}