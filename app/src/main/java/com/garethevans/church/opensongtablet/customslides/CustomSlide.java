package com.garethevans.church.opensongtablet.customslides;

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class CustomSlide {

    // This object holds and deals with any custom slide objects

    // Firstly the variables used when on the create slide page
    private String createType = "note";     // Type of custom slide being created
    private boolean createLoop = false;     // Should slide have loop
    private String createTitle = "";        // The slide title
    private String createTime = "";         // The time for slides in secs
    private boolean createReusable = false; // Create this as a reusable file (not just in the _cache folder)
    private String createContent = "";      // The slide content
    private String createImages = "";       // Images used with the slides

    // Now the variables used when storing as a fake song
    private String xml;                     // Saved as a fake song
    private String type;                    // The type of file (translation version) as a location
    private String folder;                  // The folder for saving based on type
    private String file;                    // A file safe version of the title
    private String title, lyrics, key;      // Hopefully obvious as the title, content and key
    private String author;                  // For scripture:  The translation
    private String copyright;               // For slideshow: The transition
    private String user1;                   // For slideshow: The duration of the slide
    private String user2;                   // For slideshow: A boolean if slides loop
    private String user3;                   // For slideshow: Links to background images
    private String aka;                     // Any image saved as a background
    private String hymn_num;                // Notes saved with the slide
    private String key_line;                //

    public void buildCustomSlide(Context c, MainActivityInterface mainActivityInterface, ArrayList<String> customSlide) {
        resetCustomSlide();
        if (customSlide!=null && customSlide.size()>2) {
            title = customSlide.get(1);
            lyrics = customSlide.get(2);
            file = mainActivityInterface.getStorageAccess().safeFilename(title);

            switch (customSlide.get(0)) {
                case "scripture":
                    author = customSlide.get(3);
                    folder = "Scripture";
                    type = c.getString(R.string.scripture);
                    // Add the translation to the filename
                    file = file+" "+mainActivityInterface.getStorageAccess().safeFilename(author);
                    break;
                case "note":
                    folder = "Notes";
                    type = c.getResources().getString(R.string.note);
                    break;
                case "slide":
                    user1 = customSlide.get(3);
                    user2 = customSlide.get(4);
                    folder = "Slides";
                    type = c.getResources().getString(R.string.slide);
                    break;
                case "image":
                    user1 = customSlide.get(3);
                    user2 = customSlide.get(4);
                    user3 = customSlide.get(5);
                    lyrics = "";
                    folder = "Images";
                    type = c.getResources().getString(R.string.image);
                    break;
            }
        }
    }

    private void resetCustomSlide() {
        xml = "";
        title = "";
        author = "";
        copyright = "";
        key = "";
        lyrics = "";
        user1 = "";
        user2 = "";
        user3 = "";
        aka = "";
        key_line = "";
        hymn_num = "";
        folder = "";
        type = "";
        file = "";
    }

    public void addItemToSet(Context c, MainActivityInterface mainActivityInterface, boolean reusable) {
        if (file != null && !file.isEmpty() && folder != null && !folder.isEmpty()) {
            // Prepare the custom slide so it can be viewed in the app
            // When exporting/saving the set, the contents get grabbed from this

            // If slide content is empty - put the title in
            if (lyrics.isEmpty()) {
                lyrics = title;
            }

            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
            xml += "<song>\n";
            xml += "  <title>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(title) + "</title>\n";
            xml += "  <author>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(author) + "</author>\n";
            xml += "  <copyright>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(copyright) + "</copyright>\n";
            xml += "  <key>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(key) + "</key>\n";
            xml += "  <user1>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(user1) + "</user1>\n";
            xml += "  <user2>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(user2) + "</user2>\n";
            xml += "  <user3>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(user3) + "</user3>\n";
            xml += "  <aka>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(aka) + "</aka>\n";
            xml += "  <key_line>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(key_line) + "</key_line>\n";
            xml += "  <hymn_number>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(hymn_num) + "</hymn_number>\n";
            xml += "  <lyrics>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(lyrics) + "</lyrics>\n";
            xml += "</song>";

            // Make sure any & are encoded properly - first reset any currently encoded, then put back
            xml = xml.replace("&amp;", "&");
            xml = xml.replace("&", "&amp;");

            // Now prepare to save the file
            // If it is flagged to be reusable, it also gets saved in the top level folder
            // All custom slides get saved into the temp _cache folder for use with this set
            mainActivityInterface.getStorageAccess().doStringWriteToFile(folder, "_cache", file, xml);

            if (reusable) {
                mainActivityInterface.getStorageAccess().doStringWriteToFile(folder, "", file, xml);
            }

            // Add to set $**_**{customsfolder}/filename_***key***_**$
            String songforsetwork = "$**_**" + folder + "/" + file + "_***" + key + "***__**$";
            mainActivityInterface.getCurrentSet().addToCurrentSetString(songforsetwork);
            mainActivityInterface.getCurrentSet().addSetItem(songforsetwork);
            mainActivityInterface.getCurrentSet().addSetValues("**" + folder, file, key);
            mainActivityInterface.addSetItem(mainActivityInterface.getCurrentSet().getSetItems().size()-1);

            mainActivityInterface.getCurrentSet().setCurrentSetString(mainActivityInterface.getSetActions().getSetAsPreferenceString(mainActivityInterface));
            mainActivityInterface.getPreferences().setMyPreferenceString("setCurrent", mainActivityInterface.getCurrentSet().getCurrentSetString());

            // Update the set menu
            //mainActivityInterface.updateSetList();
            mainActivityInterface.refreshSetList();

            mainActivityInterface.getShowToast().doIt(c.getString(R.string.success));

        } else {
            // Incorrect folder/filename
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.error));
        }
    }

    // The getters
    public String getCreateType() {
        return createType;
    }
    public boolean getCreateLoop() {
        return createLoop;
    }
    public String getCreateTitle() {
        return createTitle;
    }
    public String getCreateTime() {
        return createTime;
    }
    public boolean getCreateReusable() {
        return createReusable;
    }
    public String getCreateContent() {
        return createContent;
    }
    public String getCreateImages() {
        return createImages;
    }

    // The setters
    public void setCreateType(String createType) {
        this.createType = createType;
    }
    public void setCreateLoop(boolean createLoop) {
        this.createLoop = createLoop;
    }
    public void setCreateTitle(String createTitle) {
        this.createTitle = createTitle;
    }
    public void setCreateTime(String createTime) {
        // Only keep numbers
        createTime = createTime.replaceAll("[\\D]", "");
        this.createTime = createTime;
    }
    public void setCreateReusable(boolean createReusable) {
        this.createReusable = createReusable;
    }
    public void setCreateContent(String createContent) {
        this.createContent = createContent;
    }
    public void setCreateImages(String createImages) {
        this.createImages = createImages;
    }
}
