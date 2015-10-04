package com.garethevans.church.opensongtablet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;

public class SetActions extends Activity {

    static boolean check_action;

    //	public static void updateOptionListSets(Activity act, View view) {
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
        // Get the linkclicked and load the set into memory
        // Update the savedpreferences to include this set
        // This means reformatting

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


        FullscreenActivity.mySetXML = null;
        FullscreenActivity.mySetXML = "";
        FullscreenActivity.myParsedSet = null;

        // Test if file exists - the settoload is the link clicked so is still the set name
        FullscreenActivity.setfile = new File(FullscreenActivity.dirsets + "/"
                + FullscreenActivity.settoload);
        if (!FullscreenActivity.setfile.exists()) {
            return;
        }


        try {
            FileInputStream inputStream = new FileInputStream(new File(
                    FullscreenActivity.dirsets + "/"
                            + FullscreenActivity.settoload));

            int size = (int) FullscreenActivity.setfile.length();
            int bytesdone = 0;

            InputStreamReader streamReader = new InputStreamReader(
                    inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            String l;

            int count = 0;
            while ((l = bufferedReader.readLine()) != null) {
                // do what you want with the line
                FullscreenActivity.mySetXML = FullscreenActivity.mySetXML
                        + l + "\n";
                count = count + 1;
                bytesdone = bytesdone + l.length()+1;
                int percentage_done;
                try {
                    percentage_done = Math.round(((float)bytesdone / (float)size) * 100);
                } catch (Exception e) {
                    percentage_done = 0;
                }
                if (percentage_done > 100) {
                    percentage_done = 100;
                }
                PopUpListSetsFragment.val = FullscreenActivity.set_loading + ": " + percentage_done+"%";
                PopUpListSetsFragment.mHandler.post(PopUpListSetsFragment.runnable);
            }

            inputStream.close();
            bufferedReader.close();


        } catch (java.io.FileNotFoundException e) {
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

        //XmlPullParserFactory factory;
        //factory = XmlPullParserFactory.newInstance();

        //factory.setNamespaceAware(true);
        //XmlPullParser xpp;
        //xpp = factory.newPullParser();

        //xpp.setInput(new StringReader(FullscreenActivity.mySetXML));
        //int eventType;
        String scripture_title;
        String scripture_translation;
        String scripture_text;
        String scripture_seconds;
        String scripture_loop;
        String custom_name;
        String custom_title;
        String custom_subtitle;
        String custom_seconds;
        String custom_loop;
        String custom_notes;
        String custom_text;
        String image_name;
        String image_title;
        String image_subtitle;
        String image_seconds;
        String image_loop;
        String image_notes;
        String slide_images = null;
        String slide_image_temp_data;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new StringReader (FullscreenActivity.mySetXML));
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG) {
                if (xpp.getName().equals("slide_group")) {
                    // Is this a song?
                    if (xpp.getAttributeValue(null,"type").equals("song")) {
                        Log.d("parse","song found");
                        FullscreenActivity.mySet = FullscreenActivity.mySet
                                + "$**_" + xpp.getAttributeValue(3) + xpp.getAttributeValue(0) + "_**$";


                    } else if (xpp.getAttributeValue(null,"type").equals("scripture")) {
                        Log.d("parse","scripture found");
                        // Ok parse this bit seperately
                        // Initialise the values
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

                        File temp = new File(FullscreenActivity.dirbibleverses + "/" + scripture_title);
                        FileOutputStream overWrite = new FileOutputStream(temp, false);
                        // Prepare the new XML file
                        String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
                        myNEWXML += "<song>\n";
                        myNEWXML += "  <title>" + scripture_title + "</title>\n";
                        myNEWXML += "  <author>" + scripture_translation + "</author>\n";
                        myNEWXML += "  <user1>" + scripture_seconds + "</user1>\n";
                        myNEWXML += "  <user2>" + scripture_loop + "</user2>\n";
                        myNEWXML += "  <lyrics>" + scripture_text.trim() + "</lyrics>\n";
                        myNEWXML += "</song>";
                        overWrite.write(myNEWXML.getBytes());
                        overWrite.flush();
                        overWrite.close();
                        FullscreenActivity.mySet = FullscreenActivity.mySet
                                + "$**_" + "Scripture/" + scripture_title + "_**$";



                    } else if (xpp.getAttributeValue(null,"type").equals("custom")) {
                        Log.d("parse","custom found");
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

                        File temp;
                        // If the custom slide is just a note (no content), fix the content
                        if (custom_name.contains("# " + FullscreenActivity.text_note + " # - ")) {
                            // Prepare for a note
                            custom_name = custom_name.replace("# " + FullscreenActivity.text_note + " # - ", "");
                            custom_text = custom_notes;
                            custom_notes = "";
                            custom_title = "";
                            custom_subtitle = "";
                            custom_seconds = "";
                            FullscreenActivity.mySet = FullscreenActivity.mySet
                                    + "$**_" + FullscreenActivity.text_note + "/" + custom_name + "_**$";
                            temp = new File(FullscreenActivity.dircustomnotes + "/" + custom_name);
                        } else {
                            FullscreenActivity.mySet = FullscreenActivity.mySet
                                    + "$**_" + FullscreenActivity.text_slide + "/" + custom_name + "_**$";
                            temp = new File(FullscreenActivity.dircustomslides + "/" + custom_name);
                        }

                        FileOutputStream overWrite = new FileOutputStream(temp, false);
                        // Prepare the new XML file
                        String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
                        myNEWXML += "<song>\n";
                        myNEWXML += "  <title>" + custom_title + "</title>\n";
                        myNEWXML += "  <author>" + custom_subtitle + "</author>\n";
                        myNEWXML += "  <user1>" + custom_seconds + "</user1>\n";
                        myNEWXML += "  <user2>" + custom_loop + "</user2>\n";
                        myNEWXML += "  <aka>" + custom_name + "</aka>\n";
                        myNEWXML += "  <key_line>" + custom_notes + "</key_line>\n";
                        myNEWXML += "  <lyrics>" + custom_text.trim() + "</lyrics>\n";
                        myNEWXML += "</song>";
                        overWrite.write(myNEWXML.getBytes());
                        overWrite.flush();
                        overWrite.close();



                    } else if (xpp.getAttributeValue(null,"type").equals("image")) {
                        Log.d("parse","image found");
                        // Ok parse this bit seperately.  This could have multiple images
                        image_name = xpp.getAttributeValue(null,"name");
                        image_seconds = xpp.getAttributeValue(null,"seconds");
                        image_loop = xpp.getAttributeValue(null,"loop");
                        int imagenums = 0;
                        image_title = "";
                        image_subtitle = "";
                        image_notes = "";

                        boolean image_finished = false;
                        while (!image_finished) {
                            if (xpp.getName().equals("title")) {
                                image_title = xpp.nextText();
                            } else if (xpp.getName().equals("subtitle")) {
                                image_subtitle = xpp.nextText();
                            } else if (xpp.getName().equals("notes")) {
                                image_notes = xpp.nextText();
                            } else if (xpp.getName().equals("slide")) {
                                // Now loop through all the images
                                boolean indimagesfinished = false;
                                while (!indimagesfinished) {
                                    if (xpp.getName().equals("image")) {
                                        // Convert the image data into a file and get the link to it
                                        slide_image_temp_data = xpp.nextText();
                                        // Save this as a file;
                                        if (slide_image_temp_data != null) {
                                            File imgfile = new File(FullscreenActivity.dircustomimages + "/" + image_title + "_" + imagenums + ".jpg");
                                            FileOutputStream overWrite = new FileOutputStream(imgfile, false);
                                            byte[] decodedString = Base64.decode(slide_image_temp_data, Base64.DEFAULT);
                                            overWrite.write(decodedString);
                                            overWrite.flush();
                                            overWrite.close();
                                            slide_images = slide_images + imgfile.toString() + "\n";
                                            imagenums++;
                                        }
                                    }

                                    if (xpp.getEventType() != XmlPullParser.END_TAG) {
                                        xpp.nextTag();
                                    }

                                    if (xpp.getEventType() == XmlPullParser.END_TAG) {
                                        if (xpp.getName().equals("slide")) {
                                            indimagesfinished = true;
                                        }
                                    }

                                }
                            }
                            if (xpp.getEventType() != XmlPullParser.END_TAG) {
                                xpp.nextTag();
                            }

                            if (xpp.getEventType() == XmlPullParser.END_TAG) {
                                if (xpp.getName().equals("slide")) {
                                    image_finished = true;
                                }
                            }
                        }

                        File temp;

                        FullscreenActivity.mySet = FullscreenActivity.mySet
                                + "$**_" + FullscreenActivity.text_image + "/" + image_name + "_**$";
                        temp = new File(FullscreenActivity.dircustomimages + "/" + image_name);

                        FileOutputStream overWrite = new FileOutputStream(temp, false);
                        // Prepare the new XML file
                        String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
                        myNEWXML += "<song>\n";
                        myNEWXML += "  <title>" + image_title + "</title>\n";
                        myNEWXML += "  <author>" + image_subtitle + "</author>\n";
                        myNEWXML += "  <user1>" + image_seconds + "</user1>\n";
                        myNEWXML += "  <user2>" + image_loop + "</user2>\n";
                        myNEWXML += "  <aka>" + image_name + "</aka>\n";
                        myNEWXML += "  <hymn_number>" + slide_images.trim() + "</hymn_number>\n";
                        myNEWXML += "  <key_line>" + image_notes + "</key_line>\n";
                        myNEWXML += "  <lyrics>" + slide_images.trim() + "</lyrics>\n";
                        myNEWXML += "</song>";
                        overWrite.write(myNEWXML.getBytes());
                        overWrite.flush();
                        overWrite.close();

                    }
                }
            }
            eventType = xpp.next();
        }

