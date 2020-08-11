package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

class SetActions {

    public interface MyInterface {
        void doMoveSection();
        void loadSong();
    }

    private boolean encodedimage;
    private String title = "", author = "", lyrics = "", hymn_number = "";
    private String custom_notes, user1 = "", user2 = "", user3 = "", aka = "", key_line = "";
    private XmlPullParser xpp;

    ArrayList<String> listAllSets(Context c, Preferences preferences, StorageAccess storageAccess) {
        return storageAccess.listFilesInFolder(c, preferences, "Sets", "");
    }
    ArrayList<String> listSetCategories(Context c, ArrayList<String> allsets) {
        // Look for the different categories
        ArrayList<String> cats = new ArrayList<>();
        for (String set:allsets) {
            if (set.contains("__")) {
                // This is a set name with a category before the __
                String[] bit = set.split("__");
                if (!cats.contains(bit[0])) {
                    // This category isn't already in the array, so add it
                    cats.add(bit[0]);
                }
            }
        }
        // Sort the categories alphabetically
        if (!FullscreenActivity.sortAlphabetically) {
            Collections.sort(cats);
            Collections.reverse(cats);
        } else {
            Collections.sort(cats);
        }

        // Add the main category to the beginning (for sets without __)
        cats.add(0,c.getString(R.string.mainfoldername));

        return cats;
    }
    ArrayList<String> listFilteredSets(Context c, ArrayList<String> allsets, String cat) {
        if (cat==null || cat.equals(c.getString(R.string.mainfoldername)) || cat.equals("MAIN")) {
            cat = "";
        }

        ArrayList<String> filtered = new ArrayList<>();

        // Go through each item in the sets category and add it if it is the category
        // "" is sent for the mainfoldername = no category
        for (String found:allsets) {
            if (cat.equals("") && !found.contains("__")) {
                // Add sets that are in the MAIN/unspecified category
                filtered.add(found);
            } else if (found.startsWith(cat+"__")) {
                filtered.add(found.replace(cat+"__",""));
            }
        }
        Collator collator = Collator.getInstance(StaticVariables.locale);
        collator.setStrength(Collator.SECONDARY);

        // Sort the categories alphabetically
        if (!FullscreenActivity.sortAlphabetically) {
            Collections.sort(filtered,collator);
            Collections.reverse(filtered);
        } else {
            Collections.sort(filtered,collator );
        }
        return filtered;
    }

    private String currentSet;

    void loadASet(Context c, Preferences preferences, StorageAccess storageAccess) throws XmlPullParserException, IOException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        xpp = factory.newPullParser();
        Uri uri = storageAccess.getUriForItem(c, preferences, "Sets", "", StaticVariables.settoload);
        String utf = storageAccess.getUTFEncoding(c,uri);
        InputStream inputStream = storageAccess.getInputStream(c,uri);
        xpp.setInput(inputStream, utf);

        // Initialise the current set to the stored one (may be loading multiple sets, so not necessarily empty
        // The set is initialised before this routine runs for the first time

        currentSet = preferences.getMyPreferenceString(c,"setCurrent","");

