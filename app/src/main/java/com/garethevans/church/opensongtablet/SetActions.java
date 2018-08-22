package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SetActions extends Activity {

    public interface MyInterface {
        void doMoveSection();
        void loadSong();
    }

    public static MyInterface mListener;

    private static boolean encodedimage;
    static String title = "", author = "", lyrics = "", hymn_number = "";
    private static String custom_notes, user1 = "", user2 = "", user3 = "", aka = "", key_line = "";
    private static XmlPullParser xpp;

    static void updateOptionListSets() {
        // Load up the songs in the Sets folder
        File[] tempmyFiles = FullscreenActivity.dirsets.listFiles();
        // Go through this list and check if the item is a directory or a file.
        // Add these to the correct array
        int tempnumfiles = 0;
        if (tempmyFiles!=null) {
            tempnumfiles = tempmyFiles.length;
        }

        int numactualfiles = 0;
        int numactualdirs = 0;
        for (int x = 0; x < tempnumfiles; x++) {
            if (tempmyFiles[x].isFile()) {
                numactualfiles++;
            } else {
                numactualdirs++;
            }
        }

        // Now set the size of the arrays
        FullscreenActivity.mySetsFileNames = new String[numactualfiles];
        FullscreenActivity.mySetsFiles = new File[numactualfiles];
        FullscreenActivity.mySetsFolderNames = new String[numactualdirs];
        FullscreenActivity.mySetsDirectories = new File[numactualdirs];

        // Go back through these items and add them to the file names
        // whichset is an integer that goes through the mySetsFileNames array
        // whichsetfolder is an integer that goes through the mySetsFolderNames
        // array
        int whichset = 0;
        int whichsetfolder = 0;
        for (int x = 0; x < tempnumfiles; x++) {
            if (tempmyFiles[x].isFile()) {
                FullscreenActivity.mySetsFileNames[whichset] = tempmyFiles[x].getName();
                FullscreenActivity.mySetsFiles[whichset] = tempmyFiles[x];
                whichset++;
            } else if (tempmyFiles[x].isDirectory()) {
                FullscreenActivity.mySetsFolderNames[whichsetfolder] = tempmyFiles[x].getName();
                FullscreenActivity.mySetsDirectories[whichsetfolder] = tempmyFiles[x];
                whichsetfolder++;
            }
        }

        // Make the array in the setList list these sets
        // Set the variable setView to be true
        FullscreenActivity.showingSetsToLoad = true;
        // The above line isn't needed anymore
    }

    static void prepareSetList() {
        try {
            FullscreenActivity.mSet = null;
            FullscreenActivity.mSetList = null;

            // Remove any blank set entries that shouldn't be there
            FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**__**$", "");

            // Add a delimiter between songs
            FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$$**_", "_**$%%%$**_");

            // Break the saved set up into a new String[]
            FullscreenActivity.mSet = FullscreenActivity.mySet.split("%%%");
            FullscreenActivity.mSetList = FullscreenActivity.mSet;

            // Restore the set back to what it was
            FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$%%%$**_", "_**$$**_");

            FullscreenActivity.setSize = FullscreenActivity.mSetList.length;

            // Get rid of tags before and after folder/filenames
            for (int x = 0; x < FullscreenActivity.mSetList.length; x++) {
                FullscreenActivity.mSetList[x] = FullscreenActivity.mSetList[x]
                        .replace("$**_", "");
                FullscreenActivity.mSetList[x] = FullscreenActivity.mSetList[x]
                        .replace("_**$", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void loadASet(Context c) throws XmlPullParserException, IOException {

        FullscreenActivity.mySetXML = null;
        FullscreenActivity.mySetXML = "";
        FullscreenActivity.myParsedSet = null;

        // Reset any current set
        FullscreenActivity.mySet = null;
        FullscreenActivity.mySet = "";

        // Test if file exists - the settoload is the link clicked so is still the set name
        FullscreenActivity.setfile = new File(FullscreenActivity.dirsets + "/" + FullscreenActivity.settoload);
        if (!FullscreenActivity.setfile.exists()) {
            return;
        }

        FullscreenActivity.lastSetName = FullscreenActivity.settoload;

        // Try the new, improved method of loading in a set
        // First up, try to get the encoding of the set file
        String utf = LoadXML.getUTFEncoding(FullscreenActivity.setfile, c);

        // Now we know the encoding, iterate through the file extracting the items as we go
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        xpp = factory.newPullParser();
        InputStream inputStream = new FileInputStream(FullscreenActivity.setfile);
        xpp.setInput(inputStream, utf);

        int eventType;
        if (PopUpListSetsFragment.dataTask!=null) {

            eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("slide_group")) {
                        // Is this a song?
                        switch (xpp.getAttributeValue(null, "type")) {
                            case "song":
                                // Get song
                                getSong();
                                break;
                            case "scripture":
                                // Get Scripture
                                getScripture(c);
                                break;
                            case "custom":
                                // Get Custom (Note or slide or variation)
                                getCustom(c);
                                break;
                            case "image":
                                // Get the Image(s)
                                getImage(c);
                                break;
                        }
                    }
                }
                eventType = xpp.next();
            }
        }
        // Save the loaded set contents so we can compare to the current set to see if it has changed
        // On the set list popup it will compare these and display unsaved if it is different.
        FullscreenActivity.lastLoadedSetContent = FullscreenActivity.mySet;

        // Save the preferences
        Preferences.savePreferences();
    }

    static void indexSongInSet() {

        // Initialise variables if they are null
        if (FullscreenActivity.mSetList==null) {
            FullscreenActivity.mSetList = new String[0];
        }

        if (FullscreenActivity.mSet==null) {
            FullscreenActivity.mSet = new String[0];
        }

        if (FullscreenActivity.whatsongforsetwork==null) {
            FullscreenActivity.whatsongforsetwork = "";
        }
        // See if we are already there!
        boolean alreadythere = false;
        if (FullscreenActivity.indexSongInSet>-1 && FullscreenActivity.mSetList!=null &&
                FullscreenActivity.indexSongInSet<FullscreenActivity.mSetList.length) {
            if (FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet].contains(FullscreenActivity.songfilename)) {
                alreadythere = true;
            }
        }

        if (FullscreenActivity.mSetList!=null) {
            FullscreenActivity.setSize = FullscreenActivity.mSetList.length;
        } else {
            FullscreenActivity.setSize = 0;
        }

        if (alreadythere) {
            if (FullscreenActivity.indexSongInSet>0) {
                FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet-1];
            } else {
                FullscreenActivity.previousSongInSet = "";
            }
            if (FullscreenActivity.indexSongInSet<FullscreenActivity.mSetList.length-1) {
                FullscreenActivity.nextSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet+1];
            } else {
                FullscreenActivity.nextSongInSet = "";
            }
        } else {
            FullscreenActivity.previousSongInSet = "";
            FullscreenActivity.nextSongInSet = "";
        }

        // Go backwards through the setlist - this finishes with the first occurrence
        // Useful for duplicate items, otherwise it returns the last occurrence
        // Not yet tested, so left

        FullscreenActivity.mSet = FullscreenActivity.mSetList;

        if (!alreadythere) {
            for (int x = 0; x < FullscreenActivity.setSize; x++) {

                if (FullscreenActivity.mSet!=null && FullscreenActivity.mSet.length>=x &&
                        FullscreenActivity.mSet[x]!=null && FullscreenActivity.whatsongforsetwork!=null &&
                        (FullscreenActivity.mSet[x].equals(FullscreenActivity.whatsongforsetwork) ||
                        FullscreenActivity.mSet[x].equals("**" + FullscreenActivity.whatsongforsetwork))) {

                    FullscreenActivity.indexSongInSet = x;
                    if (x > 0) {
                        FullscreenActivity.previousSongInSet = FullscreenActivity.mSet[x - 1];
                    }
                    if (x != FullscreenActivity.setSize - 1) {
                        FullscreenActivity.nextSongInSet = FullscreenActivity.mSet[x + 1];
                    }

                }
            }
        }
    }

    static void songIndexClickInSet() {
        if (FullscreenActivity.indexSongInSet == 0) {
            // Already first item
            FullscreenActivity.previousSongInSet = "";
        } else {
            FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet - 1];
        }

        if (FullscreenActivity.indexSongInSet == (FullscreenActivity.setSize - 1)) {
            // Last item
            FullscreenActivity.nextSongInSet = "";
        } else {
            FullscreenActivity.nextSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet + 1];
        }
        FullscreenActivity.whichDirection = "R2L";
    }

    static void saveSetMessage(Context c) {
        FullscreenActivity.whattodo = "";
        if (FullscreenActivity.mSetList!=null && FullscreenActivity.mSetList.length>0) {
            if (!CreateNewSet.doCreation(c)) {
                FullscreenActivity.myToastMessage = c.getString(R.string.error_notset);
            }
        } else if (FullscreenActivity.mSetList!=null) {
            FullscreenActivity.myToastMessage = c.getString(R.string.error_notset);
        }

        if (FullscreenActivity.myToastMessage.equals("yes")) {
            FullscreenActivity.myToastMessage = c.getString(R.string.set_save)
                    + " - " + c.getString(R.string.ok);
        }
    }

    static void clearSet(Context c) {
        FullscreenActivity.mySet = "";
        FullscreenActivity.mSetList = null;
        FullscreenActivity.setView = false;

        FullscreenActivity.lastSetName = "";

        // Save the new, empty, set
        Preferences.savePreferences();

        FullscreenActivity.myToastMessage = c.getString(R.string.options_set_clear) + " " +
                c.getString(R.string.ok);
    }

    static void deleteSet(Context c) {
        String[] tempsets = FullscreenActivity.setnamechosen.split("%_%");
        FullscreenActivity.myToastMessage = "";
        StringBuilder message = new StringBuilder();
        for (String tempfile:tempsets) {
            if (tempfile!=null && !tempfile.equals("") && !tempfile.isEmpty()) {
                File settodelete = new File(FullscreenActivity.dirsets + "/" + tempfile);
                if (settodelete.delete()) {
                     message.append(tempfile).append(", ");
                }
            }
        }
        FullscreenActivity.myToastMessage = message.toString();
        if (FullscreenActivity.myToastMessage.length()>2) {
            FullscreenActivity.myToastMessage = FullscreenActivity.myToastMessage.substring(0, FullscreenActivity.myToastMessage.length() - 2);
        }
        FullscreenActivity.myToastMessage = FullscreenActivity.myToastMessage + " " + c.getString(R.string.sethasbeendeleted);
    }

    static void getSongForSetWork(Context c) {
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Scripture/_cache")) {
            FullscreenActivity.whatsongforsetwork = c.getResources().getString(R.string.scripture) + "/" + FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Slides/_cache")) {
            FullscreenActivity.whatsongforsetwork = c.getResources().getString(R.string.slide) + "/" + FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Notes/_cache")) {
            FullscreenActivity.whatsongforsetwork = c.getResources().getString(R.string.note) + "/" + FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Images/_cache")) {
            FullscreenActivity.whatsongforsetwork = c.getResources().getString(R.string.image) + "/" + FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Variations")) {
            FullscreenActivity.whatsongforsetwork = c.getResources().getString(R.string.variation) + "/" + FullscreenActivity.songfilename;
        } else {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.whichSongFolder + "/"
                    + FullscreenActivity.songfilename;
        }
    }

    static boolean isSongInSet(Context c) {
        if (FullscreenActivity.setSize > 0) {
            // Get the name of the song to look for (including folders if need be)
            getSongForSetWork(c);

            if (FullscreenActivity.setView && FullscreenActivity.mySet.contains(FullscreenActivity.whatsongforsetwork)) {
                // If we are currently in set mode, check if the new song is there, in which case do nothing else
                FullscreenActivity.setView = true;
                indexSongInSet();
                return true;

            } else if (FullscreenActivity.setView && !FullscreenActivity.mySet.contains(FullscreenActivity.whatsongforsetwork)) {
                // If we are currently in set mode, but the new song isn't there, leave set mode
                FullscreenActivity.setView = false;
                FullscreenActivity.previousSongInSet = "";
                FullscreenActivity.nextSongInSet = "";
                FullscreenActivity.indexSongInSet = 0;
                return false;

            } else if (!FullscreenActivity.setView && FullscreenActivity.mySet.contains(FullscreenActivity.whatsongforsetwork)) {
                // If we aren't currently in set mode and the new song is there, enter set mode and get the index
                FullscreenActivity.setView = true;
                FullscreenActivity.previousSongInSet = "";
                FullscreenActivity.nextSongInSet = "";

                // Get the song index
                indexSongInSet();
                return true;

            } else if (!FullscreenActivity.mySet.contains(FullscreenActivity.whatsongforsetwork)) {
                // The new song isn't in the set, so leave set mode and reset index
                FullscreenActivity.setView = false;
                FullscreenActivity.previousSongInSet = "";
                FullscreenActivity.nextSongInSet = "";
                FullscreenActivity.indexSongInSet = 0;
                return false;
            }

        } else {
            // User wasn't in set view, or the set was empty
            // Switch off the set view (buttons in action bar)
            FullscreenActivity.setView = false;
            FullscreenActivity.previousSongInSet = "";
            FullscreenActivity.nextSongInSet = "";
            FullscreenActivity.indexSongInSet = 0;
            return false;
        }
        return false;
    }

    static void checkDirectories() {
        // Check the Scripture _cache Directory exists
        boolean check_action;
        if (FullscreenActivity.dirscriptureverses.exists()) {
            // Scripture folder exists, do nothing other than clear it!
            for (File scripfile : FullscreenActivity.dirscriptureverses.listFiles()) {
                check_action = scripfile.delete();
                if (!check_action) {
                    Log.d("d","Problem clearing scripture file");
                }
            }
        } else {
            // Tell the user we're creating the Scripture _cache directory
            check_action = FullscreenActivity.dirscriptureverses.mkdirs();
            if (!check_action) {
                Log.d("d","Problem creating scripture folder");
            }
        }

        // Check the Slides _cache Directory exists
        if (FullscreenActivity.dircustomslides.exists()) {
            // Slides folder exists, do nothing other than clear it!
            for (File slidesfile : FullscreenActivity.dircustomslides.listFiles()) {
                check_action = slidesfile.delete();
                if (!check_action) {
                    Log.d("d","Problem clearing custom slides file");
                }
            }
        } else {
            // Tell the user we're creating the Slides _cache directory
            check_action = FullscreenActivity.dircustomslides.mkdirs();
            if (!check_action) {
                Log.d("d","Problem creating custom slides folder");
            }
        }

        // Check the Notes _cache Directory exists
        if (FullscreenActivity.dircustomnotes.exists()) {
            // Slides folder exists, do nothing other than clear it!
            for (File notesfile : FullscreenActivity.dircustomnotes.listFiles()) {
                check_action = notesfile.delete();
                if (!check_action) {
                    Log.d("d","Problem clearing custom notes file");
                }
            }
        } else {
            // Tell the user we're creating the Notes _cache directory
            check_action = FullscreenActivity.dircustomnotes.mkdirs();
            if (!check_action) {
                Log.d("d","Problem creating custom notes folder");
            }
        }

        // Check the Images _cache Directory exists
        if (FullscreenActivity.dircustomimages.exists()) {
            // Images folder exists, do nothing other than clear it!
            for (File imagesfile : FullscreenActivity.dircustomimages.listFiles()) {
                check_action = imagesfile.delete();
                if (!check_action) {
                    Log.d("d","Problem clearing image file");
                }
            }
        } else {
            // Tell the user we're creating the Slides _cache directory
            check_action = FullscreenActivity.dircustomimages.mkdirs();
            if (!check_action) {
                Log.d("d","Problem creating custom image folder");
            }
        }

        // Check the Variations Directory exists
        if (FullscreenActivity.dirvariations.exists()) {
            // Variations folder exists, do nothing other than clear it!
            for (File variationsfile : FullscreenActivity.dirvariations.listFiles()) {
                check_action = variationsfile.delete();
                if (!check_action) {
                    Log.d("d","Problem clearing variation file");
                }
            }
        } else {
            // Tell the user we're creating the Variations directory
            check_action = FullscreenActivity.dirvariations.mkdirs();
            if (!check_action) {
                Log.d("d","Problem creating variation folder");
            }
        }

    }

    private static void writeTempSlide(String where, String what, Context c) throws IOException {
        // Fix the custom name so there are no illegal characters
        what = what.replaceAll("[|?*<\":>+\\[\\]']", " ");
        File temp;
        String set_item;
        String foldername;
        String setprefix;

        if (where.equals(c.getResources().getString(R.string.scripture))) {
            foldername = FullscreenActivity.dirscriptureverses.toString();
            setprefix  = "$**_**" + c.getResources().getString(R.string.scripture) + "/";
        } else if (where.equals(c.getResources().getString(R.string.slide))) {
            foldername = FullscreenActivity.dircustomslides.toString();
            setprefix  = "$**_**" + c.getResources().getString(R.string.slide) + "/";
        } else if (where.equals(c.getResources().getString(R.string.image))) {
            foldername = FullscreenActivity.dircustomimages.toString();
            setprefix  = "$**_**" + c.getResources().getString(R.string.image) + "/";
        } else if (where.equals(c.getResources().getString(R.string.variation))) {
            foldername = FullscreenActivity.dirvariations.toString();
            setprefix  = "$**_**" + c.getResources().getString(R.string.variation) + "/";
        } else {
            foldername = FullscreenActivity.dircustomnotes.toString();
            setprefix  = "$**_**" + c.getResources().getString(R.string.note) + "/";
        }

        // Check to see if that file already exists (same name).  If so, add _ to the end
        temp = new File(foldername + "/" + what);
        StringBuilder whatBuilder = new StringBuilder(what);
        while (temp.exists()) {
            whatBuilder.append("_");
            temp = new File(foldername + "/" + whatBuilder);
        }
        what = whatBuilder.toString();

        set_item = setprefix + what + "_**$";

        FileOutputStream overWrite = new FileOutputStream(temp, false);
        // Prepare the new XML file
        String my_NEW_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        my_NEW_XML += "<song>\n";
        my_NEW_XML += "  <title>" + PopUpEditSongFragment.parseToHTMLEntities(title) + "</title>\n";
        my_NEW_XML += "  <author>" + PopUpEditSongFragment.parseToHTMLEntities(author) + "</author>\n";
        my_NEW_XML += "  <user1>" + PopUpEditSongFragment.parseToHTMLEntities(user1) + "</user1>\n";
        my_NEW_XML += "  <user2>" + PopUpEditSongFragment.parseToHTMLEntities(user2) + "</user2>\n";
        my_NEW_XML += "  <user3>" + PopUpEditSongFragment.parseToHTMLEntities(user3) + "</user3>\n";
        my_NEW_XML += "  <aka>" + PopUpEditSongFragment.parseToHTMLEntities(aka) + "</aka>\n";
        my_NEW_XML += "  <key_line>" + PopUpEditSongFragment.parseToHTMLEntities(key_line) + "</key_line>\n";
        my_NEW_XML += "  <hymn_number>" + PopUpEditSongFragment.parseToHTMLEntities(hymn_number) + "</hymn_number>\n";
        my_NEW_XML += "  <lyrics>" + PopUpEditSongFragment.parseToHTMLEntities(lyrics) + "</lyrics>\n";
        my_NEW_XML += "</song>";

        if (where.equals(c.getResources().getString(R.string.variation))) {
            // Create a full song instead
            byte[] data = Base64.decode(custom_notes, Base64.DEFAULT);
            my_NEW_XML = new String(data, "UTF-8");
        }

        overWrite.write(my_NEW_XML.getBytes());
        overWrite.flush();
        overWrite.close();
        FullscreenActivity.mySet = FullscreenActivity.mySet + set_item;
    }

    private static void getSong() {
        try {
            // Get path and remove leading /
            String p_name = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"path"));
            String s_name = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name"));
            if (p_name.startsWith("/")) {
                p_name = p_name.replaceFirst("/","");
            }
            if (p_name.endsWith("/")) {
                p_name = p_name.substring(0,p_name.length()-1);
            }
            if (s_name.startsWith("/")) {
                s_name = s_name.replaceFirst("/","");
            }
            if (s_name.endsWith("/")) {
                s_name = s_name.substring(0,s_name.length()-1);
            }
            String location = p_name + "/" + s_name;
            if (location.startsWith("/")) {
                location = location.replaceFirst("/","");
            }
            if (location.endsWith("/")) {
                location = location.substring(0,location.length()-1);
            }

            FullscreenActivity.mySet = FullscreenActivity.mySet + "$**_" + location + "_**$";
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            xpp.nextTag();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void getScripture(Context c) throws IOException, XmlPullParserException {
        // Ok parse this bit seperately.  Initialise the values
        String scripture_title = "";
        String scripture_translation = "";
        StringBuilder scripture_text = new StringBuilder();
        String scripture_seconds = xpp.getAttributeValue(null, "seconds");
        String scripture_loop = xpp.getAttributeValue(null, "loop");

        boolean scripture_finished = false;
        while (!scripture_finished) {
            switch (xpp.getName()) {
                case "title":
                    scripture_title = xpp.nextText();
                    break;
                case "body":
                    scripture_text.append("\n[]\n").append(LoadXML.parseFromHTMLEntities(xpp.nextText()));
                    break;
                case "subtitle":
                    scripture_translation = LoadXML.parseFromHTMLEntities(xpp.nextText());
                    break;
            }

            xpp.nextTag();

            if (xpp.getEventType()==XmlPullParser.END_TAG) {
                if (xpp.getName().equals("slides")) {
                    scripture_finished = true;
                }
            }
        }

        // Create a new file for each of these entries.  Filename is title with Scripture/

        // Break the scripture_text up into small manageable chunks
        // First up, start each new verse on a new line
        //Replace all spaces (split points) with \n
        scripture_text = new StringBuilder(scripture_text.toString().replace(" ", "\n"));
        scripture_text = new StringBuilder(scripture_text.toString().replace("---", "[]"));
        //Split the verses up into an array by new lines - array of words
        String[] temp_text = scripture_text.toString().split("\n");

        //String[] add_text = new String[800];
        //int array_line = 0;

        //Add all the array back together and make sure no line goes above 40 characters
        ArrayList<String> vlines = new ArrayList<>();
        StringBuilder currline = new StringBuilder();
        for (String words : temp_text) {
            int check = currline.length();
            if (check>40 || words.contains("[]")) {
                if (words.contains("[]")) {
                    // This is a new section
                    vlines.add(currline.toString());
                    vlines.add("[]\n");
                    currline = new StringBuilder();
                } else {
                    vlines.add(currline.toString());
                    currline = new StringBuilder(" " + words);
                }
            } else {
                currline.append(" ").append(words);
            }
        }
        vlines.add(currline.toString());

        scripture_text = new StringBuilder();

        // Ok go back through the array and add the non-empty lines back up
        for (int i=0; i<vlines.size();i++) {
            String s = vlines.get(i);
            if (s != null && !s.equals("")) {
                scripture_text.append("\n").append(s);
            }
        }


        /*// Ok go back through the array and add the non-empty lines back up
        for (String anAdd_text : add_text) {
            if (anAdd_text != null && !anAdd_text.equals("")) {
                if (anAdd_text.contains("[]")) {
                    scripture_text = scripture_text + "\n" + anAdd_text;
                } else {
                    scripture_text = scripture_text + "\n " + anAdd_text;
                }
            }
        }*/

/*        for (String aTemp_text : temp_text) {
            if (add_text[array_line] == null) {
                add_text[array_line] = "";
            }

            int check;
            check = add_text[array_line].length();
            if (check > 40 || aTemp_text.contains("[]")) {
                array_line++;
                if (aTemp_text.contains("[]")) {
                    add_text[array_line] = "[]\n ";
                } else {
                    add_text[array_line] = " " + aTemp_text;
                }
            } else {
                add_text[array_line] = add_text[array_line] + " " + aTemp_text;
            }
        }*/


        while (scripture_text.toString().contains("\\n\\n")) {
            scripture_text = new StringBuilder(scripture_text.toString().replace("\\n\\n", "\\n"));
        }

        title = scripture_title;
        author = scripture_translation;
        user1 = scripture_seconds;
        user2 = scripture_loop;
        user3 = "";
        lyrics = scripture_text.toString().trim();
        aka = "";
        key_line = "";
        hymn_number = "";

        writeTempSlide(c.getResources().getString(R.string.scripture), scripture_title,c);

        xpp.nextTag();
     }

    private static void getCustom(Context c) throws IOException, XmlPullParserException {
        // Ok parse this bit seperately.  Could be a note or a slide or a variation
        // Notes have # Note # - in the name
        // Variations have # Variation # - in the name
        String custom_name = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "name"));
        String custom_seconds = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "seconds"));
        String custom_loop = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "loop"));
        String custom_title = "";
        String custom_subtitle = "";
        custom_notes = "";
        StringBuilder custom_text = new StringBuilder();

        boolean custom_finished = false;
        while (!custom_finished) {
            switch (xpp.getName()) {
                case "title":
                    custom_title = LoadXML.parseFromHTMLEntities(xpp.nextText());
                    break;
                case "notes":
                    custom_notes = LoadXML.parseFromHTMLEntities(xpp.nextText());
                    break;
                case "body":
                    custom_text.append("\n---\n").append(LoadXML.parseFromHTMLEntities(xpp.nextText()));
                    break;
                case "subtitle":
                    custom_subtitle = LoadXML.parseFromHTMLEntities(xpp.nextText());
                    break;
            }

            xpp.nextTag();

            if (xpp.getEventType()==XmlPullParser.END_TAG) {
                if (xpp.getName().equals("slides")) {
                    custom_finished = true;
                }
            }
        }

        // Remove first ---
        if (custom_text.indexOf("\n---\n") == 0) {
            custom_text = new StringBuilder(custom_text.toString().replaceFirst("\n---\n", ""));
        }

        // If the custom slide is just a note (no content), fix the content
        String noteorslide = c.getResources().getString(R.string.slide);
        if (custom_name.contains("# " + c.getResources().getString(R.string.note) + " # - ")) {
            // Prepare for a note
            custom_name = custom_name.replace("# " + c.getResources().getString(R.string.note) + " # - ", "");
            custom_text = new StringBuilder(custom_notes);
            if (custom_notes.equals("")) {
                custom_text = new StringBuilder(custom_name);
            }
            custom_notes = "";
            custom_title = "";
            custom_subtitle = "";
            custom_seconds = "";
            noteorslide = c.getResources().getString(R.string.note);

        // If it is a song variation, the full song contents are written to the notes part
        // The contents will be a compatible slide for OpenSong desktop presentation, not needed in this app
        } else if (custom_name.contains("# " + c.getResources().getString(R.string.variation) + " # - ")) {
            // Prepare for a variation
            custom_name = custom_name.replace("# " + c.getResources().getString(R.string.variation) + " # - ", "");
            custom_text = new StringBuilder(custom_notes);
            custom_title = custom_name;
            custom_subtitle = "";
            custom_seconds = "";
            noteorslide = c.getResources().getString(R.string.variation);
        }

        title = custom_title;
        author = custom_subtitle;
        user1 = custom_seconds;
        user2 = custom_loop;
        user3 = "";
        aka = custom_name;
        key_line = custom_notes;
        lyrics = custom_text.toString();
        hymn_number = "";

        writeTempSlide(noteorslide, custom_name,c);

    }

    private static void getImage(Context c) throws IOException, XmlPullParserException {
        // Ok parse this bit separately.  This could have multiple images
        String image_name = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "name"));
        String image_seconds = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "seconds"));
        String image_loop = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "loop"));
        StringBuilder image_title = new StringBuilder();
        String image_subtitle = "";
        StringBuilder slide_images;
        StringBuilder slide_image_titles;
        String image_notes = "";
        String image_filename;
        StringBuilder hymn_number_imagecode = new StringBuilder();
        key_line = "";
        int imagenums = 0;

        // Work through the xml tags until we reach the end of the image slide
        // The end will be when we get to </slide_group>

        int eventType = xpp.getEventType();
        boolean allimagesdone = false;
        String image_content = "";
        String image_type;
        slide_images = new StringBuilder();
        slide_image_titles = new StringBuilder();

        while (!allimagesdone) { // Keep iterating unless the current eventType is the end of the document
            if(eventType == XmlPullParser.START_TAG) {
                switch (xpp.getName()) {
                    case "title":
                        image_title = new StringBuilder(LoadXML.parseFromHTMLEntities(xpp.nextText()));

                        break;
                    case "subtitle":
                        image_subtitle = LoadXML.parseFromHTMLEntities(xpp.nextText());

                        break;
                    case "notes":
                        image_notes = LoadXML.parseFromHTMLEntities(xpp.nextText());

                        break;
                    case "filename":
                        image_filename = LoadXML.parseFromHTMLEntities(xpp.nextText());
                        if (image_filename != null && !image_filename.equals("") && !image_filename.isEmpty()) {
                            slide_images.append(image_filename).append("\n");
                            slide_image_titles.append("[").append(c.getResources().getString(R.string.image))
                                    .append("_").append(imagenums + 1).append("]\n").append(image_filename)
                                    .append("\n\n");
                            imagenums++;
                            encodedimage = false;
                        }

                        break;
                    case "description":
                        String file_name = LoadXML.parseFromHTMLEntities(xpp.nextText());
                        if (file_name.contains(".png") || file_name.contains(".PNG")) {
                            image_type = ".png";
                        } else if (file_name.contains(".gif") || file_name.contains(".GIF")) {
                            image_type = ".gif";
                        } else {
                            image_type = ".jpg";
                        }

                        if (encodedimage) {
                            // Save this image content
                            // Need to see if the image already exists
                            if (image_title.toString().equals("")) {
                                image_title = new StringBuilder(c.getResources().getString(R.string.image));
                            }

                            File imgfile = new File(FullscreenActivity.dircustomimages + "/" + image_title + imagenums + image_type);
                            while (imgfile.exists()) {

                                image_title.append("_");
                                imgfile = new File(FullscreenActivity.dircustomimages + "/" +
                                        image_title + imagenums + image_type);
                            }
                            FileOutputStream overWrite = new FileOutputStream(imgfile, false);
                            byte[] decodedString = Base64.decode(image_content, Base64.DEFAULT);
                            overWrite.write(decodedString);
                            overWrite.flush();
                            overWrite.close();
                            image_content = "";
                            slide_images.append(imgfile.toString()).append("\n");
                            slide_image_titles.append("[").append(c.getResources().getString(R.string.image))
                                    .append("_").append(imagenums + 1).append("]\n").append(imgfile).append("\n\n");
                            imagenums++;
                            encodedimage = false;
                        }

                        break;
                    case "image":
                        image_content = xpp.nextText();
                        hymn_number_imagecode.append(image_content.trim()).append("XX_IMAGE_XX");
                        encodedimage = true;
                        break;
                }

            } else if(eventType == XmlPullParser.END_TAG) {
                if (xpp.getName().equals("slide_group")) {
                    allimagesdone = true;
                }
            }

            eventType = xpp.next(); // Set the current event type from the return value of next()
        }

        if (image_title.toString().equals("")) {
            image_title = new StringBuilder(c.getResources().getString(R.string.image));
        }

        if (image_subtitle ==null) {
            image_subtitle = "";
        }

        if (image_seconds ==null) {
            image_seconds = "";
        }

        if (image_loop ==null) {
            image_loop = "";
        }

        if (image_name ==null) {
            image_name = "";
        }

        if (image_notes ==null) {
            image_notes = "";
        }

        title = image_title.toString();
        author = image_subtitle;
        user1 = image_seconds;
        user2 = image_loop;
        user3 = slide_images.toString().trim();
        aka = image_name;
        hymn_number = hymn_number_imagecode.toString();
        key_line = image_notes;
        lyrics = slide_image_titles.toString().trim();
        writeTempSlide(c.getResources().getString(R.string.image),title,c);
    }

    static void prepareFirstItem(Context c) {
        // If we have just loaded a set, and it isn't empty,  load the first item
        if (FullscreenActivity.mSetList.length>0) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.mSetList[0];
            FullscreenActivity.setView = true;

            FullscreenActivity.linkclicked = FullscreenActivity.mSetList[0];
            FullscreenActivity.pdfPageCurrent = 0;

            // Get the song and folder names from the item clicked in the set list
            getSongFileAndFolder(c);

            // Match the song folder
            ListSongFiles.getAllSongFiles();

            // Get the index of the song in the current set
            SetActions.indexSongInSet();
        }
    }

    static void getSongFileAndFolder(Context c) {
        if (!FullscreenActivity.linkclicked.contains("/")) {
            FullscreenActivity.linkclicked = "/" + FullscreenActivity.linkclicked;
        }

        if (FullscreenActivity.linkclicked==null || FullscreenActivity.linkclicked.equals("/")) {
            // There was no song clicked, so just reload the current one
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                FullscreenActivity.linkclicked = "/"+FullscreenActivity.songfilename;
            } else {
                FullscreenActivity.linkclicked = FullscreenActivity.whichSongFolder + "/" +
                        FullscreenActivity.songfilename;
            }
        }

        // The song is the bit after the last /
        int songpos = FullscreenActivity.linkclicked.lastIndexOf("/");
        if (songpos==0) {
            // Empty folder
            FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
        } else {
            FullscreenActivity.whichSongFolder = FullscreenActivity.linkclicked.substring(0,songpos);
        }

        if (songpos>=FullscreenActivity.linkclicked.length()) {
            // Empty song
            FullscreenActivity.songfilename = "";
        } else {
            FullscreenActivity.songfilename = FullscreenActivity.linkclicked.substring(songpos + 1);
        }

        if (FullscreenActivity.whichSongFolder!=null && FullscreenActivity.whichSongFolder.equals("")) {
            FullscreenActivity.whichSongFolder = c.getResources().getString(R.string.mainfoldername);
        }

        // If the folder length isn't 0, it is a folder
        if (FullscreenActivity.whichSongFolder.length() > 0 &&
                FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.scripture)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.variation)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.image)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.slide)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.note))) {
            FullscreenActivity.whichSongFolder = "../Scripture/_cache";

        } else if (FullscreenActivity.whichSongFolder.length() > 0 &&
                FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.slide)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.variation)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.image)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.note)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.scripture))) {
            FullscreenActivity.whichSongFolder = "../Slides/_cache";

        } else if (FullscreenActivity.whichSongFolder.length() > 0 &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.variation)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.slide)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.image)) &&
                FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.note)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.scripture))) {
            FullscreenActivity.whichSongFolder = "../Notes/_cache";

        } else if (FullscreenActivity.whichSongFolder.length() > 0 &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.variation)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.slide)) &&
                FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.image)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.note)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.scripture))) {
            FullscreenActivity.whichSongFolder = "../Images/_cache";

        } else if (FullscreenActivity.whichSongFolder.length() > 0 &&
                FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.variation)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.slide)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.image)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.note)) &&
                !FullscreenActivity.whichSongFolder.contains("**"+c.getResources().getString(R.string.scripture))) {
            FullscreenActivity.whichSongFolder = "../Variations";

        }
    }

    static void doMoveInSet(Context c) {
        mListener = (MyInterface) c;

        boolean justmovingsections = false;

        // If we are in Stage Mode, check the sections first
        if (FullscreenActivity.whichMode.equals("Stage")) {

            // Might be staying on the same song but moving section
            if (FullscreenActivity.setMoveDirection.equals("back")) {
                if (FullscreenActivity.currentSection > 0) {
                    justmovingsections = true;
                    mListener.doMoveSection();
                } else {
                    FullscreenActivity.currentSection = 0;
                }
            } else if (FullscreenActivity.setMoveDirection.equals("forward")) {
                if (FullscreenActivity.currentSection<FullscreenActivity.songSections.length-1) {
                    justmovingsections = true;
                    mListener.doMoveSection();
                } else {
                    FullscreenActivity.currentSection = 0;
                }
            }
        }

        if (!justmovingsections) {
            // Moving to a different song
            if (FullscreenActivity.setMoveDirection.equals("back")) {
                if (FullscreenActivity.indexSongInSet>0) {
                    FullscreenActivity.indexSongInSet -= 1;
                    FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
                    FullscreenActivity.whatsongforsetwork = FullscreenActivity.linkclicked;
                    if (FullscreenActivity.linkclicked == null) {
                        FullscreenActivity.linkclicked = "";
                        FullscreenActivity.whatsongforsetwork = "";
                    }
                }

            } else if (FullscreenActivity.setMoveDirection.equals("forward")) {
                if (FullscreenActivity.indexSongInSet<FullscreenActivity.mSetList.length-1) {
                    FullscreenActivity.indexSongInSet += 1;
                    FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
                    FullscreenActivity.whatsongforsetwork = FullscreenActivity.linkclicked;
                    if (FullscreenActivity.linkclicked == null) {
                        FullscreenActivity.linkclicked = "";
                        FullscreenActivity.whatsongforsetwork = "";
                    }
                }
            }

            FullscreenActivity.setMoveDirection = "";

            // Get the song and folder names from the item clicked in the set list
            getSongFileAndFolder(c);

            // Save the preferences
            Preferences.savePreferences();

            // Match the song folder
            ListSongFiles.getAllSongFiles();

            FullscreenActivity.setMoveDirection = "";
            mListener.loadSong();
        }

    }

}