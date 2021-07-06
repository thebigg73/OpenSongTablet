package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

// This class interacts with the CurrentSet object and does the processing, etc.

public class SetActions {

    private final String itemStart = "$**_", itemEnd = "_**$",
            keyStart = "_***", keyEnd = "***_", setCategorySeparator = "__",
            folderVariations = "Variations", folderNotes = "Notes", folderSlides="Slides",
            folderScripture = "Scripture", folderImages = "Images", TAG = "SetActions";

    private ArrayList<Integer> missingKeyPositions;

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

        // Make sure the specific arrays are empty to start with!
        mainActivityInterface.getCurrentSet().initialiseTheSpecifics();

        for (int x=0; x<mainActivityInterface.getCurrentSet().getSetItems().size(); x++) {
            String setItem = mainActivityInterface.getCurrentSet().getItem(x);
            String key, folder, item;
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
            // We only do this is the key isn't empty and it isn't a custom item (e.g. notes, slides)
            // Of course if the indexing isn't complete it will still be null - don't check the file yet
            key = getKeyFromDatabaseOrFile(c,mainActivityInterface,false,key,folder,setItem);

            // Build the set item.  Useful for shuffling and rebuilding and searching
            item = itemStart + folder + "/" + setItem + keyStart + key + keyEnd + itemEnd;
            mainActivityInterface.getCurrentSet().setItem(x,item);

            // Put the values into the set arrays (the filename is what is left)
            mainActivityInterface.getCurrentSet().addSetValues(folder, setItem, key);
        }
    }
    private String getKeyFromDatabaseOrFile(Context c, MainActivityInterface mainActivityInterface,
                                            boolean lastCheck, String key, String folder, String filename) {
        // First off, check, the database.  If it isn't there, it might be coming soon
        // If we've finished indexing, the last chance is to try the file directly if it
        // still isn't in the database - could be a variation file created from the set.
        // We only check the variation file last to avoid any issues during indexing.
        if ((key==null || key.isEmpty()) && folder.startsWith("../") && folder.startsWith("**")) {
            Log.d(TAG,"folder="+folder+"  filename="+filename+" key not set, checking database...");
            // If this is an image or pdf, check the nonOpenSongSQLite database
            // Otherwise use the normal database
            if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("imageorpdf",filename)) {
                key = mainActivityInterface.getNonOpenSongSQLiteHelper().getKey(c,mainActivityInterface,folder,filename);
            } else {
                key = mainActivityInterface.getSQLiteHelper().getKey(c,mainActivityInterface,folder,filename);
            }
        } else if ((key==null || key.isEmpty()) && folder.contains(folderVariations) && lastCheck) {
            // This is a custom variation item so load the key from the file
            key = mainActivityInterface.getLoadSong().loadKeyOfSong(c,mainActivityInterface,folder,filename);
        }
        if (key==null) {
            key = "";
        }
        return key;
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
    public String getSongForSetWork(String folder, String filename, String key) {
        return itemStart + folder + "/" + filename + keyStart + key + keyEnd + itemEnd;
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
        // Build an arraylist of the positions so we can redraw the adapter
        missingKeyPositions = new ArrayList<>();
        int size = mainActivityInterface.getCurrentSet().getSetKeys().size();
        Log.d(TAG,"size="+size);
        for (int x = 0; x<mainActivityInterface.getCurrentSet().getSetKeys().size(); x++) {
            String key = mainActivityInterface.getCurrentSet().getKey(x);
            if (key.isEmpty()) {
                String folder = mainActivityInterface.getCurrentSet().getFolder(x);
                String filename = mainActivityInterface.getCurrentSet().getFilename(x);

                key = getKeyFromDatabaseOrFile(c,mainActivityInterface,true,key,folder,filename);

                Log.d(TAG,"folder="+folder+"  filename="+filename+"  key="+key);
                if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("imageorpdf",filename)) {
                    key = mainActivityInterface.getNonOpenSongSQLiteHelper().
                                    getKey(c, mainActivityInterface, folder, filename);
                } else {
                    key = mainActivityInterface.getSQLiteHelper().
                                    getKey(c, mainActivityInterface, folder, filename);
                }
                Log.d(TAG,"found key="+key);
                mainActivityInterface.getCurrentSet().setKey(x,key);
                missingKeyPositions.add(x);
                Log.d(TAG,"getKey["+x+"]="+mainActivityInterface.getCurrentSet().getKey(x));
            }
        }
    }
    public ArrayList<Integer> getMissingKeyPositions() {
        return missingKeyPositions;
    }
    public void nullMissingKeyPositions() {
        missingKeyPositions = null;
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
            title = lastSetName.replace(setCategorySeparator, "/");
            if (mainActivityInterface.getCurrentSet().getInitialSetString().equals(
                    mainActivityInterface.getCurrentSet().getCurrentSetString())) {
                title += " (" + c.getString(R.string.not_saved) + ")";
            }
        }
        return title;
    }

    public String niceCustomLocationFromFolder(Context c, String folderLocation) {
        // This gives a nice output for the folderLocation for viewing
        folderLocation = folderLocation.replace("../","");
        folderLocation = folderLocation.replace("/_cache","");
        switch (folderLocation) {
            case folderVariations:
                folderLocation = c.getString(R.string.variation);
                break;
            case folderNotes:
                folderLocation = c.getString(R.string.note);
                break;
            case folderSlides:
                folderLocation = c.getString(R.string.slide);
                break;
            case folderScripture:
                folderLocation = c.getString(R.string.scripture);
                break;
            case folderImages:
                folderLocation = c.getString(R.string.image);
                break;
        }
        return "**" + folderLocation;
    }
    public String folderFromNiceCustomLocation(Context c, String niceFolder) {
        // This converts things like **Variations, **Scripture back to the folder
        niceFolder = niceFolder.replace("**","");
        niceFolder = niceFolder.replace(c.getString(R.string.variation),folderVariations);
        niceFolder = niceFolder.replace(c.getString(R.string.note),folderNotes);
        niceFolder = niceFolder.replace(c.getString(R.string.slide),folderSlides);
        niceFolder = niceFolder.replace(c.getString(R.string.scripture),folderScripture);
        niceFolder = niceFolder.replace(c.getString(R.string.image),folderImages);

        // Some of the above actually go into a _cache subfolder, but that is dealt with later
        return niceFolder;
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

    public void makeVariation(Context c, MainActivityInterface mainActivityInterface, int position) {
        // Takes the chosen song and copies it to a temporary file in the Variations folder
        // This also updates the set menu to point to the new temporary item
        // This allows the user to freely edit the variation object
        // This is also called after an item is clicked in a set with a different key to the default
        // If this happens, the variation is transposed by the required amount

        // Get the current set item values
        String filename = mainActivityInterface.getCurrentSet().getFilename(position);
        String folder = mainActivityInterface.getCurrentSet().getFolder(position);
        String key = mainActivityInterface.getCurrentSet().getKey(position);
        String variationFolder = niceCustomLocationFromFolder(c, folderVariations);

        // Fix the item in the set
        mainActivityInterface.getCurrentSet().setItem(position,
                getSongForSetWork(variationFolder, filename, key));
        mainActivityInterface.getCurrentSet().setFolder(position, variationFolder);
        mainActivityInterface.getCurrentSet().setFilename(position, filename);
        mainActivityInterface.getCurrentSet().setCurrentSetString(getSetAsPreferenceString(mainActivityInterface));

        // Get the uri of the original file (if it exists)
        // We receive a song object as it isn't necessarily the one loaded to MainActivity
        Uri uriOriginal = mainActivityInterface.getStorageAccess().getUriForItem(
                c, mainActivityInterface, "Songs", folder, filename);

        // Get the uri of the new variation file (Variations/filename)
        Uri uriVariation = mainActivityInterface.getStorageAccess().getUriForItem(c,
                mainActivityInterface, folderVariations, "", filename);

        // Make sure there is a file to write the output to (remove any existing first)
        mainActivityInterface.getStorageAccess().deleteFile((Context)mainActivityInterface, uriVariation);
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c,
                mainActivityInterface, uriVariation, null, folderVariations, "", filename);

        // Get an input/output stream reference and copy (streams are closed in copyFile())
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, uriOriginal);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, uriVariation);
        mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
    }

    public int getItemIcon(Context c, String valueToDecideFrom) {
        int icon;
        // Get rid of ** and ../
        valueToDecideFrom = valueToDecideFrom.replace("../","");
        valueToDecideFrom = valueToDecideFrom.replace("**","");

        if (valueToDecideFrom.equals(c.getResources().getString(R.string.slide))) {
            icon = R.drawable.ic_projector_screen_white_36dp;
        } else if (valueToDecideFrom.equals(c.getResources().getString(R.string.note))) {
            icon = R.drawable.ic_note_text_white_36dp;
        } else if (valueToDecideFrom.equals(c.getResources().getString(R.string.scripture))) {
            icon = R.drawable.ic_book_white_36dp;
        } else if (valueToDecideFrom.equals(c.getResources().getString(R.string.image))) {
            icon = R.drawable.ic_image_white_36dp;
        } else if (valueToDecideFrom.equals(c.getResources().getString(R.string.variation))) {
            icon = R.drawable.ic_file_xml_white_36dp;
        } else if (valueToDecideFrom.equals("PDF")) {
            icon = R.drawable.ic_file_pdf_white_36dp;
        } else {
            icon = R.drawable.ic_music_note_white_36dp;
        }
        return icon;
    }
    public String getIconIdentifier(Context c, MainActivityInterface mainActivityInterface, String folder, String filename) {
        // If the filename is an image, we use that
        String valueToDecideFrom;
        if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("image",filename)) {
            valueToDecideFrom = c.getString(R.string.image);
        } else if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("pdf",filename)) {
            valueToDecideFrom = "PDF";
        } else if (folder.contains("**")) {
            valueToDecideFrom = folder = folder.replace("**","");
        } else {
            valueToDecideFrom = c.getString(R.string.song);
        }
        return valueToDecideFrom;
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
            boolean isImage = name.contains(niceCustomLocationFromFolder(c,folderImages));
            boolean isVariation = name.contains(niceCustomLocationFromFolder(c,folderVariations));
            boolean isScripture = name.contains(niceCustomLocationFromFolder(c,folderScripture));
            boolean isSlide = name.contains(niceCustomLocationFromFolder(c,folderSlides));
            boolean isNote = name.contains(niceCustomLocationFromFolder(c,folderNotes));

            if (isImage) {
                // Adding an image
                Song tempSong = getTempSong(c,mainActivityInterface,"../" + folderImages + "/_cache", name);
                stringBuilder.append(buildImage(c,mainActivityInterface,tempSong));

            } else if (isScripture) {
                // Adding a scripture
                Song tempSong = getTempSong(c,mainActivityInterface, "../" + folderScripture + "/_cache", name);
                stringBuilder.append(buildScripture(mainActivityInterface,tempSong));

            } else if (isVariation) {
                // Adding a variation
                Song tempSong = getTempSong(c,mainActivityInterface, "../" + folderVariations, name);
                stringBuilder.append(buildVariation(c,mainActivityInterface,tempSong));

            } else if (isSlide) {
                // Adding a slide
                Song tempSong = getTempSong(c,mainActivityInterface, "../" + folderSlides + "/_cache", name);
                stringBuilder.append(buildSlide(mainActivityInterface,tempSong));

            } else if (isNote) {
                // Adding a note
                Song tempSong = getTempSong(c,mainActivityInterface, "../" + folderNotes + "/_cache", name);
                stringBuilder.append(buildNote(c,mainActivityInterface,tempSong));
            } else {
                // Adding a song
                stringBuilder.append(buildSong(mainActivityInterface,path,name));
            }
        }
        // Now add the final part of the xml
        stringBuilder.append("</slide_groups>\n</set>");

        return stringBuilder.toString();
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

}