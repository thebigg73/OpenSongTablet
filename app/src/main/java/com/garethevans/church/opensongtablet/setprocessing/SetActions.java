package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;
import java.util.Collections;

// This class interacts with the CurrentSet object and does the processing, etc.

public class SetActions {

    private final String itemStart = "$**_", itemEnd = "_**$",
            keyStart = "_***", keyEnd = "***_", setCategorySeparator = "___";

    public void initialiseTheSet(MainActivityInterface mainActivityInterface) {
        mainActivityInterface.getCurrentSet().initialiseTheSet();
    }

    // Convert between the currentSet string in preferences and the arrayLists
    public void preferenceStringToArrays(Context c, MainActivityInterface mainActivityInterface) {
        // Initialise the set arraylists
        initialiseTheSet(mainActivityInterface);

        // Split the set string in preferences into an arraylist for each item
        buildSetItemArray(mainActivityInterface,
                mainActivityInterface.getPreferences().getMyPreferenceString(c,"setCurrent",""));

        // Now build the individual items in the set with the folder, filename and key separate
        buildSetArraysFromItems(c,mainActivityInterface);
    }
    private void buildSetItemArray(MainActivityInterface mainActivityInterface, String set) {
        // Sets may or may not have the preferred key embedded in them (old sets before V6 will not)
        // $**_folder1/song1_**$$**_folder2/song2_**A**__**$

        // Set the initial set string (so we can look for changes later for saving)
        mainActivityInterface.getCurrentSet().setCurrentSetString(set);

        // Now split the set string into individual entries
        set = set.replace(itemEnd+itemStart,"\n");
        set = set.replace(itemStart,"");
        set = set.replace(itemEnd,"");
        String[] setItems = set.split("\n");
        for (String setItem:setItems) {
            if (!setItem.isEmpty()) {
                if (!setItem.contains(keyStart) && !setItem.contains(keyEnd)) {
                    // No key value is here, so add a blank entry
                    setItem = setItem + keyStart + keyEnd;
                }
                mainActivityInterface.getCurrentSet().addSetItem(itemStart + setItem + itemEnd);
            }
        }
    }
    public void buildSetArraysFromItems(Context c, MainActivityInterface mainActivityInterface) {
        // Each set item is a stored with the $**_folder/filename_**key**__**$
        // We now parse this to get the values separately
        for (int x=0; x<mainActivityInterface.getCurrentSet().getSetItems().size(); x++) {
            String setItem = mainActivityInterface.getCurrentSet().getItem(x);
            String key = "", folder = "", item = setItem;
            setItem = setItem.replace(itemStart,"");
            setItem = setItem.replace(itemEnd,"");
            // Check for embedded key
            if (setItem.contains(keyStart) && setItem.contains(keyEnd)) {
                // A key has been included (so auto transpose is allowed)
                key = setItem.substring(setItem.indexOf(keyStart) + 4, setItem.indexOf(keyEnd));
                setItem = setItem.replace(keyStart+key+keyEnd, "");
            } else {
                // No key was specified, so keep it empty (i.e. just use song default)
                key = "";
            }

            // Get the folder
            if (setItem.startsWith("/")) {
                setItem = setItem.replaceFirst("/", "");
            }
            if (setItem.contains("/")) {
                folder = setItem.substring(0, setItem.lastIndexOf("/"));
                setItem = setItem.substring(setItem.lastIndexOf("/"));
                setItem = setItem.replace("/", "");
            } else {
                folder = c.getString(R.string.mainfoldername);
            }

            // Now we have the folder and filename, we can update default keys from the database
            if (key.isEmpty()) {
                Log.d("SetActions","folder="+folder+"  filename="+setItem+" key not set, checking database...");
                // If this is an image or pdf, check the nonOpenSongSQLite database
                // Otherwise use the normal database
                if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("imageorpdf",setItem)) {
                    key = mainActivityInterface.getNonOpenSongSQLiteHelper().getKey(c,mainActivityInterface,folder,setItem);
                } else {
                    key = mainActivityInterface.getSQLiteHelper().getKey(c,mainActivityInterface,folder,setItem);
                }
            }

            // Build the set item.  Useful for shuffling and rebuilding and searching
            item = itemStart + folder + "/" + setItem + keyStart + key + keyEnd + itemEnd;
            mainActivityInterface.getCurrentSet().setItem(x,item);

            // Put the values into the set arrays (the filename is what is left)
            mainActivityInterface.getCurrentSet().addSetValues(folder, setItem, key);
        }
    }
    public String getSetAsPreferenceString(MainActivityInterface mainActivityInterface) {
        // Build the set list into a string that can be saved to preferences
        // Use the arrays for folder, song title and key.  These should match!
        // As a precaution, each item is in a try/catch incase the arrays are different sizes
        StringBuilder stringBuilder = new StringBuilder();
        for (int x = 0; x<mainActivityInterface.getCurrentSet().getSetFilenames().size(); x++) {
            if (!mainActivityInterface.getCurrentSet().getFilename(x).isEmpty())
                try {
                    stringBuilder.append(itemStart).
                            append(mainActivityInterface.getCurrentSet().getFolder(x)).
                            append("/").
                            append(mainActivityInterface.getCurrentSet().getFilename(x)).
                            append(keyStart).
                            append(mainActivityInterface.getCurrentSet().getKey(x)).
                            append(keyEnd).
                            append(itemEnd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return stringBuilder.toString();
    }

    // Get a reference string for thisSong for working with the currentSet string in preferences
    public String getSongForSetWork(Context c, Song thisSong) {
        return itemStart + c.getString(R.string.mainfoldername) + "/" + thisSong.getFilename() +
                keyStart + thisSong.getKey() + keyEnd + itemEnd;
    }

    public int indexSongInSet(MainActivityInterface mainActivityInterface, Song thisSong) {
        // TODO decide if we should find the first or last entry
        int positionFilename = mainActivityInterface.getCurrentSet().getSetFilenames().indexOf(thisSong.getFilename());
        int positionFolder = mainActivityInterface.getCurrentSet().getSetFolders().indexOf(thisSong.getFolder());
        if (positionFilename>-1 && positionFilename==positionFolder) {
            return positionFilename;
        } else {
            return -1;
        }
    }

    public void shuffleSet(Context c, MainActivityInterface mainActivityInterface) {
        // Shuffle the currentSet item array - all entries are like $$_folder/filename_**key**__$$
        Collections.shuffle(mainActivityInterface.getCurrentSet().getSetItems());

        // Now reset the folder, filename and key arrays as we will rebuild them
        // We don't initialise all arrays as we need to keep the currentSet one
        mainActivityInterface.getCurrentSet().initialiseTheSpecifics();

        // Now build the individual values from the set item array which we shuffled
        buildSetArraysFromItems(c,mainActivityInterface);

        // Now build the modified set string for comparision for saving
        mainActivityInterface.getCurrentSet().setCurrentSetString(getSetAsPreferenceString(mainActivityInterface));
    }

    public void checkMissingKeys(Context c, MainActivityInterface mainActivityInterface) {
        // Called once song indexing is complete
        // Some keys may not have been loaded to the database when they were first looked for
        // If there is an empty value, try again
        for (int x = 0; x<mainActivityInterface.getCurrentSet().getSetKeys().size(); x++) {
            String key = mainActivityInterface.getCurrentSet().getKey(x);
            if (key.isEmpty()) {
                String folder = mainActivityInterface.getCurrentSet().getFolder(x);
                String filename = mainActivityInterface.getCurrentSet().getFilename(x);

                Log.d("d","folder="+folder+"  filename="+filename+"  key="+key);
                if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("imageorpdf",filename)) {
                    mainActivityInterface.getCurrentSet().
                            setKey(x, mainActivityInterface.getNonOpenSongSQLiteHelper().
                                    getKey(c, mainActivityInterface, folder, filename));
                } else {
                    mainActivityInterface.getCurrentSet().
                            setKey(x, mainActivityInterface.getSQLiteHelper().
                                    getKey(c, mainActivityInterface, folder, filename));
                }
            }
        }
    }
    public void addToSet(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        mainActivityInterface.getCurrentSet().addSetItem(getSongForSetWork(c,thisSong));
        mainActivityInterface.getCurrentSet().addSetValues(thisSong.getFolder(),thisSong.getFilename(),thisSong.getKey());
    }

    public void removeFromSet(Context c, MainActivityInterface mainActivityInterface, int mypos) {
        mainActivityInterface.getCurrentSet().getSetItems().remove(mypos);
        mainActivityInterface.getCurrentSet().getSetFolders().remove(mypos);
        mainActivityInterface.getCurrentSet().getSetFilenames().remove(mypos);
        mainActivityInterface.getCurrentSet().getSetKeys().remove(mypos);
    }

    public void saveTheSet(Context c, MainActivityInterface mainActivityInterface) {
        mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrent",
                getSetAsPreferenceString(mainActivityInterface));
    }

    public String currentSetNameForMenu(Context c, MainActivityInterface mainActivityInterface) {
        // This decides on the set name to display as a title
        // If it is a new set (unsaved), it will be called 'current (unsaved)'
        // If it is a non-modified loaded set, it will be called 'set name'
        // If it is a modified, unsaved, loaded set, it will be called 'set name (unsaved)'

        String title;
        String lastSetName = mainActivityInterface.getPreferences().getMyPreferenceString(c, "setCurrentLastName", "");
        if (lastSetName == null || lastSetName.equals("")) {
            title = c.getString(R.string.set_current) +
                    " (" + c.getString(R.string.not_saved) + ")";
        } else {
            title = lastSetName.replace("__", "/");
            if (!mainActivityInterface.getPreferences().getMyPreferenceString(c, "setCurrent", "")
                    .equals(mainActivityInterface.getPreferences().getMyPreferenceString(c, "setCurrentBeforeEdits", ""))) {
                title += " (" + c.getString(R.string.not_saved) + ")";
            }
        }
        return title;
    }

    public String whatToLookFor(Song thisSong) {
        // This deals with custom locations (variations, notes, slides, etc)
        String folder;
        if (thisSong.getFolder().startsWith("../")) {
            folder = thisSong.getFolder().replaceFirst("../","**");
        } else {
            folder = thisSong.getFolder();
        }
        return itemStart + folder + "/" + thisSong.getFilename() + keyStart + thisSong.getKey() +
                keyEnd + itemEnd;
    }

    public ArrayList<String> getAllSets(Context c, MainActivityInterface mainActivityInterface) {
        return mainActivityInterface.getStorageAccess().listFilesInFolder(c,
                mainActivityInterface, "Sets", "");
    }
    public ArrayList<String> setsInCategory(Context c, MainActivityInterface mainActivityInterface,
                                            ArrayList<String> allSets) {
        ArrayList<String> availableSets = new ArrayList<>();
        String category = mainActivityInterface.getPreferences().getMyPreferenceString(c,
                "whichSetCategory", c.getString(R.string.mainfoldername));
        boolean mainCategory = category.equals(c.getString(R.string.mainfoldername));

        for (String possibleSet:allSets) {
            if (mainCategory && !possibleSet.contains(setCategorySeparator)) {
                availableSets.add(possibleSet);
            } else if (possibleSet.contains(category+setCategorySeparator)) {
                availableSets.add(possibleSet);
            }
        }
        Collections.sort(availableSets);
        return availableSets;
    }
    public ArrayList<String> getCategories(Context c, ArrayList<String> allSets) {
        ArrayList<String> categories = new ArrayList<>();

        for (String setName:allSets) {
            if (setName.contains(setCategorySeparator)) {
                String category = setName.substring(0,setName.indexOf(setCategorySeparator));
                if (!categories.contains(category)) {
                    categories.add(category);
                }
            }
        }
        Collections.sort(categories);
        categories.add(0,c.getString(R.string.mainfoldername));
        return categories;
    }


    public String createSetXML(Context c, MainActivityInterface mainActivityInterface) {
        StringBuilder stringBuilder = new StringBuilder();

        // The starting of the xml file
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n").
                append("<set name=\"").
                append(mainActivityInterface.getProcessSong().parseToHTMLEntities(mainActivityInterface.getCurrentSet().getSetName())).
                append("\">\n<slide_groups>\n");

        // Now go through each set entry and build the appropriate xml
        for (int x = 0; x < mainActivityInterface.getCurrentSet().getSetItems().size(); x++) {
            String path = mainActivityInterface.getCurrentSet().getFolder(x);
            // If the path isn't empty, add a forward slash to the end
            if (!path.isEmpty()) {
                path = path + "/";
            }
            String name = mainActivityInterface.getCurrentSet().getFilename(x);
            boolean isImage = name.contains("**" + c.getString(R.string.image));
            boolean isVariation = name.contains("**" + c.getString(R.string.variation));
            boolean isScripture = name.contains("**" + c.getString(R.string.scripture));
            boolean isSlide = name.contains("**" + c.getString(R.string.slide));
            boolean isNote = name.contains("**" + c.getString(R.string.note));

            if (isImage) {
                // Adding an image
                Song tempSong = getTempSong(c,mainActivityInterface,"../Images/_cache", name);
                stringBuilder.append(buildImage(c,mainActivityInterface,tempSong));

            } else if (isScripture) {
                // Adding a scripture
                Song tempSong = getTempSong(c,mainActivityInterface, "../Scripture/_cache", name);
                stringBuilder.append(buildScripture(mainActivityInterface,tempSong));

            } else if (isVariation) {
                // Adding a variation
                Song tempSong = getTempSong(c,mainActivityInterface, "../Variations", name);
                stringBuilder.append(buildVariation(c,mainActivityInterface,tempSong));

            } else if (isSlide) {
                // Adding a slide
                Song tempSong = getTempSong(c,mainActivityInterface, "../Slides/_cache", name);
                stringBuilder.append(buildSlide(mainActivityInterface,tempSong));

            } else if (isNote) {
                // Adding a note
                Song tempSong = getTempSong(c,mainActivityInterface, "../Notes/_cache", name);
                stringBuilder.append(buildNote(c,mainActivityInterface,tempSong));
            } else {
                // Adding a song
                stringBuilder.append(buildSong(mainActivityInterface,path,name));
            }
        }
        // Now add the final part of the xml
        stringBuilder.append("</slide_groups>\n</set>");

        return stringBuilder.toString();

        /*// Write the string to the file
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Sets", "", mainActivityInterface.getCurrentSet().getSetFile());

        // Check the uri exists for the outputstream to be valid
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c, mainActivityInterface, uri, null, "Sets", "", mainActivityInterface.getCurrentSet().getSetFile());

        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, uri);
        if (mainActivityInterface.getStorageAccess().writeFileFromString(stringBuilder.toString(), outputStream)) {
            // Update the initial set string to match the one we just saved
            mainActivityInterface.getCurrentSet().setInitialSetString(mainActivityInterface.getCurrentSet().getCurrentSetString());

            // Update the last loaded set now it is saved.

            mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrentBeforeEdits",
                    mainActivityInterface.getPreferences().getMyPreferenceString(c,"setCurrent",""));

            return true;
        }

        mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrentLastName",mainActivityInterface.getCurrentSet().getSetFile());
*/
    }

    private Song getTempSong(Context c, MainActivityInterface mainActivityInterface, String folder, String name) {
        Song tempSong = mainActivityInterface.getProcessSong().initialiseSong(mainActivityInterface,folder,name);
        try {
            tempSong = mainActivityInterface.getLoadSong().doLoadSong(c, mainActivityInterface, tempSong,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempSong;
    }
    private StringBuilder buildSong(MainActivityInterface mainActivityInterface, String path, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <slide_group name=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(name))
                .append("\" type=\"song\" presentation=\"\" path=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(path))
                .append("\"/>\n");
        return sb;
    }
    private StringBuilder buildScripture(MainActivityInterface mainActivityInterface, Song tempSong) {
        StringBuilder sb = new StringBuilder();

        // The scripture is loaded to a new, temp song object
        String scripture_lyrics = tempSong.getLyrics();

        // Parse the lyrics into individual slides;
        scripture_lyrics = scripture_lyrics.replace("[]", "_SPLITHERE_");

        String[] mySlides = scripture_lyrics.split("_SPLITHERE_");

        String newname = tempSong.getFilename();
        if (!tempSong.getAuthor().equals("")) {
            newname = newname+"|"+ tempSong.getAuthor();
        }
        sb.append("  <slide_group type=\"scripture\" name=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(newname))
                .append("\" print=\"true\">\n")
                .append("    <title>")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getFilename()))
                .append("</title>\n");

        sb.append("    <slides>\n");

        for (String mySlide : mySlides) {
            if (mySlide != null && mySlide.length() > 0) {
                sb.append("      <slide>\n")
                        .append("      <body>")
                        .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(mySlide.trim()))
                        .append("</body>\n")
                        .append("      </slide>\n");
            }
        }
        sb.append("    </slides>\n")
                .append("    <subtitle></subtitle>\n")
                .append( "    <notes />\n")
                .append("  </slide_group>\n");

        return sb;
    }
    private StringBuilder buildVariation(Context c, MainActivityInterface mainActivityInterface, Song tempSong) {
        StringBuilder sb = new StringBuilder();

        // The variation is loaded to a new, temp song object
        // The entire song is copied to the notes as an encrypted string, and a simplified version is copied to the text

        String slide_lyrics = tempSong.getLyrics();
        try {
            byte[] data = mainActivityInterface.getProcessSong().getXML(c,mainActivityInterface,tempSong).getBytes(tempSong.getEncoding());
            slide_lyrics = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Prepare the slide contents so it remains compatible with the desktop app
        // Split the lyrics into individual lines
        String[] lyrics_lines = tempSong.getLyrics().split("\n");
        StringBuilder currentslide = new StringBuilder();
        ArrayList<String> newslides = new ArrayList<>();

        for (String thisline:lyrics_lines) {
            if (!thisline.equals("") && !thisline.startsWith(".") && !thisline.startsWith("[") && !thisline.startsWith(";")) {
                // Add the current line into the new slide
                // Replace any new line codes | with \n
                thisline = thisline.replace("||","\n");
                thisline = thisline.replace("---","\n");
                thisline = thisline.replace("|","\n");
                currentslide.append(thisline.trim()).append("\n");
            } else if (thisline.startsWith("[")) {
                // Save the current slide and create a new one
                currentslide = new StringBuilder(currentslide.toString().trim());
                newslides.add(currentslide.toString());
                currentslide = new StringBuilder();
            }
        }
        newslides.add(currentslide.toString());
        // Now go back through the currentslides and write the slide text
        StringBuilder slidetexttowrite = new StringBuilder();
        for (int z=0; z<newslides.size();z++) {
            if (!newslides.get(z).equals("")) {
                slidetexttowrite.append("      <slide>\n")
                        .append("        <body>")
                        .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(newslides.get(z).trim()))
                        .append("\n")
                        .append("        </body>\n")
                        .append("      </slide>\n");
            }
        }

        sb.append("  <slide_group name=\"# ")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(c.getString(R.string.variation)))
                .append(" # - ")
                .append(tempSong.getFilename())
                .append("\"")
                .append(" type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\">\n")
                .append("    <title>")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getTitle()))
                .append("</title>\n")
                .append("    <subtitle>")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getAuthor()))
                .append("</subtitle>\n")
                .append("    <notes>")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(slide_lyrics))
                .append("</notes>\n")
                .append("    <slides>\n")
                .append(slidetexttowrite)
                .append("    </slides>\n")
                .append("  </slide_group>\n");

        return sb;
    }
    private StringBuilder buildSlide(MainActivityInterface mainActivityInterface, Song tempSong) {
        StringBuilder sb = new StringBuilder();
        // Adding a custom slide
        String slide_lyrics = tempSong.getLyrics();

        if (slide_lyrics.indexOf("---\n") == 0) {
            slide_lyrics = slide_lyrics.replaceFirst("---\n", "");
        }
        // Parse the lyrics into individual slides;
        slide_lyrics = slide_lyrics.replace("---", "_SPLITHERE_");

        String[] mySlides = slide_lyrics.split("_SPLITHERE_");

        sb.append("  <slide_group name=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getFilename()))
                .append("\" type=\"custom\" print=\"true\" seconds=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getUser1()))
                .append("\" loop=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getUser2()))
                .append("\" transition=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getUser3()))
                .append("\">\n    <title>")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getTitle()))
                .append("</title>\n    <subtitle>")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getCopyright()))
                .append("</subtitle>\n    <notes>")
                .append("")
                .append("</notes>\n    <slides>\n");

        for (String mySlide : mySlides) {
            if (mySlide != null && mySlide.length() > 0) {
                sb.append("      <slide>\n        <body>")
                        .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(mySlide.trim()))
                        .append("</body>\n      </slide>\n");
            }
        }

        sb.append("    </slides>\n  </slide_group>\n");

        return sb;
    }
    private StringBuilder buildNote(Context c, MainActivityInterface mainActivityInterface, Song tempSong) {
        StringBuilder sb = new StringBuilder();
        // Adding a note

        String slide_lyrics = tempSong.getLyrics();

        sb.append("  <slide_group name=\"# ")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(c.getResources().getString(R.string.note)))
                .append(" # - ")
                .append(tempSong.getFilename())
                .append("\" type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\">\n")
                .append("    <title></title>\n")
                .append("    <subtitle></subtitle>\n")
                .append("    <notes>")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(slide_lyrics))
                .append("</notes>\n")
                .append("    <slides></slides>\n")
                .append("  </slide_group>\n");

        return sb;
    }
    private StringBuilder buildImage(Context c, MainActivityInterface mainActivityInterface, Song tempSong) {
        // Adding a custom image slide
        StringBuilder sb = new StringBuilder();

        // The mUser3 field should contain all the images
        // Break all the images into the relevant slides
        String[] separate_slide = tempSong.getUser3().split("\n");

        StringBuilder slideCode = new StringBuilder();
        for (String aSeparate_slide : separate_slide) {
            String imglinetext;
            // Try to get the image into bytes
            String imgcode = mainActivityInterface.getStorageAccess().getImageSlide(c, aSeparate_slide);
            if (!imgcode.isEmpty()) {
                imglinetext = "        <image>" + imgcode.trim() + "</image>\n";
            } else {
                imglinetext = "        <filename>" + aSeparate_slide + "</filename>\n";
            }
            slideCode.append("      <slide>\n")
                    .append(imglinetext)
                    .append("        <description>")
                    .append(aSeparate_slide)
                    .append("</description>\n")
                    .append("      </slide>\n");
        }

        sb.append("  <slide_group name=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getAka()))
                .append("\" type=\"image\" print=\"true\" seconds=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getUser1()))
                .append("\" loop=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getUser2()))
                .append("\" transition=\"0\" resize=\"screen\" keep_aspect=\"false\" link=\"false\">\n")
                .append("    <title>")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getTitle()))
                .append("</title>\n")
                .append("    <subtitle>")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getAuthor()))
                .append("</subtitle>\n")
                .append("    <notes>")
                .append("")
                .append("</notes>\n")
                .append("    <slides>\n")
                .append(slideCode)
                .append("\n")
                .append("    </slides>\n")
                .append("  </slide_group>\n");

        return sb;
    }

