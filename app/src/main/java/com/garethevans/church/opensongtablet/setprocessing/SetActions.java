package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

// This class interacts with the CurrentSet object and does the processing, etc.

public class SetActions {

    private final String itemStart = "$**_", itemEnd = "_**$",
            keyStart = "_***", keyEnd = "***_", setCategorySeparator = "__",
            folderVariations = "Variations", folderNotes = "Notes", folderSlides="Slides",
            folderScripture = "Scripture", folderImages = "Images", TAG = "SetActions",
            transposeStart = "__tr__", cache = "_cache", customLocStart = "**";

    private ArrayList<Integer> missingKeyPositions;
    private ArrayList<String> newItem_Folder;
    private ArrayList<String> newItem_Filename;
    private ArrayList<String> newItem_Content;

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
        if ((key==null || key.isEmpty()) && folder.startsWith("../") && folder.startsWith(customLocStart)) {
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
        // This saves the set to user preferences for loading in next time
        // Not to be confused with exporting/saving the set as a file
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
        return customLocStart + folderLocation;
    }
    public String folderFromNiceCustomLocation(Context c, String niceFolder) {
        // This converts things like **Variations, **Scripture back to the folder
        niceFolder = niceFolder.replace(customLocStart,"");
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
            folder = thisSong.getFolder().replaceFirst("../",customLocStart);
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
        valueToDecideFrom = valueToDecideFrom.replace(customLocStart,"");

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
        } else if (folder.contains(customLocStart)) {
            valueToDecideFrom = folder = folder.replace(customLocStart,"");
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


    public void loadSets(Context c, MainActivityInterface mainActivityInterface, ArrayList<Uri> setsToLoad) {
        // This is called via a new thread in the manage sets fragment
        // We can append multiple sets together
        // If the set loaded has a key specified with it, we compare with our key
        // If it is different, the app creates a variation of our song then transposes it
        // Any variations bundled in the set are extracted as variations here too

        // First up, clear out our _cache folders after we remove any entries from the database
        removeCacheItemsFromDB(c, mainActivityInterface, folderScripture, cache);
        removeCacheItemsFromDB(c, mainActivityInterface, folderSlides, cache);
        removeCacheItemsFromDB(c, mainActivityInterface, folderNotes, cache);
        removeCacheItemsFromDB(c, mainActivityInterface, folderImages, cache);
        removeCacheItemsFromDB(c, mainActivityInterface, folderVariations, cache);

        // Create the cache directories again if any are missing
        mainActivityInterface.getStorageAccess().createOrCheckRootFolders(c,null,mainActivityInterface);


        // Now users can load multiple sets and merge them, we need to load each one it turn
        // We then add the items to a temp string 'allsongsinset'
        // Once we have loaded them all, we replace the mySet field.

        // Initialise the arrays that will hold the loaded information
        mainActivityInterface.getCurrentSet().initialiseTheSet();

        for (Uri setToLoad:setsToLoad) {
            // Pass each uri to the set extraction function and let it populate the arrays
            extractSetFile(c, mainActivityInterface, setToLoad);
        }

        // Now we have the entire set contents, save it to our preferences
        saveTheSet(c, mainActivityInterface);

    }

    private void extractSetFile(Context c, MainActivityInterface mainActivityInterface, Uri uri) {
        // This loads individual set files and populates the arrays
        // Set up the xml utility
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            String utf = mainActivityInterface.getStorageAccess().getUTFEncoding(c, uri);
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, uri);
            xpp.setInput(inputStream, utf);
            int eventType;
            eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("slide_group")) {
                        // Is this a song?
                        switch (xpp.getAttributeValue(null, "type")) {
                            case "song":
                                // Get song
                                try {
                                    getSong(mainActivityInterface,xpp);
                                } catch (Exception e) {
                                    Log.d(TAG, "Couldn't get song location from set");
                                    e.printStackTrace();
                                }
                                break;

                            case "scripture":
                                // Get Scripture
                                try {
                                    getScripture(c,mainActivityInterface, xpp);
                                } catch (Exception e) {
                                    Log.d(TAG, "Couldn't get scripture from set");
                                    e.printStackTrace();
                                }
                                break;
                            case "custom":
                                // Get Custom (Note or slide or variation)
                                getCustom(c, mainActivityInterface, xpp);
                                break;
                            case "image":
                                // Get the Image(s)
                                getImage(c,mainActivityInterface,xpp);
                                break;
                        }
                    }
                }
                try {
                    eventType = xpp.next();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "End of XML file");
                }
            }

        } catch (Exception e) {
            // Error setting up the XML utility
            e.printStackTrace();
        }
    }

    private String stripSlashes(String string) {
        if (string.startsWith("/")) {
            string = string.replaceFirst("/", "");
        }
        if (string.endsWith("/")) {
            string = string.substring(0, string.lastIndexOf("/"));
        }
        return string;
    }

    private void getSong(MainActivityInterface mainActivityInterface, XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        // Set this info into the current set.  We will just load our song
        // When we load, we will transpose our song if the key is different
        String path = stripSlashes(mainActivityInterface.getProcessSong().
                parseHTML(xpp.getAttributeValue(null,"path")));
        String name = stripSlashes(mainActivityInterface.getProcessSong().
                parseHTML(xpp.getAttributeValue(null,"name")));
        String key = "";
        if (xpp.getAttributeCount()>2) {
            // Assume a key has been set as well
            key = xpp.getAttributeValue(null, "key");
        }

        mainActivityInterface.getCurrentSet().addSetValues(path, name, key);
        mainActivityInterface.getCurrentSet().addToCurrentSet(getSongForSetWork(path, name, key));

        xpp.nextTag();
    }

    private void getScripture(Context c, MainActivityInterface mainActivityInterface,
                              XmlPullParser xpp) throws IOException, XmlPullParserException {
        // Scripture entries in a set are custom slides.  Get the data and save it
        // This will ultimately be saved in our Scripture/_cache folder
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
                    scripture_text.append("\n[]\n").append(mainActivityInterface.getProcessSong().
                            parseHTML(xpp.nextText()));
                    break;
                case "subtitle":
                    scripture_translation = mainActivityInterface.getProcessSong().parseHTML(xpp.nextText());
                    break;
            }

            xpp.nextTag();

            if (xpp.getEventType()==XmlPullParser.END_TAG) {
                if (xpp.getName().equals("slides")) {
                    scripture_finished = true;
                }
            }
        }

        // Create a new file for each of these entries (might be more than one slide in the group).
        // Filename is title with Scripture/

        // Break the scripture_text up into small manageable chunks
        // First up, start each new verse on a new line
        // Replace all spaces (split points) with \n
        scripture_text = new StringBuilder(scripture_text.toString().replace(" ", "\n"));
        scripture_text = new StringBuilder(scripture_text.toString().replace("---", "[]"));
        //Split the verses up into an array by new lines - array of words
        String[] temp_text = scripture_text.toString().split("\n");

        //String[] add_text = new String[800];
        //int array_line = 0;

        // Add all the array back together and make sure no line goes above 50 characters
        // This means it won't appear tiny as the app tries to scale the lyrics
        ArrayList<String> vlines = new ArrayList<>();
        StringBuilder currline = new StringBuilder();
        for (String words : temp_text) {
            int check = currline.length();
            if (check>50 || words.contains("[]")) {
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

        while (scripture_text.toString().contains("\\n\\n")) {
            scripture_text = new StringBuilder(scripture_text.toString().replace("\\n\\n", "\\n"));
        }

        Song tempSong = mainActivityInterface.getProcessSong().initialiseSong(mainActivityInterface,
                customLocStart+folderScripture, scripture_title);
        tempSong.setTitle(scripture_title);
        tempSong.setSongid(mainActivityInterface.getCommonSQL().getAnySongId(customLocStart+folderScripture, scripture_title));
        tempSong.setAuthor(scripture_translation);
        tempSong.setUser1(scripture_seconds);
        tempSong.setUser2(scripture_loop);
        tempSong.setLyrics(scripture_text.toString().trim());

        // Add to the set
        mainActivityInterface.getCurrentSet().addToCurrentSet(getSongForSetWork(
                tempSong.getFolder(), tempSong.getFilename(), ""));
        mainActivityInterface.getCurrentSet().addSetValues(tempSong.getFolder(),
                tempSong.getFilename(),"");

        // Now create the file in the Scripture/_cache folder
        writeTempSlide(c,mainActivityInterface,folderScripture,cache,tempSong);

        xpp.nextTag();

    }

    private void getCustom(Context c, MainActivityInterface mainActivityInterface,
                           XmlPullParser xpp) throws IOException, XmlPullParserException {
        // Could be a note or a slide or a variation
        // Notes have # Note # - in the name
        // Variations have # Variation # - in the name
        String custom_name = mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "name"));
        String custom_seconds = mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "seconds"));
        String custom_loop = mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "loop"));
        String custom_title = "";
        String custom_subtitle = "";
        String custom_notes = "";
        StringBuilder custom_text = new StringBuilder();
        String tempcache = cache;

        boolean custom_finished = false;
        while (!custom_finished) {
            switch (xpp.getName()) {
                case "title":
                    custom_title = mainActivityInterface.getProcessSong().parseHTML(xpp.nextText());
                    break;
                case "notes":
                    custom_notes = mainActivityInterface.getProcessSong().parseHTML(xpp.nextText());
                    break;
                case "body":
                    custom_text.append("\n---\n").append(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                    break;
                case "subtitle":
                    custom_subtitle = mainActivityInterface.getProcessSong().parseHTML(xpp.nextText());
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

        // Get a new tempSong ready for the info
        Song tempSong = mainActivityInterface.getProcessSong().initialiseSong(mainActivityInterface,
                customLocStart+folderSlides, custom_title);

        if (custom_name.contains("# " + c.getResources().getString(R.string.note) + " # - ")) {
            // Prepare for a note
            custom_name = custom_name.replace("# " + c.getResources().getString(R.string.note) + " # - ", "");
            tempSong.setFolder(customLocStart+folderNotes);

            } else if (custom_name.contains("# " + c.getResources().getString(R.string.variation) + " # - ")) {
            // Prepare for a variation
            custom_name = custom_name.replace("# " + c.getResources().getString(R.string.variation) + " # - ", "");
            tempSong.setFolder(customLocStart + folderVariations);
            tempcache = "";
        }

        tempSong.setFilename(custom_name);
        tempSong.setTitle(custom_name);
        tempSong.setLyrics(custom_notes);
        tempSong.setUser1(custom_seconds);
        tempSong.setUser2(custom_loop);
        tempSong.setUser3(custom_subtitle);

        // Add the slide to the set
        mainActivityInterface.getCurrentSet().addToCurrentSet(getSongForSetWork(
                tempSong.getFolder(), tempSong.getFilename(), ""));
        mainActivityInterface.getCurrentSet().addSetValues(tempSong.getFolder(),
                tempSong.getFilename(),"");

        // Now create the file in the appropriate location /_cache folder
        writeTempSlide(c,mainActivityInterface,
                tempSong.getFolder().replace(customLocStart,""),tempcache,tempSong);
    }

    private void getImage(Context c, MainActivityInterface mainActivityInterface,
                          XmlPullParser xpp) throws IOException, XmlPullParserException {
        // Ok parse this bit separately.  This could have multiple images
        String image_name = mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "name"));
        String image_seconds = mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "seconds"));
        String image_loop = mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "loop"));
        StringBuilder image_title = new StringBuilder();
        String image_subtitle = "";
        StringBuilder slide_images;
        StringBuilder slide_image_titles;
        String image_notes = "";
        String image_filename;
        StringBuilder hymn_number_imagecode = new StringBuilder();
        String key_line = "";
        int imagenums = 0;
        boolean encodedimage = false;

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
                switch (xpp.getName()) {
                    case "title":
                        image_title = new StringBuilder(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                        break;

                    case "subtitle":
                        image_subtitle = mainActivityInterface.getProcessSong().parseHTML(xpp.nextText());
                        break;
                    case "notes":
                        image_notes = mainActivityInterface.getProcessSong().parseHTML(xpp.nextText());
                        break;
                    case "filename":
                        image_filename = mainActivityInterface.getProcessSong().parseHTML(xpp.nextText());
                        if (!image_filename.equals("") && !image_filename.isEmpty()) {
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
                        String file_name = mainActivityInterface.getProcessSong().parseHTML(xpp.nextText());
                        if (file_name.toLowerCase(Locale.ROOT).contains(".png")) {
                            image_type = ".png";
                        } else if (file_name.toLowerCase(Locale.ROOT).contains(".gif")) {
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

                            String safeFilename = mainActivityInterface.getStorageAccess().
                                    safeFilename(image_title.toString() + imagenums + image_type);
                            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface,
                                    folderImages, cache, safeFilename);

                            // Check the uri exists for the outputstream to be valid
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c, mainActivityInterface, uri, null,
                                    folderImages, cache, safeFilename);

                            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, uri);
                            byte[] decodedString = Base64.decode(image_content, Base64.DEFAULT);
                            mainActivityInterface.getStorageAccess().writeFileFromDecodedImageString(outputStream, decodedString);

                            slide_images.append(uri.toString()).append("\n");
                            slide_image_titles.append("[").append(c.getResources().getString(R.string.image))
                                    .append("_").append(imagenums + 1).append("]\n").append(uri).append("\n\n");
                            imagenums++;
                            encodedimage = false;
                        }
                        break;
                }
            }
            allimagesdone = eventType == XmlPullParser.END_TAG && xpp.getName() != null &&
                    xpp.getName().equals("slide_group");
            eventType = xpp.next();
        }

        if (image_title.toString().equals("")) {
            image_title = new StringBuilder(c.getResources().getString(R.string.image));
        }

        image_subtitle = fixNull(image_subtitle);
        image_seconds = fixNull(image_seconds);
        image_loop = fixNull(image_loop);
        image_name = fixNull(image_name);
        image_notes = fixNull(image_notes);

        // Get a new tempSong ready for the info
        Song tempSong = mainActivityInterface.getProcessSong().initialiseSong(mainActivityInterface,
                customLocStart+folderImages, image_title.toString());

        tempSong.setTitle(image_title.toString());
        tempSong.setAuthor(image_subtitle);
        tempSong.setUser1(image_seconds);
        tempSong.setUser2(image_loop);
        tempSong.setUser3(slide_images.toString().trim());
        tempSong.setAka(image_name);
        tempSong.setKey(image_notes);
        tempSong.setLyrics(slide_image_titles.toString().trim());
        writeTempSlide(c,mainActivityInterface,folderImages,cache,tempSong);
    }

    private String fixNull(String s) {
        if (s == null) {
            s = "";
        }
        return s;
    }

    private void removeCacheItemsFromDB(Context c, MainActivityInterface mainActivityInterface, String folder, String subfolder) {
        ArrayList<String> filesInFolder = mainActivityInterface.getStorageAccess().listFilesInFolder(c, mainActivityInterface, folder, subfolder);
        for (String filename:filesInFolder) {
            mainActivityInterface.getSQLiteHelper().deleteSong(c, mainActivityInterface, customLocStart+folder, filename);
        }

        // Now empty the actual folder
        mainActivityInterface.getStorageAccess().wipeFolder(c,mainActivityInterface,folder, subfolder);

    }

    private void writeTempSlide(Context c, MainActivityInterface mainActivityInterface,
                                String folder, String subfolder, Song tempSong) {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface,
                folder, subfolder, tempSong.getFilename());
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c, mainActivityInterface,
                uri, null, folder, subfolder, tempSong.getFilename());

        // Get the song as XML
        String tempSongXML = mainActivityInterface.getProcessSong().getXML(c, mainActivityInterface, tempSong);

        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, uri);
        mainActivityInterface.getStorageAccess().writeFileFromString(tempSongXML, outputStream);
    }

}