/*        // Begin the parsing!!!!!!
        eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xpp.getName().equals("slide_group")) {
                    if (xpp.getAttributeValue(1).equals("song")) {
                        // SONG
                        FullscreenActivity.mySet = FullscreenActivity.mySet
                                + "$**_" + xpp.getAttributeValue(3) + xpp.getAttributeValue(0) + "_**$";




                    } else if (xpp.getAttributeValue(0).equals("scripture")) {
                        // SCRIPTURE
                        // Ok parse this bit seperately
                        scripture_title = "";
                        scripture_translation = "";
                        scripture_text = "";
                        boolean scripture_finished = false;
                        while (!scripture_finished) {
                            if (xpp.getName().equals("title")) {
                                scripture_title = xpp.nextText();
                            } else if (xpp.getName().equals("body")) {
                                scripture_text = scripture_text + "\n[]\n" + xpp.nextText();
                            } else if (xpp.getName().equals("subtitle")) {
                                scripture_translation = xpp.nextText();
                            }
                            eventType = xpp.nextTag();
                            if (xpp.getName().equals("notes")) {
                                scripture_finished = true;
                            }
                        }
                        // Create a new file for each of these entries
                        // Filename is title with Scripture/

                        // Break the scripture_text up into small manageable chunks
                        // First up, start each new verse on a new line
                        // The verses are in the titles
                        //Replace all spaces (split points) with \n
                        scripture_text = scripture_text.replace(" ", "\n");

                        //Split the verses up into an array by new lines
                        String[] temp_text = scripture_text.split("\n");

                        String[] add_text = new String[100];
                        int array_line = 0;
                        //Add all the array back together and make sure no line goes above 40 characters
                        scripture_text = "";
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

                        FullscreenActivity.dirbibleverses.mkdirs();
                        File temp = new File(FullscreenActivity.dirbibleverses + "/" + scripture_title);
                        FileOutputStream overWrite = new FileOutputStream(temp, false);
                        // Prepare the new XML file
                        String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
                        myNEWXML += "<song>\n";
                        myNEWXML += "  <title>" + scripture_title + "</title>\n";
                        myNEWXML += "  <author>" + scripture_translation + "</author>\n";
                        myNEWXML += "  <lyrics>" + scripture_text.trim() + "</lyrics>\n";
                        myNEWXML += "</song>";
                        overWrite.write(myNEWXML.getBytes());
                        overWrite.flush();
                        overWrite.close();
                        FullscreenActivity.mySet = FullscreenActivity.mySet
                                + "$**_" + "Scripture/" + scripture_title + "_**$";





                    } else if (xpp.getAttributeValue(0).equals("custom") || xpp.getAttributeValue(1).equals("custom")) {
                        // CUSTOM
                        // Ok parse this bit seperately.  Could be a note or a slide
                        // Notes have # Note # - in the name
                        boolean slide_finished = false;
                        slide_name = "";
                        slide_title = "";
                        slide_subtitle = "";
                        slide_seconds = "";
                        slide_loop = "";
                        slide_transition = "";
                        slide_notes = "";
                        slide_text = "";

                        while (!slide_finished) {
                            if (xpp.getAttributeCount() > 1) {
                                if (xpp.getAttributeName(0).equals("name")) {
                                    slide_name = xpp.getAttributeValue(0);
                                } else if (xpp.getAttributeName(1).equals("name")) {
                                    slide_name = xpp.getAttributeValue(1);
                                }
                            }
                            if (xpp.getAttributeCount() > 3) {
                                if (xpp.getAttributeName(3).equals("seconds")) {
                                    slide_seconds = xpp.getAttributeValue(3);
                                }
                            }
                            if (xpp.getAttributeCount() > 4) {
                                if (xpp.getAttributeName(4).equals("loop")) {
                                    slide_loop = xpp.getAttributeValue(4);
                                }
                            }
                            if (xpp.getAttributeCount() > 5) {
                                if (xpp.getAttributeName(5).equals("transition")) {
                                    slide_transition = xpp.getAttributeValue(5);
                                }
                            }

                            if (xpp.getName().equals("title")) {
                                slide_title = xpp.nextText();
                            } else if (xpp.getName().equals("subtitle")) {
                                slide_subtitle = xpp.nextText();
                            } else if (xpp.getName().equals("notes")) {
                                slide_notes = xpp.nextText();
                            } else if (xpp.getName().equals("body")) {
                                slide_text = slide_text + "\n---\n" + xpp.nextText();
                            } else if (xpp.getName().equals("subtitle")) {
                                slide_subtitle = xpp.nextText();
                            }
                            eventType = xpp.nextTag();
                            if (xpp.getName().equals("slides") && xpp.getEventType() == XmlPullParser.END_TAG) {
                                slide_finished = true;
                            }
                        }

                        // Remove first ---
                        if (slide_text.indexOf("\n---\n") == 0) {
                            slide_text = slide_text.replaceFirst("\n---\n", "");
                        }

                        slide_finished = false;

                        File temp;
                        // If the custom slide is just a note (no content), fix the content
                        if (slide_name.contains("# " + FullscreenActivity.text_note + " # - ")) {
                            // Prepare for a note
                            slide_name = slide_name.replace("# " + FullscreenActivity.text_note + " # - ", "");
                            slide_text = slide_notes;
                            slide_notes = "";
                            slide_title = "";
                            slide_subtitle = "";
                            slide_seconds = "";
                            slide_transition = "";
                            FullscreenActivity.mySet = FullscreenActivity.mySet
                                    + "$**_" + FullscreenActivity.text_note + "/" + slide_name + "_**$";
                            FullscreenActivity.dircustomnotes.mkdirs();
                            temp = new File(FullscreenActivity.dircustomnotes + "/" + slide_name);
                        } else {
                            FullscreenActivity.mySet = FullscreenActivity.mySet
                                    + "$**_" + FullscreenActivity.text_slide + "/" + slide_name + "_**$";
                            FullscreenActivity.dircustomslides.mkdirs();
                            temp = new File(FullscreenActivity.dircustomslides + "/" + slide_name);
                        }

                        FileOutputStream overWrite = new FileOutputStream(temp, false);
                        // Prepare the new XML file
                        String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
                        myNEWXML += "<song>\n";
                        myNEWXML += "  <title>" + slide_title + "</title>\n";
                        myNEWXML += "  <author>" + slide_subtitle + "</author>\n";
                        myNEWXML += "  <user1>" + slide_seconds + "</user1>\n";
                        myNEWXML += "  <user2>" + slide_loop + "</user2>\n";
                        myNEWXML += "  <user3>" + slide_transition + "</user3>\n";
                        myNEWXML += "  <aka>" + slide_name + "</aka>\n";
                        myNEWXML += "  <key_line>" + slide_notes + "</key_line>\n";
                        myNEWXML += "  <lyrics>" + slide_text.trim() + "</lyrics>\n";
                        myNEWXML += "</song>";
                        overWrite.write(myNEWXML.getBytes());
                        overWrite.flush();
                        overWrite.close();
                        scripture_text = "";



                    } else if (xpp.getAttributeValue(1).equals("image")) {
                        // IMAGE
                        // Ok parse this bit seperately.  This could have multiple images
                        boolean image_finished = false;
                        slide_name = "";
                        slide_seconds = "";
                        slide_loop = "";
                        int imagenums = 0;
                        slide_title = "";
                        slide_subtitle = "";
                        slide_notes = "";

                        if (xpp.getAttributeCount() > 0) {
                            if (xpp.getAttributeName(0).equals("name")) {
                                slide_name = xpp.getAttributeValue(0);
                            } else if (xpp.getAttributeName(1).equals("name")) {
                                slide_name = xpp.getAttributeValue(1);
                            }
                        }
                        if (xpp.getAttributeCount() > 4) {
                            if (xpp.getAttributeName(4).equals("seconds")) {
                                slide_seconds = xpp.getAttributeValue(4);
                            }
                        }
                        if (xpp.getAttributeCount() > 5) {
                            if (xpp.getAttributeName(5).equals("loop")) {
                                slide_loop = xpp.getAttributeValue(5);
                            }
                        }

                        while (!image_finished) {
                            if (xpp.getName().equals("title")) {
                                slide_title = xpp.nextText();
                            } else if (xpp.getName().equals("subtitle")) {
                                slide_subtitle = xpp.nextText();
                            } else if (xpp.getName().equals("notes")) {
                                slide_notes = xpp.nextText();
                            } else if (xpp.getName().equals("slides")) {
                                // Now loop through all the images
                                boolean indimagesfinished = false;
                                while (!indimagesfinished) {
                                    if (xpp.getName().equals("image")) {
                                        // Convert the image data into a file and get the link to it
                                        slide_image_temp_data = xpp.nextText();
                                        // Save this as a file;
                                        if (slide_image_temp_data!=null) {
                                            File imgfile = new File(FullscreenActivity.dircustomimages + "/" + slide_title + "_" + imagenums + ".jpg");
                                            FileOutputStream overWrite =  new FileOutputStream(imgfile, false);
                                            byte[] decodedString = Base64.decode(slide_image_temp_data, Base64.DEFAULT);
                                            overWrite.write(decodedString);
                                            overWrite.flush();
                                            overWrite.close();
                                            slide_images = slide_images + imgfile.toString() + "\n";
                                        }
                                        if (xpp.getName().equals("slides") && xpp.getEventType() == XmlPullParser.END_TAG) {
                                            indimagesfinished = true;
                                        }
                                        eventType = xpp.nextTag();
                                    }
                                }
                            }
                            if (xpp.getName().equals("slides") && xpp.getEventType() == XmlPullParser.END_TAG) {
                                image_finished = true;
                            }
                            eventType = xpp.nextTag();
                        }

                        image_finished = false;

                        File temp;

                        FullscreenActivity.mySet = FullscreenActivity.mySet
                                    + "$**_" + FullscreenActivity.text_image + "/" + slide_name + "_**$";
                            FullscreenActivity.dircustomimages.mkdirs();
                            temp = new File(FullscreenActivity.dircustomimages + "/" + slide_name);

                        FileOutputStream overWrite = new FileOutputStream(temp, false);
                        // Prepare the new XML file
                        String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
                        myNEWXML += "<song>\n";
                        myNEWXML += "  <title>" + slide_title + "</title>\n";
                        myNEWXML += "  <author>" + slide_subtitle + "</author>\n";
                        myNEWXML += "  <user1>" + slide_seconds + "</user1>\n";
                        myNEWXML += "  <user2>" + slide_loop + "</user2>\n";
                        myNEWXML += "  <user3>" + slide_transition + "</user3>\n";
                        myNEWXML += "  <aka>" + slide_name + "</aka>\n";
                        myNEWXML += "  <hymn_number>" + slide_images + "</hymn_number>\n";
                        myNEWXML += "  <key_line>" + slide_notes + "</key_line>\n";
                        myNEWXML += "  <lyrics>" + slide_text.trim() + "</lyrics>\n";
                        myNEWXML += "</song>";
                        overWrite.write(myNEWXML.getBytes());
                        overWrite.flush();
                        overWrite.close();

                    }
                    eventType = xpp.next();
                }
                eventType = xpp.next();
            }
            eventType = xpp.next();
        }*/



    }


    public static void indexSongInSet() {
        FullscreenActivity.setSize = FullscreenActivity.mSetList.length;
        FullscreenActivity.previousSongInSet = "";
        FullscreenActivity.nextSongInSet = "";

        // Fix whatsongforsetwork
        Log.d("d", "whatsongforsetwork=" + FullscreenActivity.whatsongforsetwork);

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

}