/*

    public void makeVariation(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {

        // Original file
        Uri uriOriginal = mainActivityInterface.getStorageAccess().getUriForItem(c,
                mainActivityInterface, "Songs",
                thisSong.getFolder(),
                thisSong.getFilename());

        // Copy the file into the variations folder
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, uriOriginal);

        // If the file already exists, add _ to the filename
        StringBuilder newsongname = new StringBuilder(thisSong.getFilename());
        Uri uriVariation = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Variations", "",
                newsongname.toString());
        while (mainActivityInterface.getStorageAccess().uriExists(c,uriVariation)) {
            newsongname.append("_");
            uriVariation = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Variations", "",
                    newsongname.toString());
        }

        // Check the uri exists for the outputstream to be valid
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c, mainActivityInterface, uriVariation, null,
                "Variations", "", newsongname.toString());

        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, uriVariation);
        mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);

        // Fix the song name and folder for loading
        thisSong.setFilename(newsongname.toString());
        thisSong.setFolder("**Variations");

        // Get the original position in the set
        int position = mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface,thisSong);

        String songItem = getSongForSetWork(c,thisSong);

        // Replace this item with the new values
        mainActivityInterface.getCurrentSet().updateSetItem(position, songItem);

        songForSetWork = start + "**" + c.getString(R.string.variation) + "/" +
                mainActivityInterface.getSong().getFilename() + end;

        // Replace the set item with the variation
        mainActivityInterface.getCurrentSet().getMyCurrentSet().
                set(mainActivityInterface.getCurrentSet().getIndexSongInSet(),getSongForSetWork(thisSong));
        setToSetString(c, mainActivityInterface);
        updateSetItems(c,mainActivityInterface);

        mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrent",
                mainActivityInterface.getCurrentSet().getCurrentSetString());

        mainActivityInterface.getShowToast().doIt(c, c.getResources().getString(R.string.variation_edit));

        // Now load the new variation item up
        // TODO calling activity should do this!

    }
*/

    // TODO Not sure if this is used
    public String fixIsInSetSearch(String s) {
        if (s.contains("**_Variations/")) {
            s = s.replace("**_Variations/","**_**Variation/");
        } else if (s.contains("**_Variation/")) {
            s = s.replace("**_Variation/","**_**Variation/");
        }
        return s;
    }
}