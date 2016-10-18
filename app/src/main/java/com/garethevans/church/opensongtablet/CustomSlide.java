package com.garethevans.church.opensongtablet;

import java.io.FileOutputStream;
import java.io.IOException;

public class CustomSlide {

    public static void addCustomSlide() {
        String filename;
        String reusablefilename;
        String templocator;

        // Get rid of illegal characters
        String filetitle = FullscreenActivity.customslide_title.replaceAll("[|?*<\":>+\\[\\]']", " ");

        switch (FullscreenActivity.noteorslide) {
            case "note":
                filename = FullscreenActivity.dircustomnotes + "/" + filetitle;
                reusablefilename = FullscreenActivity.homedir + "/Notes/" + filetitle;
                templocator = FullscreenActivity.note;
                FullscreenActivity.customimage_list = "";
                break;
            case "slide":
                filename = FullscreenActivity.dircustomslides + "/" + filetitle;
                reusablefilename = FullscreenActivity.homedir + "/Slides/" + filetitle;
                templocator = FullscreenActivity.slide;
                FullscreenActivity.customimage_list = "";
                break;
            case "scripture":
                filename = FullscreenActivity.dirscriptureverses + "/" + filetitle;
                reusablefilename = FullscreenActivity.dirscripture + "/" + filetitle;
                templocator = FullscreenActivity.text_scripture;
                FullscreenActivity.customreusable = false;
                FullscreenActivity.customimage_list = "";
                break;
            default:
                filename = FullscreenActivity.dircustomimages + "/" + filetitle;
                reusablefilename = FullscreenActivity.homedir + "/Images/" + filetitle;
                templocator = FullscreenActivity.image;
                break;
        }

        // If slide content is empty - put the title in
        if ((FullscreenActivity.customslide_content.isEmpty() ||
                FullscreenActivity.customslide_content.equals("")) &&
                !FullscreenActivity.noteorslide.equals("image")) {
            FullscreenActivity.customslide_content = FullscreenActivity.customslide_title;
        }

        // Prepare the custom slide so it can be viewed in the app
        // When exporting/saving the set, the contents get grabbed from this
        FullscreenActivity.mynewXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        FullscreenActivity.mynewXML += "<song>\n";
        FullscreenActivity.mynewXML += "  <title>" + FullscreenActivity.customslide_title + "</title>\n";
        FullscreenActivity.mynewXML += "  <author></author>\n";
        FullscreenActivity.mynewXML += "  <user1>" + FullscreenActivity.customimage_time + "</user1>\n";  // This is used for auto advance time
        FullscreenActivity.mynewXML += "  <user2>" + FullscreenActivity.customimage_loop + "</user2>\n";  // This is used for loop on or off
        FullscreenActivity.mynewXML += "  <user3>" + FullscreenActivity.customimage_list + "</user3>\n";  // This is used as links to a background images
        FullscreenActivity.mynewXML += "  <aka></aka>\n";
        FullscreenActivity.mynewXML += "  <key_line></key_line>\n";
        FullscreenActivity.mynewXML += "  <hymn_number></hymn_number>\n";
        FullscreenActivity.mynewXML += "  <lyrics>" + FullscreenActivity.customslide_content + "</lyrics>\n";
        FullscreenActivity.mynewXML += "</song>";

        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;", "&");
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&", "&amp;");

        // Now write the modified song
        FileOutputStream overWrite;
        try {
            overWrite = new FileOutputStream(filename, false);
            overWrite.write(FullscreenActivity.mynewXML.getBytes());
            overWrite.flush();
            overWrite.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If this is to be a reusable custom slide
        if (FullscreenActivity.customreusable) {
            // Now write the modified song
            FileOutputStream overWriteResuable;
            try {
                overWriteResuable = new FileOutputStream(reusablefilename, false);
                overWriteResuable.write(FullscreenActivity.mynewXML.getBytes());
                overWriteResuable.flush();
                overWriteResuable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FullscreenActivity.customreusable = false;
        }

        // Add to set
        FullscreenActivity.whatsongforsetwork = "$**_**" + templocator + "/" + filetitle + "_**$";

        // Allow the song to be added, even if it is already there
        FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;

        // Save the set and other preferences
        Preferences.savePreferences();

        // Show the current set
        SetActions.prepareSetList();
    }
}
