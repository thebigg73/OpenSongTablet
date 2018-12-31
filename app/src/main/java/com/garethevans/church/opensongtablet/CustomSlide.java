package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;

import java.io.OutputStream;

class CustomSlide {

    void addCustomSlide(Context c, Preferences preferences) {
        String templocator,folder;

        // Get rid of illegal characters
        String filetitle = FullscreenActivity.customslide_title.replaceAll("[|?*<\":>+\\[\\]']", " ");

        switch (FullscreenActivity.noteorslide) {
            case "note":
                folder = "Notes";
                templocator = c.getResources().getString(R.string.note);
                FullscreenActivity.customimage_list = "";
                break;
            case "slide":
                folder = "Slides";
                templocator = c.getResources().getString(R.string.slide);
                FullscreenActivity.customimage_list = "";
                break;
            case "scripture":
                folder = "Scripture";
                templocator = c.getResources().getString(R.string.scripture);
                FullscreenActivity.customreusable = false;
                FullscreenActivity.customimage_list = "";
                break;
            case "image":
            default:
                folder = "Images";
                templocator = c.getResources().getString(R.string.image);
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

        StorageAccess storageAccess = new StorageAccess();
        Uri uri = storageAccess.getUriForItem(c, preferences, folder, "_cache", filetitle);
        // Now write the modified item
        OutputStream outputStream = storageAccess.getOutputStream(c,uri);
        storageAccess.writeFileFromString(FullscreenActivity.mynewXML,outputStream);


        // If this is to be a reusable custom slide - not in the _cache folder
        if (FullscreenActivity.customreusable) {
            // Now write the modified item
            Uri uriReusable = storageAccess.getUriForItem(c, preferences, folder, "", filetitle);
            OutputStream outputStreamReusable = storageAccess.getOutputStream(c,uriReusable);
            storageAccess.writeFileFromString(FullscreenActivity.mynewXML,outputStreamReusable);
            FullscreenActivity.customreusable = false;
        }

        // Add to set
        FullscreenActivity.whatsongforsetwork = "$**_**" + templocator + "/" + filetitle + "_**$";

        // Allow the song to be added, even if it is already there
        FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;

        // Save the set and other preferences
        Preferences.savePreferences();

        // Show the current set
        SetActions setActions = new SetActions();
        setActions.prepareSetList();
    }
}