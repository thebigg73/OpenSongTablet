package com.garethevans.church.opensongtablet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.app.Activity;
import android.util.Base64;
import android.util.Log;

public class SetActions extends Activity {

    static boolean check_action;
    static FileInputStream inputStream;
    static InputStreamReader streamReader;
    static BufferedReader bufferedReader;
    static String scripture_title;
    static String scripture_translation;
    static String scripture_text;
    static String scripture_seconds;
    static String scripture_loop;
    static String custom_name;
    static String custom_title;
    static String custom_subtitle;
    static String custom_seconds;
    static String custom_loop;
    static String custom_notes;
    static String custom_text;
    static String image_name;
    static String image_title;
    static String image_subtitle;
    static String image_seconds;
    static String image_loop;
    static String image_notes;
    static String image_filename;
    static String slide_images;
    static String slide_image_titles;
    static boolean encodedimage;
    static String title = "";
    static String author = "";
    static String user1 = "";
    static String user2 = "";
    static String user3 = "";
    static String lyrics = "";
    static String aka = "";
    static String key_line = "";
    static String hymn_number = "";
    static XmlPullParserFactory factory;
    static XmlPullParser xpp;

    public static void updateOptionListSets() {
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

    public static void prepareSetList() {

        FullscreenActivity.mSet = null;
        FullscreenActivity.mSetList = null;

        // Remove any blank set entries that shouldn't be there
        FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**__**$", "");

        // Add a delimiter between songs
        FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$$**_",	"_**$%%%$**_");

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
    }

    public static void loadASet() throws XmlPullParserException, IOException {

        FullscreenActivity.mySetXML = null;
        FullscreenActivity.mySetXML = "";
        FullscreenActivity.myParsedSet = null;

        // Test if file exists - the settoload is the link clicked so is still the set name
        FullscreenActivity.setfile = new File(FullscreenActivity.dirsets + "/" + FullscreenActivity.settoload);
        if (!FullscreenActivity.setfile.exists()) {
            return;
        }

        try {
            loadSetIn();
        } catch (Exception e) {
            // file doesn't exist
        }

        PopUpListSetsFragment.val = FullscreenActivity.set_processing + "...";
        PopUpListSetsFragment.mHandler.post(PopUpListSetsFragment.runnable);

        // Ok parse the set XML file and extract the stuff needed (slides and bible verses to be kept)
        // Stuff will be saved in mySet string.
        // Songs identified by $**_XXXX_**$
        // Slide contents identified by $**_Slide/XXXX_**$
        // Note contents identified by $**_Note/XXXX_**$
        // Scripture contents identified by $**_Scripture/XXXX_**$
        // Image contents identified by $**_Image/XXXX_**$

        // Reset any current set
        FullscreenActivity.mySet = null;
        FullscreenActivity.mySet = "";

        factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        xpp = factory.newPullParser();

        if (PopUpListSetsFragment.dataTask!=null && !PopUpListSetsFragment.dataTask.isCancelled()) {
            xpp.setInput(new StringReader(FullscreenActivity.mySetXML));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("slide_group")) {
                        // Is this a song?
                        if (xpp.getAttributeValue(null, "type").equals("song")) {
                            // Get song
                            getSong();
                        } else if (xpp.getAttributeValue(null, "type").equals("scripture")) {
                            // Get Scripture
                            getScripture();
                        } else if (xpp.getAttributeValue(null, "type").equals("custom")) {
                            // Get Custom (Note or slide)
                            getCustom();
                        } else if (xpp.getAttributeValue(null, "type").equals("image")) {
                            // Get the Image(s)
                            getImage();
                        }
                    }
                }
                eventType = xpp.next();
            }
        }
    }

    public static void indexSongInSet() {
        FullscreenActivity.setSize = FullscreenActivity.mSetList.length;
        FullscreenActivity.previousSongInSet = "";
        FullscreenActivity.nextSongInSet = "";

        // Go backwards through the setlist - this finishes with the first occurrence
        // Useful for duplicate items, otherwise it returns the last occurrence
        // Not yet tested, so left
        for (int x = 0; x < FullscreenActivity.setSize; x++) {
//		for (int x = FullscreenActivity.setSize-1; x<1; x--) {
            if (FullscreenActivity.mSet[x].equals(FullscreenActivity.whatsongforsetwork)) {
                FullscreenActivity.indexSongInSet = x;
                if (x>0) {
                    FullscreenActivity.previousSongInSet = FullscreenActivity.mSet[x - 1];
                }
                if (x != FullscreenActivity.setSize - 1) {
                    FullscreenActivity.nextSongInSet = FullscreenActivity.mSet[x + 1];
                }
            }
        }
    }

    public static void getSongForSetWork() {
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../OpenSong Scripture/_cache")) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.scripture + "/" + FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Slides/_cache")) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.slide + "/" + FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Notes/_cache")) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.note + "/" + FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Images/_cache")) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.image + "/" + FullscreenActivity.songfilename;
        } else {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.whichSongFolder + "/"
                    + FullscreenActivity.songfilename;
        }
    }

    public static boolean isSongInSet() {
        if (FullscreenActivity.setSize > 0) {
            // Get the name of the song to look for (including folders if need be)
            getSongForSetWork();

            if (FullscreenActivity.mySet.contains(FullscreenActivity.whatsongforsetwork)) {
                // Song is in current set.  Find the song position in the current set and load it (and next/prev)
                // The first song has an index of 6 (the 7th item as the rest are menu items)
                FullscreenActivity.setView = "Y";
                FullscreenActivity.previousSongInSet = "";
                FullscreenActivity.nextSongInSet = "";

                // Get the song index
                indexSongInSet();
                return true;

            } else {
                // Song isn't in the set, so just show the song
                // Switch off the set view (buttons in action bar)
                FullscreenActivity.setView = "N";
                return false;
            }
        } else {
            // User wasn't in set view, or the set was empty
            // Switch off the set view (buttons in action bar)
            FullscreenActivity.setView = "N";
            return false;
        }
    }

    public static void checkDirectories() {
        // Check the OpenSong Scripture _cache Directory exists
        if (FullscreenActivity.dirbibleverses.exists()) {
            // Scripture folder exists, do nothing other than clear it!
            for (File scripfile : FullscreenActivity.dirbibleverses.listFiles()) {
                check_action = scripfile.delete();
            }
        } else {
            // Tell the user we're creating the OpenSong Scripture _cache directory
            check_action = FullscreenActivity.dirbibleverses.mkdirs();
        }

        // Check the Slides _cache Directory exists
        if (FullscreenActivity.dircustomslides.exists()) {
            // Slides folder exists, do nothing other than clear it!
            for (File slidesfile : FullscreenActivity.dircustomslides.listFiles()) {
                check_action = slidesfile.delete();
            }
        } else {
            // Tell the user we're creating the Slides _cache directory
            check_action = FullscreenActivity.dircustomslides.mkdirs();
        }

        // Check the Notes _cache Directory exists
        if (FullscreenActivity.dircustomnotes.exists()) {
            // Slides folder exists, do nothing other than clear it!
            for (File notesfile : FullscreenActivity.dircustomnotes.listFiles()) {
                check_action = notesfile.delete();
            }
        } else {
            // Tell the user we're creating the Notes _cache directory
            check_action = FullscreenActivity.dircustomnotes.mkdirs();
        }

        // Check the Images _cache Directory exists
        if (FullscreenActivity.dircustomimages.exists()) {
            // Images folder exists, do nothing other than clear it!
            for (File imagesfile : FullscreenActivity.dircustomimages.listFiles()) {
                check_action = imagesfile.delete();
            }
        } else {
            // Tell the user we're creating the Slides _cache directory
            check_action = FullscreenActivity.dircustomimages.mkdirs();
        }
    }

    public static void loadSetIn() throws IOException {
        inputStream = new FileInputStream(new File(FullscreenActivity.dirsets + "/" + FullscreenActivity.settoload));

        int size = (int) FullscreenActivity.setfile.length();
        int bytesdone = 0;

        streamReader = new InputStreamReader(inputStream);
        bufferedReader = new BufferedReader(streamReader);

        String l;

        int count = 0;
        while ((l = bufferedReader.readLine()) != null  && !PopUpListSetsFragment.dataTask.isCancelled()) {
            // do what you want with the line
            if (!PopUpListSetsFragment.dataTask.isCancelled()) {
                FullscreenActivity.mySetXML = FullscreenActivity.mySetXML
                        + l + "\n";
                count = count + 1;
                bytesdone = bytesdone + l.length() + 1;
                int percentage_done;
                try {
                    percentage_done = Math.round(((float) bytesdone / (float) size) * 100);
                } catch (Exception e) {
                    percentage_done = 0;
                }
                if (percentage_done > 100) {
                    percentage_done = 100;
                }
                PopUpListSetsFragment.val = FullscreenActivity.set_loading + ": " + percentage_done + "%";
                PopUpListSetsFragment.mHandler.post(PopUpListSetsFragment.runnable);
            }
        }

        inputStream.close();
        bufferedReader.close();
    }

    public static void writeTempSlide(String where, String what) throws IOException {
        File temp = new File(FullscreenActivity.dircustomnotes + "/" + custom_name);
        String set_item = "";
        Log.d("write","where="+where);
        Log.d("write","what="+what);

        if (where.equals(FullscreenActivity.text_scripture)) {
            temp = new File(FullscreenActivity.dirbibleverses + "/" + what);
            set_item = "$**_" + FullscreenActivity.scripture + "/" + scripture_title + "_**$";
        } else if (where.equals(FullscreenActivity.text_slide)) {
            set_item = "$**_" + FullscreenActivity.slide + "/" + custom_name + "_**$";
            temp = new File(FullscreenActivity.dircustomslides + "/" + custom_name);
        } else if (where.equals(FullscreenActivity.text_note)) {
            set_item = "$**_" + FullscreenActivity.note + "/" + custom_name + "_**$";
            temp = new File(FullscreenActivity.dircustomnotes + "/" + custom_name);
        } else if (where.equals(FullscreenActivity.image)) {
            set_item = "$**_" + FullscreenActivity.image + "/" + image_name + "_**$";
            temp = new File(FullscreenActivity.dircustomimages + "/" + image_name);
        }

        FileOutputStream overWrite = new FileOutputStream(temp, false);
        // Prepare the new XML file
        String my_NEW_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        my_NEW_XML += "<song>\n";
        my_NEW_XML += "  <title>" + title + "</title>\n";
        my_NEW_XML += "  <author>" + author + "</author>\n";
        my_NEW_XML += "  <user1>" + user1 + "</user1>\n";
        my_NEW_XML += "  <user2>" + user2 + "</user2>\n";
        my_NEW_XML += "  <user3>" + user3 + "</user3>\n";
        my_NEW_XML += "  <aka>" + aka + "</aka>\n";
        my_NEW_XML += "  <key_line>" + key_line + "</key_line>\n";
        my_NEW_XML += "  <hymn_number>" + hymn_number + "</hymn_number>\n";
        my_NEW_XML += "  <lyrics>" + lyrics + "</lyrics>\n";
        my_NEW_XML += "</song>";
        overWrite.write(my_NEW_XML.getBytes());
        overWrite.flush();
        overWrite.close();
        FullscreenActivity.mySet = FullscreenActivity.mySet + set_item;
    }

    public static void getSong() throws IOException, XmlPullParserException {
        FullscreenActivity.mySet = FullscreenActivity.mySet
                + "$**_" + xpp.getAttributeValue(null,"path") + xpp.getAttributeValue(null,"name") + "_**$";
        xpp.nextTag();
    }

    public static void getScripture() throws IOException, XmlPullParserException {
        Log.d("getScripture","here");
        // Ok parse this bit seperately.  Initialise the values
        scripture_title = "";
        scripture_translation = "";
        scripture_text = "";
        scripture_seconds = xpp.getAttributeValue(null, "seconds");
        scripture_loop = xpp.getAttributeValue(null,"loop");

        boolean scripture_finished = false;
        while (!scripture_finished) {
            if (xpp.getName().equals("title")) {
                scripture_title = xpp.nextText();
            } else if (xpp.getName().equals("body")) {
                scripture_text = scripture_text + "\n[]\n" + xpp.nextText();
            } else if (xpp.getName().equals("subtitle")) {
                scripture_translation = xpp.nextText();
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
        scripture_text = scripture_text.replace(" ", "\n");

        //Split the verses up into an array by new lines - array of words
        String[] temp_text = scripture_text.split("\n");

        String[] add_text = new String[100];
        int array_line = 0;
        //Add all the array back together and make sure no line goes above 40 characters

        for (String aTemp_text : temp_text) {
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
        }

        scripture_text = "";

        // Ok go back through the array and add the non-empty lines back up
        for (String anAdd_text : add_text) {
            if (anAdd_text != null && !anAdd_text.equals("")) {
                if (anAdd_text.contains("[]")) {
                    scripture_text = scripture_text + "\n" + anAdd_text;
                } else {
                    scripture_text = scripture_text + "\n " + anAdd_text;
                }
            }
        }
        while (scripture_text.contains("\\n\\n")) {
            scripture_text = scripture_text.replace("\\n\\n", "\\n");
        }

        title = scripture_title;
        author = scripture_translation;
        user1 = scripture_seconds;
        user2 = scripture_loop;
        user3 = "";
        lyrics = scripture_text.trim();
        aka = "";
        key_line = "";
        hymn_number = "";

        writeTempSlide("Scripture",scripture_title);

        xpp.nextTag();
     }

    public static void getCustom() throws IOException, XmlPullParserException {
        // Ok parse this bit seperately.  Could be a note or a slide
        // Notes have # Note # - in the name
        custom_name = xpp.getAttributeValue(null,"name");
        custom_seconds = xpp.getAttributeValue(null, "seconds");
        custom_loop = xpp.getAttributeValue(null,"loop");
        custom_title = "";
        custom_subtitle = "";
        custom_notes = "";
        custom_text = "";

        boolean custom_finished = false;
        while (!custom_finished) {
            if (xpp.getName().equals("title")) {
                custom_title = xpp.nextText();
            } else if (xpp.getName().equals("subtitle")) {
                custom_subtitle = xpp.nextText();
            } else if (xpp.getName().equals("notes")) {
                custom_notes = xpp.nextText();
            } else if (xpp.getName().equals("body")) {
                custom_text = custom_text + "\n---\n" + xpp.nextText();
            } else if (xpp.getName().equals("subtitle")) {
                custom_subtitle = xpp.nextText();
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
            custom_text = custom_text.replaceFirst("\n---\n", "");
        }

        // If the custom slide is just a note (no content), fix the content
        String noteorslide = FullscreenActivity.text_slide;
        if (custom_name.contains("# " + FullscreenActivity.text_note + " # - ")) {
            // Prepare for a note
            custom_name = custom_name.replace("# " + FullscreenActivity.text_note + " # - ", "");
            custom_text = custom_notes;
            custom_notes = "";
            custom_title = "";
            custom_subtitle = "";
            custom_seconds = "";
            noteorslide = FullscreenActivity.text_note;
        }

        title = custom_title;
        author = custom_subtitle;
        user1 = custom_seconds;
        user2 = custom_loop;
        user3 = "";
        aka = custom_name;
        key_line = custom_notes;
        lyrics = custom_text;
        hymn_number = "";

        writeTempSlide(noteorslide, custom_name);

    }

    public static void getImage() throws IOException, XmlPullParserException {
        // Ok parse this bit separately.  This could have multiple images
        image_name = xpp.getAttributeValue(null,"name");
        image_seconds = xpp.getAttributeValue(null,"seconds");
        image_loop = xpp.getAttributeValue(null,"loop");
        image_title = "";
        image_subtitle = "";
        slide_images = "";
        slide_image_titles="";
        image_notes = "";
        image_filename = "";
        int imagenums = 0;

        // Work through the xml tags until we reach the end of the image slide
        // The end will be when we get to </slide_group>

        int eventType = xpp.getEventType();
        boolean allimagesdone = false;
        String image_content = "";
        String image_type;
        slide_images = "";
        slide_image_titles = "";

        while (!allimagesdone) { // Keep iterating unless the current eventType is the end of the document
            if(eventType == XmlPullParser.START_TAG) {
                if (xpp.getName().equals("title")) {
                    eventType = xpp.next();
                    image_title = xpp.getText();

                } else if (xpp.getName().equals("subtitle")) {
                    eventType = xpp.next();
                    image_subtitle = xpp.getText();

                } else if (xpp.getName().equals("notes")) {
                    eventType = xpp.next();
                    image_notes = xpp.getText();

                } else if (xpp.getName().equals("filename")) {
                    eventType = xpp.next();
                    image_filename = xpp.getText();
                    if (image_filename!=null && !image_filename.equals("") && !image_filename.isEmpty()) {
                        slide_images = slide_images + image_filename + "\n";
                        slide_image_titles = slide_image_titles + "[" + FullscreenActivity.image + "_" + (imagenums + 1) + "]\n" + image_filename + "\n\n";
                        imagenums++;
                        encodedimage = false;
                    }

                } else if (xpp.getName().equals("description")) {
                    eventType = xpp.next();
                    if (xpp.getText().contains(".png") || xpp.getText().contains(".PNG")) {
                        image_type = ".png";
                    } else if (xpp.getText().contains(".gif") || xpp.getText().contains(".GIF")) {
                        image_type = ".gif";
                    } else {
                        image_type = ".jpg";
                    }

                    if (encodedimage) {
                        // Save this image content
                        File imgfile = new File(FullscreenActivity.dircustomimages + "/" + image_title + imagenums + image_type);
                        FileOutputStream overWrite = new FileOutputStream(imgfile, false);
                        byte[] decodedString = Base64.decode(image_content, Base64.DEFAULT);
                        overWrite.write(decodedString);
                        overWrite.flush();
                        overWrite.close();
                        image_content = "";
                        slide_images = slide_images + imgfile.toString() + "\n";
                        slide_image_titles = slide_image_titles + "[" + FullscreenActivity.image + "_" + (imagenums+1) + "]\n" + imgfile + "\n\n";
                        imagenums++;
                        encodedimage=false;
                    }

                } else if (xpp.getName().equals("image")) {
                    eventType = xpp.next();
                    image_content = xpp.getText();
                    encodedimage = true;
                }

            } else if(eventType == XmlPullParser.END_TAG) {
                if (xpp.getName().equals("slide_group")) {
                    allimagesdone = true;
                }
            }

            eventType = xpp.next(); // Set the current event type from the return value of next()
        }


        if (image_subtitle==null) {
            image_subtitle = "";
        }

        if (image_seconds==null) {
            image_seconds = "";
        }

        if (image_loop==null) {
            image_loop = "";
        }

        if (slide_images==null) {
            slide_images = "";
        }

        if (image_name==null) {
            image_name = "";
        }

        if (image_notes==null) {
            image_notes = "";
        }

        if (slide_image_titles==null) {
            slide_image_titles = "";
        }

        title = image_title;
        author = image_subtitle;
        user1 = image_seconds;
        user2 = image_loop;
        user3 = slide_images.trim();
        aka = image_name;
        hymn_number = "";
        key_line = image_notes;
        lyrics = slide_image_titles.trim();

        writeTempSlide(FullscreenActivity.image,title);
    }

    public static void prepareFirstItem() {
        // If we have just loaded a set, and it isn't empty,  load the first item
        if (FullscreenActivity.mSetList.length>0) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.mSetList[0];
            FullscreenActivity.setView = "Y";

            FullscreenActivity.linkclicked = FullscreenActivity.mSetList[0];
            FullscreenActivity.pdfPageCurrent = 0;
            if (!FullscreenActivity.linkclicked.contains("/")) {
                // Right it doesn't, so add the /
                FullscreenActivity.linkclicked = "/" + FullscreenActivity.linkclicked;
            }

            // Now split the linkclicked into two song parts 0=folder 1=file
            String[] songpart = FullscreenActivity.linkclicked.split("/");

            // If the folder length isn't 0, it is a folder
            if (songpart[0].length() > 0 && !songpart[0].contains(FullscreenActivity.text_scripture) && !songpart[0].contains(FullscreenActivity.image) && !songpart[0].contains(FullscreenActivity.text_slide) && !songpart[0].contains(FullscreenActivity.text_note)) {
                FullscreenActivity.whichSongFolder = songpart[0];

            } else if (songpart[0].length() > 0 && songpart[0].contains(FullscreenActivity.text_scripture) && !songpart[0].contains(FullscreenActivity.image) && !songpart[0].contains(FullscreenActivity.text_slide) && !songpart[0].contains(FullscreenActivity.text_note)) {
                FullscreenActivity.whichSongFolder = "../OpenSong Scripture/_cache";
                songpart[0] = "../OpenSong Scripture/_cache";

            } else if (songpart[0].length() > 0 && songpart[0].contains(FullscreenActivity.text_slide) && !songpart[0].contains(FullscreenActivity.image) && !songpart[0].contains(FullscreenActivity.text_note) && !songpart[0].contains(FullscreenActivity.text_scripture)) {
                FullscreenActivity.whichSongFolder = "../Slides/_cache";
                songpart[0] = "../Slides/_cache";

            } else if (songpart[0].length() > 0 && !songpart[0].contains(FullscreenActivity.text_slide) && !songpart[0].contains(FullscreenActivity.image) && songpart[0].contains(FullscreenActivity.text_note) && !songpart[0].contains(FullscreenActivity.text_scripture)) {
                FullscreenActivity.whichSongFolder = "../Notes/_cache";
                songpart[0] = "../Notes/_cache";

            } else if (songpart[0].length() > 0 && !songpart[0].contains(FullscreenActivity.text_slide) && songpart[0].contains(FullscreenActivity.image) && !songpart[0].contains(FullscreenActivity.text_note) && !songpart[0].contains(FullscreenActivity.text_scripture)) {
                FullscreenActivity.whichSongFolder = "../Images/_cache";
                songpart[0] = "../Images/_cache";

            } else {
                FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
            }

            // Match the song folder
            ListSongFiles.listSongs();
            // Redraw the Lyrics View
            FullscreenActivity.songfilename = null;
            FullscreenActivity.songfilename = "";
            FullscreenActivity.songfilename = songpart[1];

            SetActions.indexSongInSet();
        }
    }
}