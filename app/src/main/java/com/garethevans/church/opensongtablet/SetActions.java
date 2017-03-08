package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SetActions extends Activity {

    public interface MyInterface {
        void doMoveSection();
        void loadSong();
    }

    public static MyInterface mListener;

    static boolean check_action;
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
    static String hymn_number_imagecode;
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

        // Reset any current set
        FullscreenActivity.mySet = null;
        FullscreenActivity.mySet = "";

        // Test if file exists - the settoload is the link clicked so is still the set name
        FullscreenActivity.setfile = new File(FullscreenActivity.dirsets + "/" + FullscreenActivity.settoload);
        if (!FullscreenActivity.setfile.exists()) {
            return;
        }

        // Try the new, improved method of loading in a set
        // First up, try to get the encoding of the set file
        String utf = LoadXML.getUTFEncoding(FullscreenActivity.setfile);

        // Now we know the encoding, iterate through the file extracting the items as we go
        factory = XmlPullParserFactory.newInstance();
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
                        if (xpp.getAttributeValue(null, "type").equals("song")) {
                            // Get song
                            getSong();
                        } else if (xpp.getAttributeValue(null, "type").equals("scripture")) {
                            // Get Scripture
                            getScripture();
                        } else if (xpp.getAttributeValue(null, "type").equals("custom")) {
                            // Get Custom (Note or slide or variation)
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
        if (FullscreenActivity.mSetList!=null) {
            FullscreenActivity.setSize = FullscreenActivity.mSetList.length;
        } else {
            FullscreenActivity.setSize = 0;
        }
        FullscreenActivity.previousSongInSet = "";
        FullscreenActivity.nextSongInSet = "";

        // Go backwards through the setlist - this finishes with the first occurrence
        // Useful for duplicate items, otherwise it returns the last occurrence
        // Not yet tested, so left

        FullscreenActivity.mSet = FullscreenActivity.mSetList;

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

    public static void songIndexClickInSet() {
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

    public static void saveSetMessage(Context c) {
        FullscreenActivity.whattodo = "";
        if (FullscreenActivity.mSetList!=null && FullscreenActivity.mSetList.length>0) {
            CreateNewSet.doCreation();
        }
        if (FullscreenActivity.myToastMessage.equals("yes")) {
            FullscreenActivity.myToastMessage = c.getString(R.string.set_save)
                    + " - " + c.getString(R.string.ok);
        } else {
            FullscreenActivity.myToastMessage = c.getString(R.string.set_save)
                    + " - " + c.getString(R.string.no);
        }
    }

    public static void clearSet(Context c) {
        FullscreenActivity.mySet = "";
        FullscreenActivity.mSetList = null;
        FullscreenActivity.setView = false;

        // Save the new, empty, set
        Preferences.savePreferences();

        FullscreenActivity.myToastMessage = c.getString(R.string.options_set_clear) + " " +
                c.getString(R.string.ok);
    }

    public static void deleteSet(Context c) {
        String[] tempsets = FullscreenActivity.setnamechosen.split("%_%");
        FullscreenActivity.myToastMessage = "";
        for (String tempfile:tempsets) {
            if (tempfile!=null && !tempfile.equals("") && !tempfile.isEmpty()) {
                File settodelete = new File(FullscreenActivity.dirsets + "/" + tempfile);
                if (settodelete.delete()) {
                    FullscreenActivity.myToastMessage = FullscreenActivity.myToastMessage + tempfile + ", ";
                }
            }
        }
        if (FullscreenActivity.myToastMessage.length()>2) {
            FullscreenActivity.myToastMessage = FullscreenActivity.myToastMessage.substring(0, FullscreenActivity.myToastMessage.length() - 2);
        }
        FullscreenActivity.myToastMessage = FullscreenActivity.myToastMessage + " " + c.getString(R.string.sethasbeendeleted);
    }

    public static void getSongForSetWork() {
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Scripture/_cache")) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.scripture + "/" + FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Slides/_cache")) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.slide + "/" + FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Notes/_cache")) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.note + "/" + FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Images/_cache")) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.image + "/" + FullscreenActivity.songfilename;
        } else if (FullscreenActivity.whichSongFolder.equals("../Variations")) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.text_variation + "/" + FullscreenActivity.songfilename;
        } else {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.whichSongFolder + "/"
                    + FullscreenActivity.songfilename;
        }
    }

    public static boolean isSongInSet() {
        if (FullscreenActivity.setSize > 0) {
            // Get the name of the song to look for (including folders if need be)
            getSongForSetWork();

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

    public static void checkDirectories() {
        // Check the Scripture _cache Directory exists
        if (FullscreenActivity.dirscriptureverses.exists()) {
            // Scripture folder exists, do nothing other than clear it!
            for (File scripfile : FullscreenActivity.dirscriptureverses.listFiles()) {
                check_action = scripfile.delete();
            }
        } else {
            // Tell the user we're creating the Scripture _cache directory
            check_action = FullscreenActivity.dirscriptureverses.mkdirs();
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

        // Check the Variations Directory exists
        if (FullscreenActivity.dirvariations.exists()) {
            // Variations folder exists, do nothing other than clear it!
            for (File variationsfile : FullscreenActivity.dirvariations.listFiles()) {
                check_action = variationsfile.delete();
            }
        } else {
            // Tell the user we're creating the Slides _cache directory
            check_action = FullscreenActivity.dirvariations.mkdirs();
        }

    }

    public static void writeTempSlide(String where, String what) throws IOException {
        // Fix the custom name so there are no illegal characters
        what = what.replaceAll("[|?*<\":>+\\[\\]']", " ");
        File temp;
        String set_item;
        String foldername;
        String setprefix;

        if (where.equals(FullscreenActivity.text_scripture)) {
            foldername = FullscreenActivity.dirscriptureverses.toString();
            setprefix  = "$**_**" + FullscreenActivity.scripture + "/";
        } else if (where.equals(FullscreenActivity.text_slide)) {
            foldername = FullscreenActivity.dircustomslides.toString();
            setprefix  = "$**_**" + FullscreenActivity.slide + "/";
        } else if (where.equals(FullscreenActivity.image)) {
            foldername = FullscreenActivity.dircustomimages.toString();
            setprefix  = "$**_**" + FullscreenActivity.image + "/";
        } else if (where.equals(FullscreenActivity.text_variation)) {
            foldername = FullscreenActivity.dirvariations.toString();
            setprefix  = "$**_**" + FullscreenActivity.text_variation + "/";
        } else {
            foldername = FullscreenActivity.dircustomnotes.toString();
            setprefix  = "$**_**" + FullscreenActivity.note + "/";
        }

        // Check to see if that file already exists (same name).  If so, add _ to the end
        temp = new File(foldername + "/" + what);
        while (temp.exists()) {
            what += "_";
            temp = new File(foldername + "/" + what);
        }

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

        if (where.equals(FullscreenActivity.text_variation)) {
            // Create a full song instead
            byte[] data = Base64.decode(custom_notes, Base64.DEFAULT);
            my_NEW_XML = new String(data, "UTF-8");
        }

        overWrite.write(my_NEW_XML.getBytes());
        overWrite.flush();
        overWrite.close();
        FullscreenActivity.mySet = FullscreenActivity.mySet + set_item;
    }

    public static void getSong() {
        try {
            FullscreenActivity.mySet = FullscreenActivity.mySet
                    + "$**_" + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"path")) + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name")) + "_**$";
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            xpp.nextTag();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void getScripture() throws IOException, XmlPullParserException {
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
                scripture_text = scripture_text + "\n[]\n" + LoadXML.parseFromHTMLEntities(xpp.nextText());
            } else if (xpp.getName().equals("subtitle")) {
                scripture_translation = LoadXML.parseFromHTMLEntities(xpp.nextText());
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

        writeTempSlide(FullscreenActivity.text_scripture,scripture_title);

        xpp.nextTag();
     }

    public static void getCustom() throws IOException, XmlPullParserException {
        // Ok parse this bit seperately.  Could be a note or a slide or a variation
        // Notes have # Note # - in the name
        // Variations have # Variation # - in the name
        custom_name = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name"));
        custom_seconds = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "seconds"));
        custom_loop = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"loop"));
        custom_title = "";
        custom_subtitle = "";
        custom_notes = "";
        custom_text = "";

        boolean custom_finished = false;
        while (!custom_finished) {
            if (xpp.getName().equals("title")) {
                custom_title = LoadXML.parseFromHTMLEntities(xpp.nextText());
            } else if (xpp.getName().equals("subtitle")) {
                custom_subtitle = LoadXML.parseFromHTMLEntities(xpp.nextText());
            } else if (xpp.getName().equals("notes")) {
                custom_notes = LoadXML.parseFromHTMLEntities(xpp.nextText());
            } else if (xpp.getName().equals("body")) {
                custom_text = custom_text + "\n---\n" + LoadXML.parseFromHTMLEntities(xpp.nextText());
            } else if (xpp.getName().equals("subtitle")) {
                custom_subtitle = LoadXML.parseFromHTMLEntities(xpp.nextText());
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
            if (custom_notes.equals("")) {
                custom_text = custom_name;
            }
            custom_notes = "";
            custom_title = "";
            custom_subtitle = "";
            custom_seconds = "";
            noteorslide = FullscreenActivity.text_note;

        // If it is a song variation, the full song contents are written to the notes part
        // The contents will be a compatible slide for OpenSong desktop presentation, not needed in this app
        } else if (custom_name.contains("# " + FullscreenActivity.text_variation + " # - ")) {
            // Prepare for a variation
            custom_name = custom_name.replace("# " + FullscreenActivity.text_variation + " # - ", "");
            custom_text = custom_notes;
            custom_title = custom_name;
            custom_subtitle = "";
            custom_seconds = "";
            noteorslide = FullscreenActivity.text_variation;
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
        image_name = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name"));
        image_seconds = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"seconds"));
        image_loop = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"loop"));
        image_title = "";
        image_subtitle = "";
        slide_images = "";
        slide_image_titles="";
        image_notes = "";
        image_filename = "";
        hymn_number_imagecode = "";
        key_line = "";
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
                     image_title = LoadXML.parseFromHTMLEntities(xpp.nextText());

                } else if (xpp.getName().equals("subtitle")) {
                    image_subtitle = LoadXML.parseFromHTMLEntities(xpp.nextText());

                } else if (xpp.getName().equals("notes")) {
                    image_notes = LoadXML.parseFromHTMLEntities(xpp.nextText());

                } else if (xpp.getName().equals("filename")) {
                    image_filename = LoadXML.parseFromHTMLEntities(xpp.nextText());
                    if (image_filename!=null && !image_filename.equals("") && !image_filename.isEmpty()) {
                        slide_images = slide_images + image_filename + "\n";
                        slide_image_titles = slide_image_titles + "[" + FullscreenActivity.image + "_" + (imagenums + 1) + "]\n" + image_filename + "\n\n";
                        imagenums++;
                        encodedimage = false;
                    }

                } else if (xpp.getName().equals("description")) {
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
                        if (image_title==null || image_title.equals("")) {
                            image_title = FullscreenActivity.image;
                        }

                        File imgfile = new File(FullscreenActivity.dircustomimages + "/" + image_title + imagenums + image_type);
                        while(imgfile.exists()) {

                            image_title += "_";
                            imgfile = new File(FullscreenActivity.dircustomimages + "/" + image_title + imagenums + image_type);
                        }
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
                    image_content = xpp.nextText();
                    hymn_number_imagecode = hymn_number_imagecode + image_content.trim() + "XX_IMAGE_XX";
                    encodedimage = true;
                }

            } else if(eventType == XmlPullParser.END_TAG) {
                if (xpp.getName().equals("slide_group")) {
                    allimagesdone = true;
                }
            }

            eventType = xpp.next(); // Set the current event type from the return value of next()
        }

        if (image_title==null || image_title.equals("")) {
            image_title = FullscreenActivity.image;
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
        hymn_number = hymn_number_imagecode;
        key_line = image_notes;
        lyrics = slide_image_titles.trim();
        writeTempSlide(FullscreenActivity.image,title);
    }

    public static void prepareFirstItem() {
        // If we have just loaded a set, and it isn't empty,  load the first item
        if (FullscreenActivity.mSetList.length>0) {
            FullscreenActivity.whatsongforsetwork = FullscreenActivity.mSetList[0];
            FullscreenActivity.setView = true;

            FullscreenActivity.linkclicked = FullscreenActivity.mSetList[0];
            FullscreenActivity.pdfPageCurrent = 0;

            // Get the song and folder names from the item clicked in the set list
            getSongFileAndFolder();

            // Match the song folder
            ListSongFiles.listSongs();

            // Get the index of the song in the current set
            SetActions.indexSongInSet();
        }
    }

    public static void getSongFileAndFolder() {

        if (FullscreenActivity.linkclicked!=null && !FullscreenActivity.linkclicked.contains("/")) {
            // Right it doesn't, so add the /
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
        // Now split the linkclicked into two song parts 0=folder 1=file
        String[] songpart = FullscreenActivity.linkclicked.split("/");

        FullscreenActivity.songfilename = songpart[1];

        // If the folder length isn't 0, it is a folder
        if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = songpart[0];

        } else if (songpart[0].length() > 0 &&
                songpart[0].contains("**"+FullscreenActivity.text_scripture) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note)) {
            FullscreenActivity.whichSongFolder = "../Scripture/_cache";
            songpart[0] = "../Scripture/_cache";

        } else if (songpart[0].length() > 0 &&
                songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture)) {
            FullscreenActivity.whichSongFolder = "../Slides/_cache";
            songpart[0] = "../Slides/_cache";

        } else if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                songpart[0].contains("**"+FullscreenActivity.text_note) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture)) {
            FullscreenActivity.whichSongFolder = "../Notes/_cache";
            songpart[0] = "../Notes/_cache";

        } else if (songpart[0].length() > 0 &&
                !songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture)) {
            FullscreenActivity.whichSongFolder = "../Images/_cache";
            songpart[0] = "../Images/_cache";

        } else if (songpart[0].length() > 0 &&
                songpart[0].contains("**"+FullscreenActivity.text_variation) &&
                !songpart[0].contains("**"+FullscreenActivity.text_slide) &&
                !songpart[0].contains("**"+FullscreenActivity.image) &&
                !songpart[0].contains("**"+FullscreenActivity.text_note) &&
                !songpart[0].contains("**"+FullscreenActivity.text_scripture)) {
            FullscreenActivity.whichSongFolder = "../Variations";
            songpart[0] = "../Variations";

        } else {
            FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
        }
    }

    public static void doMoveInSet(Context c) {
        mListener = (MyInterface) c;

        boolean justmovingsections = false;

        // If we are in Stage Mode or Presentation Mode, check the sections first
        if (FullscreenActivity.whichMode.equals("Stage") || FullscreenActivity.whichMode.equals("Presentation")) {

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
                    if (FullscreenActivity.linkclicked == null) {
                        FullscreenActivity.linkclicked = "";
                    }
                }

            } else if (FullscreenActivity.setMoveDirection.equals("forward")) {
                if (FullscreenActivity.indexSongInSet<FullscreenActivity.mSetList.length-1) {
                    FullscreenActivity.indexSongInSet += 1;
                    FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
                    if (FullscreenActivity.linkclicked == null) {
                        FullscreenActivity.linkclicked = "";
                    }
                }
            }

            FullscreenActivity.setMoveDirection = "";

            // Get the song and folder names from the item clicked in the set list
            getSongFileAndFolder();

            // Save the preferences
            Preferences.savePreferences();

            // Match the song folder
            ListSongFiles.listSongs();

            FullscreenActivity.setMoveDirection = "";
            mListener.loadSong();
        }

    }
}