        int eventType;
        eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xpp.getName().equals("slide_group")) {
                    // Is this a song?
                    switch (xpp.getAttributeValue(null, "type")) {
                        case "song":
                            // Get song
                            getSong(c,preferences);
                            break;
                        case "scripture":
                            // Get Scripture
                            getScripture(c, preferences, storageAccess);
                            break;
                        case "custom":
                            // Get Custom (Note or slide or variation)
                            getCustom(c, preferences, storageAccess);
                            break;
                        case "image":
                            // Get the Image(s)
                            getImage(c, preferences, storageAccess);
                            break;
                    }
                }
            }
            try {
                eventType = xpp.next();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("d","End of XML file");
            }
        }

        preferences.setMyPreferenceString(c, "setCurrent", currentSet);
        // Save the loaded set contents so we can compare to the current set to see if it has changed
        // On the set list popup it will compare these and display unsaved if it is different.
        //preferences.setMyPreferenceString(c,"setCurrentBeforeEdits",preferences.getMyPreferenceString(c,"setCurrent",""));
        preferences.setMyPreferenceString(c, "setCurrentBeforeEdits", currentSet);
        //Log.d("SetActions","setCurrent="+preferences.getMyPreferenceString(c,"setCurrent",""));
    }

    void prepareSetList(Context c, Preferences preferences) {
        try {
            StaticVariables.mSet = null;
            StaticVariables.mSetList = null;

            // Remove any blank set entries that shouldn't be there
            String setparse = preferences.getMyPreferenceString(c,"setCurrent","");
            setparse =  setparse.replace("$**__**$", "");

            // Add a delimiter between songs
            setparse = setparse.replace("_**$$**_", "_**$%%%$**_");

            // Break the saved set up into a new String[]
            StaticVariables.mSet = setparse.split("%%%");

            // Fix any MAIN folder saved in set
            for (int s=0; s<StaticVariables.mSet.length; s++) {
                StaticVariables.mSet[s] = StaticVariables.mSet[s].replace("MAIN/","");
                StaticVariables.mSet[s] = StaticVariables.mSet[s].replace(c.getString(R.string.mainfoldername)+"/","");
            }

            StaticVariables.mSetList = StaticVariables.mSet.clone();

            StaticVariables.setSize = StaticVariables.mSetList.length;

            // Get rid of tags before and after folder/filenames
            for (int x = 0; x < StaticVariables.mSetList.length; x++) {
                StaticVariables.mSetList[x] = StaticVariables.mSetList[x]
                        .replace("$**_", "");
                StaticVariables.mSetList[x] = StaticVariables.mSetList[x]
                        .replace("_**$", "");
            }

            StaticVariables.mTempSetList = new ArrayList<>();
            StaticVariables.mTempSetList.addAll(Arrays.asList(StaticVariables.mSetList));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void indexSongInSet() {
        try {
            if (StaticVariables.mSet!=null && StaticVariables.mSetList!=null && StaticVariables.whatsongforsetwork!=null) {
                boolean alreadythere = false;
                if (StaticVariables.indexSongInSet > -1 && StaticVariables.indexSongInSet < StaticVariables.mSetList.length) {
                    if (StaticVariables.mSetList[StaticVariables.indexSongInSet].contains(StaticVariables.whatsongforsetwork)) {
                        alreadythere = true;
                    }
                }

                StaticVariables.setSize = StaticVariables.mSetList.length;

                if (alreadythere) {
                    if (StaticVariables.indexSongInSet > 0) {
                        StaticVariables.previousSongInSet = StaticVariables.mSetList[StaticVariables.indexSongInSet - 1];
                    } else {
                        StaticVariables.previousSongInSet = "";
                    }
                    if (StaticVariables.indexSongInSet < StaticVariables.mSetList.length - 1) {
                        StaticVariables.nextSongInSet = StaticVariables.mSetList[StaticVariables.indexSongInSet + 1];
                    } else {
                        StaticVariables.nextSongInSet = "";
                    }

                } else {
                    StaticVariables.previousSongInSet = "";
                    StaticVariables.nextSongInSet = "";
                }

                // Go backwards through the setlist - this finishes with the first occurrence
                // Useful for duplicate items, otherwise it returns the last occurrence
                // Not yet tested, so left

                StaticVariables.mSet = StaticVariables.mSetList.clone();

                if (!alreadythere) {
                    for (int x = 0; x < StaticVariables.setSize; x++) {
//		for (int x = FullscreenActivity.setSize-1; x<1; x--) {
                        if (StaticVariables.mSet[x].contains(StaticVariables.whatsongforsetwork) ||
                                StaticVariables.mSet[x].contains("**" + StaticVariables.whatsongforsetwork)) {
                            StaticVariables.indexSongInSet = x;
                            if (x > 0) {
                                StaticVariables.previousSongInSet = StaticVariables.mSet[x - 1];
                            }
                            if (x != StaticVariables.setSize - 1) {
                                StaticVariables.nextSongInSet = StaticVariables.mSet[x + 1];
                            }

                        }
                    }
                }
            } else {
                StaticVariables.indexSongInSet = -1;
                StaticVariables.previousSongInSet = "";
                StaticVariables.nextSongInSet = "";
            }
            /*// Initialise variables if they are null
            if (StaticVariables.mSetList == null) {
                StaticVariables.mSetList = new String[1];
                StaticVariables.mSetList[0] = "";
            }

            if (StaticVariables.mSet == null) {
                StaticVariables.mSet = new String[1];
                StaticVariables.mSet[0] = "";
            }

            if (StaticVariables.whatsongforsetwork == null) {
                StaticVariables.whatsongforsetwork = "";
            }
            // See if we are already there!*/


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void songIndexClickInSet() {
        if (StaticVariables.indexSongInSet == 0) {
            // Already first item
            StaticVariables.previousSongInSet = "";
        } else {
            StaticVariables.previousSongInSet = StaticVariables.mSetList[StaticVariables.indexSongInSet - 1];
        }

        if (StaticVariables.indexSongInSet == (StaticVariables.setSize - 1)) {
            // Last item
            StaticVariables.nextSongInSet = "";
        } else {
            StaticVariables.nextSongInSet = StaticVariables.mSetList[StaticVariables.indexSongInSet + 1];
        }
        FullscreenActivity.whichDirection = "R2L";
    }

    void saveSetMessage(Context c, Preferences preferences,
                        StorageAccess storageAccess, ProcessSong processSong) {
        FullscreenActivity.whattodo = "";
        if (StaticVariables.mSetList!=null && StaticVariables.mSetList.length>0) {
            CreateNewSet createNewSet = new CreateNewSet();
            if (!createNewSet.doCreation(c, preferences, storageAccess, processSong)) {
                StaticVariables.myToastMessage = c.getString(R.string.notset);
            }
        } else if (StaticVariables.mSetList!=null) {
            StaticVariables.myToastMessage = c.getString(R.string.notset);
        }

        if (StaticVariables.myToastMessage.equals("yes")) {
            StaticVariables.myToastMessage = c.getString(R.string.save)
                    + " - " + c.getString(R.string.ok);
        }
    }

    void clearSet(Context c, Preferences preferences) {
        preferences.setMyPreferenceString(c,"setCurrent","");
        StaticVariables.mSetList = null;
        StaticVariables.setView = false;

        preferences.setMyPreferenceString(c,"setCurrentLastName","");

        StaticVariables.myToastMessage = c.getString(R.string.set_new) + " " +
                c.getString(R.string.ok);
    }

    void deleteSet(Context c, Preferences preferences, StorageAccess storageAccess) {
        String[] tempsets = StaticVariables.setnamechosen.split("%_%");
        StaticVariables.myToastMessage = "";
        StringBuilder message = new StringBuilder();
        for (String tempfile:tempsets) {
            if (tempfile!=null && !tempfile.equals("") && !tempfile.isEmpty()) {
                Uri uri = storageAccess.getUriForItem(c, preferences, "Sets", "", tempfile);
                if (storageAccess.deleteFile(c,uri)) {
                    message.append(tempfile).append(", ");
                }
            }
        }
        StaticVariables.myToastMessage = message.toString();
        if (StaticVariables.myToastMessage.length()>2) {
            StaticVariables.myToastMessage = StaticVariables.myToastMessage.substring(0, StaticVariables.myToastMessage.length() - 2);
        }
        StaticVariables.myToastMessage = StaticVariables.myToastMessage + " " + c.getString(R.string.sethasbeendeleted);
    }

    String getSongForSetWork(Context c) {
        String val;
        if (StaticVariables.whichSongFolder.equals(c.getString(R.string.mainfoldername)) || StaticVariables.whichSongFolder.equals("MAIN") ||
                StaticVariables.whichSongFolder.equals("")) {
            val = StaticVariables.songfilename;
        } else if (StaticVariables.whichSongFolder.contains("Scripture/_cache")) {
            val = c.getResources().getString(R.string.scripture) + "/" + StaticVariables.songfilename;
        } else if (StaticVariables.whichSongFolder.contains("Slides/_cache")) {
            val = c.getResources().getString(R.string.slide) + "/" + StaticVariables.songfilename;
        } else if (StaticVariables.whichSongFolder.contains("Notes/_cache")) {
            val = c.getResources().getString(R.string.note) + "/" + StaticVariables.songfilename;
        } else if (StaticVariables.whichSongFolder.contains("Images/_cache")) {
            val = c.getResources().getString(R.string.image) + "/" + StaticVariables.songfilename;
        } else if (StaticVariables.whichSongFolder.contains("Variations")) {
            val = c.getResources().getString(R.string.variation) + "/" + StaticVariables.songfilename;
        } else {
            val = StaticVariables.whichSongFolder + "/"
                    + StaticVariables.songfilename;
        }
        StaticVariables.whatsongforsetwork = val;
        return val;
    }

    String whatToLookFor(Context c, String folder, String filename) {
        String whattolookfor;
        if (folder.equals("") || folder.equals(c.getString(R.string.mainfoldername)) || folder.equals("MAIN")) {
            whattolookfor = "$**_" + filename + "_**$";
        } else if (folder.startsWith("**"+c.getString(R.string.variation)) ||
                folder.startsWith("../Variations")) {
            whattolookfor = "$**_**" + c.getString(R.string.variation) + "/" + filename + "_**$";
        } else if (folder.startsWith("**"+c.getString(R.string.note)) ||
                folder.startsWith("../Notes")) {
            whattolookfor = "$**_**" + c.getString(R.string.note) + "/" + filename + "_**$";
        } else if (folder.startsWith("**"+c.getString(R.string.slide)) ||
                folder.startsWith("../Slides")) {
            whattolookfor = "$**_**" + c.getString(R.string.slide) + "/" + filename + "_**$";
        } else if (folder.startsWith("**"+c.getString(R.string.image_slide)) ||
                folder.startsWith("../Images")) {
            whattolookfor = "$**_**" + c.getString(R.string.image_slide) + "/" + filename + "_**$";
        } else {
            whattolookfor = "$**_" + folder + "/" + filename + "_**$";
        }
        return whattolookfor;
    }

    private boolean isCustomSlide(String folder) {
        return folder.startsWith("Notes/") || folder.startsWith("Note/") || folder.startsWith("..") ||
                folder.startsWith("Slides/") || folder.startsWith("Slide/") ||
                folder.startsWith("Images/") || folder.startsWith("Image/") ||
                folder.startsWith("Scriptures/") || folder.startsWith("Scripture/") ||
                folder.startsWith("Variations/") || folder.startsWith("Variation/");
    }

    boolean isSongInSet(Context c, Preferences preferences) {
        if (StaticVariables.setSize > 0) {
            // Get the name of the song to look for (including folders if need be)
            String songforsetwork;
            if (isCustomSlide(StaticVariables.whichSongFolder)) {
                songforsetwork = "$**_**" + getSongForSetWork(c) + "_**$";
            } else {
                songforsetwork ="$**_" + getSongForSetWork(c) + "_**$";
            }
            songforsetwork = fixIsInSetSearch(songforsetwork);
            String currset = preferences.getMyPreferenceString(c,"setCurrent","");

            if (StaticVariables.setView && currset.contains(songforsetwork)) {
                // If we are currently in set mode, check if the new song is there, in which case do nothing else
                indexSongInSet();
                return true;

            } else if (StaticVariables.setView && !currset.contains(songforsetwork)) {
                // If we are currently in set mode, but the new song isn't there, leave set mode
                StaticVariables.setView = false;
                StaticVariables.previousSongInSet = "";
                StaticVariables.nextSongInSet = "";
                StaticVariables.indexSongInSet = 0;
                return false;

            } else if (!StaticVariables.setView && currset.contains(songforsetwork)) {
                // If we aren't currently in set mode and the new song is there, enter set mode and get the index
                StaticVariables.setView = true;
                StaticVariables.previousSongInSet = "";
                StaticVariables.nextSongInSet = "";

                // Get the song index
                indexSongInSet();
                return true;

            } else if (!currset.contains(songforsetwork)) {
                // The new song isn't in the set, so leave set mode and reset index
                StaticVariables.setView = false;
                StaticVariables.previousSongInSet = "";
                StaticVariables.nextSongInSet = "";
                StaticVariables.indexSongInSet = 0;
                return false;
            }

        } else {
            // User wasn't in set view, or the set was empty
            // Switch off the set view (buttons in action bar)
            StaticVariables.setView = false;
            StaticVariables.previousSongInSet = "";
            StaticVariables.nextSongInSet = "";
            StaticVariables.indexSongInSet = 0;
            return false;
        }
        return false;
    }

    void emptyCacheDirectories(Context c, Preferences preferences, StorageAccess storageAccess) {
        storageAccess.wipeFolder(c, preferences, "Scripture", "_cache");
        storageAccess.wipeFolder(c, preferences, "Slides", "_cache");
        storageAccess.wipeFolder(c, preferences, "Notes", "_cache");
        storageAccess.wipeFolder(c, preferences, "Images", "_cache");
        storageAccess.wipeFolder(c, preferences, "Variations", "");

        // Create them again if they need to be
        storageAccess.createOrCheckRootFolders(c, null, preferences);
    }

    private void writeTempSlide(String where, String what, Context c, Preferences preferences, StorageAccess storageAccess) {
        // Fix the custom name so there are no illegal characters
        what = storageAccess.safeFilename(what);
        String set_item;
        String foldername;
        String subfoldername;
        String setprefix;

        //Log.d("SetActions","where="+where+"  what="+what);
        if (where.equals(c.getResources().getString(R.string.scripture))) {
            foldername = "Scripture";
            subfoldername = "_cache";
            setprefix  = "$**_**" + c.getResources().getString(R.string.scripture) + "/";
        } else if (where.equals(c.getResources().getString(R.string.slide))) {
            foldername = "Slides";
            subfoldername = "_cache";
            setprefix  = "$**_**" + c.getResources().getString(R.string.slide) + "/";
        } else if (where.equals(c.getResources().getString(R.string.image))) {
            foldername = "Images";
            subfoldername = "_cache";
            setprefix  = "$**_**" + c.getResources().getString(R.string.image) + "/";
        } else if (where.equals(c.getResources().getString(R.string.variation))) {
            foldername = "Variations";
            subfoldername = "";
            setprefix  = "$**_**" + c.getResources().getString(R.string.variation) + "/";
        } else {
            foldername = "Notes";
            subfoldername = "_cache";
            setprefix  = "$**_**" + c.getResources().getString(R.string.note) + "/";
        }

        Uri uri = storageAccess.getUriForItem(c, preferences, foldername, subfoldername, what);

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null,
                foldername, subfoldername, what);

        OutputStream outputStream = storageAccess.getOutputStream(c, uri);
        set_item = setprefix + what + "_**$";

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
            my_NEW_XML = new String(data, StandardCharsets.UTF_8);
        }

        storageAccess.writeFileFromString(my_NEW_XML,outputStream);
        //String val = preferences.getMyPreferenceString(c,"setCurrent","") + set_item;
        //preferences.setMyPreferenceString(c,"setCurrent",val);
        currentSet = currentSet + set_item;
    }

    private void getSong(Context c, Preferences preferences) {
        try {
            // Get path and remove leading /
            String p_name = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"path"));
            String s_name = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name"));
            if (p_name.startsWith("/")) {
                p_name = p_name.replaceFirst("/","");
            }
            if (p_name.endsWith("/") && p_name.length()>1) {
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

            //String val = preferences.getMyPreferenceString(c,"setCurrent","") + "$**_" + location + "_**$";
            currentSet = currentSet + "$**_" + location + "_**$";
            //preferences.setMyPreferenceString(c, "setCurrent", val);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            xpp.nextTag();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getScripture(Context c, Preferences preferences, StorageAccess storageAccess) throws IOException, XmlPullParserException {
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

        writeTempSlide(c.getResources().getString(R.string.scripture), scripture_title, c, preferences, storageAccess);

        xpp.nextTag();
     }

    private void getCustom(Context c, Preferences preferences, StorageAccess storageAccess) throws IOException, XmlPullParserException {
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

        writeTempSlide(noteorslide, custom_name, c, preferences, storageAccess);

    }

    private void getImage(Context c, Preferences preferences, StorageAccess storageAccess) throws IOException, XmlPullParserException {
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

        while (!allimagesdone) {
            // Keep going until we get to the end of the document
            if (eventType == XmlPullParser.START_TAG) {
                if (xpp != null) {
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
                        case "image":
                            image_content = xpp.nextText();
                            hymn_number_imagecode.append(image_content.trim()).append("XX_IMAGE_XX");
                            encodedimage = true;
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

                                Uri uri = storageAccess.getUriForItem(c, preferences, "Images", "_cache",
                                        storageAccess.safeFilename(image_title.toString()) + imagenums + image_type);

                                // Check the uri exists for the outputstream to be valid
                                storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null,
                                        "Images", "_cache", storageAccess.safeFilename(image_title.toString()) + imagenums + image_type);

                                OutputStream outputStream = storageAccess.getOutputStream(c, uri);
                                byte[] decodedString = Base64.decode(image_content, Base64.DEFAULT);
                                storageAccess.writeFileFromDecodedImageString(outputStream, decodedString);
                                image_content = "";
                                slide_images.append(uri.toString()).append("\n");
                                slide_image_titles.append("[").append(c.getResources().getString(R.string.image))
                                        .append("_").append(imagenums + 1).append("]\n").append(uri.toString()).append("\n\n");
                                imagenums++;
                                encodedimage = false;
                            }
                            break;
                    }
                }
            }
            allimagesdone = eventType==XmlPullParser.END_TAG && xpp!=null && xpp.getName()!=null &&
                    xpp.getName().equals("slide_group");
            if (xpp != null) {
                eventType = xpp.next();
            }
        }

        if (image_title.toString().equals("")) {
            image_title = new StringBuilder(c.getResources().getString(R.string.image));
        }

        image_subtitle = fixNull(image_subtitle);
        image_seconds = fixNull(image_seconds);
        image_loop = fixNull(image_loop);
        image_name = fixNull(image_name);
        image_notes = fixNull(image_notes);

        title = image_title.toString();
        author = image_subtitle;
        user1 = image_seconds;
        user2 = image_loop;
        user3 = slide_images.toString().trim();
        aka = image_name;
        //hymn_number = hymn_number_imagecode.toString();
        hymn_number = ""; // Not saving the image here!
        key_line = image_notes;
        lyrics = slide_image_titles.toString().trim();
        writeTempSlide(c.getResources().getString(R.string.image), title, c, preferences, storageAccess);
    }

    private String fixNull(String s) {
        if (s==null) {
            s="";
        }
        return s;
    }

    void prepareFirstItem(Context c,Preferences preferences) {
        // If we have just loaded a set, and it isn't empty,  load the first item
        if (StaticVariables.mSetList.length>0) {
            StaticVariables.whatsongforsetwork = StaticVariables.mSetList[0];
            StaticVariables.setView = true;

            //Log.d("SetActions","whatsongforsetwork="+StaticVariables.whatsongforsetwork);
            FullscreenActivity.linkclicked = StaticVariables.mSetList[0];
            FullscreenActivity.pdfPageCurrent = 0;

            // Get the song and folder names from the item clicked in the set list
            getSongFileAndFolder(c);
            preferences.setMyPreferenceString(c,"whichSongFolder",StaticVariables.whichSongFolder);
            preferences.setMyPreferenceString(c, "songfilename",StaticVariables.songfilename);

            // Get the index of the song in the current set
            indexSongInSet();
        }
    }

    void getSongFileAndFolder(Context c) {
        //Log.d("SetActions","linkclicked="+FullscreenActivity.linkclicked);
        if (!FullscreenActivity.linkclicked.contains("/")) {
            FullscreenActivity.linkclicked = "/" + FullscreenActivity.linkclicked;
        }

        if (FullscreenActivity.linkclicked.equals("/")) {
            // There was no song clicked, so just reload the current one
            if (StaticVariables.whichSongFolder.equals(c.getString(R.string.mainfoldername)) || StaticVariables.whichSongFolder.equals("MAIN") ||
                    StaticVariables.whichSongFolder.equals("")) {
                FullscreenActivity.linkclicked = "/"+ StaticVariables.songfilename;
            } else {
                FullscreenActivity.linkclicked = StaticVariables.whichSongFolder + "/" +
                        StaticVariables.songfilename;
            }
        }

        // The song is the bit after the last /
        int songpos = FullscreenActivity.linkclicked.lastIndexOf("/");
        if (songpos==0) {
            // Empty folder
            StaticVariables.whichSongFolder = c.getString(R.string.mainfoldername);
        } else {
            StaticVariables.whichSongFolder = FullscreenActivity.linkclicked.substring(0,songpos);
        }

        if (songpos>=FullscreenActivity.linkclicked.length()) {
            // Empty song
            StaticVariables.songfilename = "";
        } else {
            StaticVariables.songfilename = FullscreenActivity.linkclicked.substring(songpos + 1);
        }

        if (StaticVariables.whichSongFolder.equals("")) {
            StaticVariables.whichSongFolder = c.getString(R.string.mainfoldername);
        }

        // If the folder length isn't 0, it is a folder
        if (StaticVariables.whichSongFolder.length() > 0 &&
                StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.scripture)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.variation)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.image)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.slide)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.note))) {
            StaticVariables.whichSongFolder = "../Scripture/_cache";

        } else if (StaticVariables.whichSongFolder.length() > 0 &&
                StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.slide)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.variation)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.image)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.note)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.scripture))) {
            StaticVariables.whichSongFolder = "../Slides/_cache";

        } else if (StaticVariables.whichSongFolder.length() > 0 &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.variation)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.slide)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.image)) &&
                StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.note)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.scripture))) {
            StaticVariables.whichSongFolder = "../Notes/_cache";

        } else if (StaticVariables.whichSongFolder.length() > 0 &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.variation)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.slide)) &&
                StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.image)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.note)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.scripture))) {
            StaticVariables.whichSongFolder = "../Images/_cache";

        } else if (StaticVariables.whichSongFolder.length() > 0 &&
                StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.variation)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.slide)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.image)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.note)) &&
                !StaticVariables.whichSongFolder.contains("**"+c.getResources().getString(R.string.scripture))) {
            StaticVariables.whichSongFolder = "../Variations";

        }
    }

    void doMoveInSet(Context c, Preferences preferences) {
        MyInterface mListener = (MyInterface) c;
        boolean justmovingsections = false;

        // If we are in Stage Mode, check the sections first
        if (StaticVariables.whichMode.equals("Stage")) {

            // Might be staying on the same song but moving section
            if (StaticVariables.setMoveDirection.equals("back")) {
                if (StaticVariables.currentSection > 0) {
                    justmovingsections = true;
                    mListener.doMoveSection();
                } else {
                    StaticVariables.currentSection = 0;
                }
            } else if (StaticVariables.setMoveDirection.equals("forward")) {
                if (StaticVariables.currentSection< StaticVariables.songSections.length-1) {
                    justmovingsections = true;
                    mListener.doMoveSection();
                } else {
                    StaticVariables.currentSection = 0;
                }
            }
        }

        if (!justmovingsections) {
            // Moving to a different song
            if (StaticVariables.setMoveDirection.equals("back")) {
                if (StaticVariables.indexSongInSet>0) {
                    StaticVariables.indexSongInSet -= 1;
                    FullscreenActivity.linkclicked = StaticVariables.mSetList[StaticVariables.indexSongInSet];
                    StaticVariables.whatsongforsetwork = FullscreenActivity.linkclicked;
                    if (FullscreenActivity.linkclicked == null) {
                        FullscreenActivity.linkclicked = "";
                        StaticVariables.whatsongforsetwork = "";
                    }
                }

            } else if (StaticVariables.setMoveDirection.equals("forward")) {
                if (StaticVariables.indexSongInSet< StaticVariables.mSetList.length-1) {
                    StaticVariables.indexSongInSet += 1;
                    FullscreenActivity.linkclicked = StaticVariables.mSetList[StaticVariables.indexSongInSet];
                    StaticVariables.whatsongforsetwork = FullscreenActivity.linkclicked;
                    if (FullscreenActivity.linkclicked == null) {
                        FullscreenActivity.linkclicked = "";
                        StaticVariables.whatsongforsetwork = "";
                    }
                }
            }

            StaticVariables.setMoveDirection = "";

            // Get the song and folder names from the item clicked in the set list
            getSongFileAndFolder(c);
            preferences.setMyPreferenceString(c,"whichSongFolder",StaticVariables.whichSongFolder);
            preferences.setMyPreferenceString(c, "songfilename",StaticVariables.songfilename);

            StaticVariables.setMoveDirection = "";
            mListener.loadSong();
        }

    }

    String fixIsInSetSearch(String s) {
        if (s.contains("**_Variations/")) {
            s = s.replace("**_Variations/","**_**Variation/");
        } else if (s.contains("**_Variation/")) {
            s = s.replace("**_Variation/","**_**Variation/");
        }
        return s;
    }
}