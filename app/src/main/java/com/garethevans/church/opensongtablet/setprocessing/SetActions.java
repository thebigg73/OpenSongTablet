package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.garethevans.church.opensongtablet.songprocessing.Song;

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
import java.util.Comparator;
import java.util.Locale;

// This class interacts with the CurrentSet object and does the processing, etc.

public class SetActions {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final String itemStart = "$**_";
    private final String itemEnd = "_**$";
    private final String keyStart = "_***";
    private final String keyEnd = "***_";
    private final String setCategorySeparator = "__";
    private final String folderVariations = "Variations";
    private final String folderNotes = "Notes";
    private final String folderSlides="Slides";
    private final String folderScripture = "Scripture";
    private final String folderExport = "Export";
    private final String folderImages = "Images";
    private final String TAG = "SetActions";
    private final String cache = "_cache";
    private final String customLocStart = "**";
    private final String customLocBasic = "../";
    private final String nicePDF, niceVariation, niceImage, niceSlide,
        niceScripture, niceNote;
    private ArrayList<Integer> missingKeyPositions;

    public SetActions(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        nicePDF = c.getString(R.string.pdf);
        niceVariation = c.getString(R.string.variation);
        niceImage = c.getString(R.string.image);
        niceSlide = c.getString(R.string.slide);
        niceScripture = c.getString(R.string.scripture);
        niceNote = c.getString(R.string.note);
    }

    // Convert between the currentSet string in preferences and the arrayLists


    // Called from the BootFragment on first boot and when clearing, sorting or on loading new set in
    public void parseCurrentSet() {
        // Sets may or may not have the preferred key embedded in them (old sets before V6 will not)
        // $**_folder1/song1_**$$**_folder2/song2_**A**__**$

        // Initialise set object array
        mainActivityInterface.getCurrentSet().initialiseTheSet();

        // Get the current set and the last edited version.
        // These are stored as variables in the CurrentSet class in this process
        mainActivityInterface.getCurrentSet().loadCurrentSet();
        mainActivityInterface.getCurrentSet().loadSetCurrentBeforeEdits();
        mainActivityInterface.getCurrentSet().loadSetCurrentLastName();

        // Look for changes and update the set title in the set menu
        mainActivityInterface.getCurrentSet().updateSetTitleView();

        // Get the currentSet string back from the CurrentSet class for processing
        String currentSet = mainActivityInterface.getCurrentSet().getSetCurrent();

        // Now split the set string into individual entries
        currentSet = currentSet.replace(itemEnd+itemStart,"\n");
        currentSet = currentSet.replace(itemStart,"");
        currentSet = currentSet.replace(itemEnd,"");
        String[] setItems = currentSet.split("\n");

        int songitem = 1;
        for (String setItem:setItems) {
            SetItemInfo setItemInfo = new SetItemInfo();

            // Add the set item number and increment for the next one if required
            setItemInfo.songitem = songitem;
            songitem ++;

            if (!setItem.isEmpty()) {
                // If we have a key, get it
                String key = "";
                if (setItem.contains(keyStart) && setItem.contains(keyEnd) &&
                        setItem.indexOf(keyStart)<setItem.indexOf(keyEnd)) {
                    key = setItem.substring(setItem.indexOf(keyStart),setItem.indexOf(keyEnd));
                    key = key.replace(keyStart,"");
                    key = key.replace(keyEnd,"");
                }
                setItem = setItem.replace(keyStart+key+keyEnd,"");

                // Now get the folder and filename
                String folder = mainActivityInterface.getMainfoldername();
                if (!setItem.contains("/")) {
                    setItem = "/" + setItem;
                }
                if (setItem.lastIndexOf("/")!=0) {
                    folder = setItem.substring(0, setItem.lastIndexOf("/"));
                    setItem = setItem.replace(folder+"/","");
                }
                String foldernice = mainActivityInterface.getSetActions().niceCustomLocationFromFolder(folder);
                String filename = setItem.replace("/","");

                setItemInfo.songfolder = folder;
                setItemInfo.songfoldernice = foldernice;
                setItemInfo.songfilename = filename;
                if (!mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                    // Get the title from the database
                    Song thisSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(folder,filename);
                    setItemInfo.songtitle = thisSong.getTitle();
                } else {
                    // Temporarily use the filename as the title - updated after indexing
                    setItemInfo.songtitle = filename;
                }
                setItemInfo.songkey = key;
                setItemInfo.songforsetwork = mainActivityInterface.getSetActions().getSongForSetWork(setItemInfo);
                mainActivityInterface.getCurrentSet().addItemToSet(setItemInfo,false);
                Song thisSong = new Song();
                thisSong.setFolder(folder);
                thisSong.setFilename(filename);
                thisSong.setKey(key);
            }
        }

        // Try to update the set title (might not be initialised)
        mainActivityInterface.getCurrentSet().updateSetTitleView();
    }

    // Called after the songs have been indexed.  Check titles from database
    public void updateSetTitlesAndIndexes() {
        for (int x=0; x<mainActivityInterface.getCurrentSet().getCurrentSetSize(); x++) {
            // Update the title
            Song tempSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songfolder,
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songfilename);
            mainActivityInterface.getCurrentSet().getSetItemInfo(x).songtitle = tempSong.getTitle();
            mainActivityInterface.getCurrentSet().getSetItemInfo(x).songitem = x+1;
            // Decide on the icon to use for the set item
            String folder = mainActivityInterface.getCurrentSet().getSetItemInfo(x).songfolder;
            if (folder!=null) {
                if (folder.equals("**Slides")) {
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songicon = "Slides";
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songfoldernice = niceSlide;
                } else if (folder.equals("**Notes")) {
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songicon = "Notes";
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songfoldernice = niceNote;
                } else if (folder.equals("**Scripture")) {
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songicon = "Scripture";
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songfoldernice = niceScripture;
                } else if (folder.equals("**Images")) {
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songicon = "Images";
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songfoldernice = niceImage;
                } else if (folder.equals("**Variations")) {
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songicon = "Variations";
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songfoldernice = niceVariation;
                } else if (folder.toLowerCase(Locale.ROOT).contains(".pdf")) {
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songicon = ".pdf";
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songfoldernice = nicePDF;
                } else {
                    mainActivityInterface.getCurrentSet().getSetItemInfo(x).songicon = "Songs";
                }
            } else {
                mainActivityInterface.getCurrentSet().getSetItemInfo(x).songicon = "Songs";
            }
            // Update checks in the song menu
            mainActivityInterface.updateCheckForThisSong(tempSong);
        }
        mainActivityInterface.updateSetList();
    }

    public boolean isSongInSet(String folderNamePair) {
        boolean inSet = false;
        for (SetItemInfo setItemInfo : mainActivityInterface.getCurrentSet().getSetItemInfos()) {
            if (folderNamePair.equals(setItemInfo.songfolder + "/" + setItemInfo.songfilename)) {
                inSet = true;
                break;
            }
        }
        return inSet;
    }

    public void clearCurrentSet() {
        mainActivityInterface.getCurrentSet().initialiseTheSet();
        mainActivityInterface.getCurrentSet().setSetCurrent("");
        mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits("");
        mainActivityInterface.getCurrentSet().setSetCurrentLastName("");
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrent", "");
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrentLastName", "");
    }

    public String getSetAsPreferenceString() {
        Log.d(TAG,"getSetAsPreferenceString()");

        // Build the set list into a string that can be saved to preferences
        // Use the arrays for folder, song title and key.  These should match!
        // As a precaution, each item is in a try/catch incase the arrays are different sizes
        StringBuilder stringBuilder = new StringBuilder();
        for (SetItemInfo setItemInfo: mainActivityInterface.getCurrentSet().getSetItemInfos()) {
            if (setItemInfo.songfilename!=null && !setItemInfo.songfilename.isEmpty())
                try {
                    String key = fixNull(setItemInfo.songkey);

                    stringBuilder.append(itemStart).
                            append(setItemInfo.songfolder).
                            append("/").
                            append(setItemInfo.songfilename).
                            append(keyStart).
                            append(key).
                            append(keyEnd).
                            append(itemEnd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return stringBuilder.toString();
    }

    // Get a reference string for thisSong for working with the currentSet string in preferences
    public String getSongForSetWork(Song thisSong) {
        return itemStart + thisSong.getFolder() + "/" + thisSong.getFilename() +
                keyStart + thisSong.getKey() + keyEnd + itemEnd;
    }
    public String getSongForSetWork(SetItemInfo setItemInfo) {
        return itemStart + setItemInfo.songfolder + "/" + setItemInfo.songfilename +
                keyStart + setItemInfo.songkey + keyEnd + itemEnd;
    }
    public String getSongForSetWork(String folder, String filename, String key) {
        return itemStart + folder + "/" + filename + keyStart + key + keyEnd + itemEnd;
    }

    public int indexSongInSet(Song thisSong) {
        Log.d(TAG,"indexSongInSet(song)");

        // Because set items can be stored with or without a specified key, we search for both
        String searchText = getSongForSetWork(thisSong);
        Song noKeySong = new Song();
        noKeySong.setFolder(thisSong.getFolder());
        noKeySong.setFilename(thisSong.getFilename());
        noKeySong.setKey("");
        String searchTextNoKeySpecified = getSongForSetWork(noKeySong);
        int position = mainActivityInterface.getCurrentSet().getMatchingSetItem(searchText);
        int positionNoKey = mainActivityInterface.getCurrentSet().getMatchingSetItem(searchTextNoKeySpecified);
        int positionVariation = -1;
        if (positionNoKey==-1 && position==-1) {
            // One last chance to find the song in the set (key changed variation)
            // Because transposed set items are variations, we need to check for those
            // Strip out the key
            String varFilename = thisSong.getFilename();
            // replace the
            if (varFilename.endsWith("_"+thisSong.getKey()) && varFilename.length()>1) {
                // Only replace the last instance
                varFilename = varFilename.substring(0,varFilename.lastIndexOf("_"));
            }
            String varFolder = thisSong.getFolder();
            if (varFolder.contains("**Variation") || varFolder.contains("**"+c.getString(R.string.variation))) {
                varFolder = "";
                // Now decide if we can extract a folder from the remaining filename
                if (varFilename.contains("_")) {
                    String[] bits = varFilename.split("_");
                    StringBuilder newFilename = new StringBuilder();
                    for (int i=0; i<bits.length; i++) {
                        if (i==0) {
                            varFolder = bits[0];
                        } else {
                            newFilename.append(bits[i]).append("/");
                        }
                    }
                    // Replace the last "/"
                    varFilename = newFilename.substring(0,newFilename.lastIndexOf("/"));
                }
            }
            String searchTextAsVariation = getSongForSetWork("**Variation",varFilename,"").replace("******__**$","");
            String searchTextAsKeyChangeVar = getSongForSetWork(varFolder,varFilename,"").replace("******__**$","");

            for (int v = 0; v < mainActivityInterface.getCurrentSet().getCurrentSetSize(); v++) {
                if (mainActivityInterface.getCurrentSet().getIsMatchingSetItem(v,searchTextAsKeyChangeVar) ||
                        mainActivityInterface.getCurrentSet().getIsMatchingSetItem(v,searchTextAsVariation)) {
                    positionVariation = v;
                }
            }
        }

        // If we have a current set index position and it matches this song, use the existing position
        int currentSetPosition = mainActivityInterface.getCurrentSet().getIndexSongInSet();
        if (currentSetPosition>-1 && mainActivityInterface.getCurrentSet().getCurrentSetSize()>currentSetPosition) {
            String currSetItem = mainActivityInterface.getCurrentSet().getSetItemInfo(currentSetPosition).songforsetwork;
            if (currSetItem==null) {
                currSetItem = "";
            }
            // If the song index isn't complete, or this is a pdf, the key text may be null rather than empty, check both
            if (currSetItem.equals(searchText) || currSetItem.equals(searchText.replace("***null***","******"))) {
                position = currentSetPosition;
            } else if (currSetItem.equals(searchTextNoKeySpecified) || currSetItem.equals(searchTextNoKeySpecified.replace("***null***","******"))) {
                positionNoKey = currentSetPosition;
            }
        }
        if (position<0 && positionVariation>=0) {
            position = positionVariation;
        }

        if (position>-1) {
            // If a key was specified in the set and it matches this song, go to that position in the set
            Log.d(TAG,"found song with key at position:"+position);
            return position;
        } else {
            // If a key wasn't specified in the set, but the song folder/filename matches, go to that position

            // If a key was specified in the set, but the song clicked on in the song menu is different,
            // stay out of the set view by returning -1 for the found position.
            // Or simply, the song just isn't in the set
            Log.d(TAG,"found song with no key at position:"+positionNoKey);
            return positionNoKey;
        }
    }

    public void shuffleSet() {
        Log.d(TAG,"shuffleSet()");
        // Shuffle the currentSet item array - all entries are like $$_folder/filename_**key**__$$
        Collections.shuffle(mainActivityInterface.getCurrentSet().getSetItemInfos());

        finishChangingSet();
    }

    public void sortSet() {
        Log.d(TAG,"sortSet()");
        // Sort the currentSet item array - all entries are like $$_folder/filename_**key**__$$
        // Comparator used to process the items and sort case insensitive and including accented chars
        Comparator<SetItemInfo> comparator = (o1, o2) -> {
            Collator collator = Collator.getInstance(mainActivityInterface.getLocale());
            collator.setStrength(Collator.SECONDARY);
            return collator.compare(o1.songfilename,o2.songfilename);
        };
        Collections.sort(mainActivityInterface.getCurrentSet().getSetItemInfos(), comparator);
        finishChangingSet();
    }

    private void finishChangingSet() {
        Log.d(TAG,"finishChangingSet()");

        // Save the current set
        mainActivityInterface.getCurrentSet().updateCurrentSetPreferences();
        Log.d(TAG,"currentSetAfterChange:"+mainActivityInterface.getCurrentSet().getSetCurrent());

        // Now build the individual values from the set item array which we shuffled
        parseCurrentSet();

        // Now build the modified set string for comparision for saving
        String setCurrent = getSetAsPreferenceString();
        mainActivityInterface.getCurrentSet().setSetCurrent(setCurrent);
        indexSongInSet(mainActivityInterface.getSong());

        mainActivityInterface.notifyToInsertAllInlineSet();
        mainActivityInterface.getDisplayPrevNext().setPrevNext();
    }

    public void checkMissingKeys() {
        Log.d(TAG,"checkMissingKeys()");

        // Called once song indexing is complete
        // Some keys may not have been loaded to the database when they were first looked for
        // If there is an empty value, try again
        // Build an arraylist of the positions so we can redraw the adapter
        missingKeyPositions = new ArrayList<>();
        for (int x = 0; x<mainActivityInterface.getCurrentSet().getCurrentSetSize(); x++) {
            SetItemInfo setItemInfo = mainActivityInterface.getCurrentSet().getSetItemInfo(x);
            String key = fixNull(setItemInfo.songkey);
            if (key.isEmpty()) {
                // Try to load the key from the database
                if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("imageorpdf",setItemInfo.songfilename)) {
                    key = mainActivityInterface.getNonOpenSongSQLiteHelper().
                                    getKey(setItemInfo.songfolder, setItemInfo.songfilename);
                } else {
                    key = mainActivityInterface.getSQLiteHelper().
                                    getKey(setItemInfo.songfolder, setItemInfo.songfilename);
                }
                key = fixNull(key);
                setItemInfo.songkey = key;
                mainActivityInterface.getCurrentSet().setSetItemInfo(x,setItemInfo);
                missingKeyPositions.add(x);
            }
        }
    }
    public ArrayList<Integer> getMissingKeyPositions() {

        Log.d(TAG,"getMissingKeyPositions()");
        return missingKeyPositions;
    }
    public void nullMissingKeyPositions() {

        Log.d(TAG,"nullMissingKeyPositions()");
        missingKeyPositions = null;
    }

    public void saveTheSet() {
        Log.d(TAG,"saveTheSet()");

        // This saves the set to user preferences for loading in next time
        // Not to be confused with exporting/saving the set as a file
        String setString = getSetAsPreferenceString();
        mainActivityInterface.getCurrentSet().setSetCurrent(setString);
        mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits(setString);
        //mainActivityInterface.updateSetList();
        mainActivityInterface.updateSongList();
    }

    public String niceCustomLocationFromFolder(String folderLocation) {
        Log.d(TAG,"niceCustomLocationFromFolder()");

        // This gives a nice output for the folderLocation for viewing
        folderLocation = folderLocation.replace(customLocBasic,"");
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

    public ArrayList<String> getAllSets() {
        Log.d(TAG,"getAllSets()");

        return mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "");
    }
    public ArrayList<String> getCategories(ArrayList<String> allSets) {
        Log.d(TAG,"getCategories");

        ArrayList<String> categories = new ArrayList<>();

        for (String setName:allSets) {
            if (setName.contains(setCategorySeparator)) {
                String category = setName.substring(0,setName.indexOf(setCategorySeparator));
                if (!categories.contains(category)) {
                    categories.add(category);
                }
            }
        }
        Collator coll = Collator.getInstance(mainActivityInterface.getLocale());
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(categories, coll);
        categories.add(0,c.getString(R.string.mainfoldername));
        return categories;
    }
    public ArrayList<String> setsInCategory(ArrayList<String> allSets) {
        Log.d(TAG,"setsInCategory");

        ArrayList<String> availableSets = new ArrayList<>();
        String category = mainActivityInterface.getPreferences().getMyPreferenceString(
                "whichSetCategory", c.getString(R.string.mainfoldername));
        boolean mainCategory = category.equals(c.getString(R.string.mainfoldername));

        for (String possibleSet:allSets) {
            if (mainCategory && !possibleSet.contains(setCategorySeparator)) {
                availableSets.add(possibleSet);
            } else if (possibleSet.contains(category+setCategorySeparator)) {
                availableSets.add(possibleSet);
            }
        }
        Collator coll = Collator.getInstance(mainActivityInterface.getLocale());
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(availableSets, coll);
        return availableSets;
    }
    public ArrayList<String> listSetsWithCategories(ArrayList<String> allSets) {
        Log.d(TAG,"listSetsWithCategories");

        ArrayList<String> availableSets = new ArrayList<>();
        for (String possibleSet:allSets) {
            if (possibleSet.contains(setCategorySeparator)) {
                possibleSet = possibleSet.replace(setCategorySeparator,"/");
            } else {
                possibleSet = c.getString(R.string.mainfoldername) + "/" + possibleSet;
            }
            availableSets.add(possibleSet);
        }
        Collator collator;
        if (mainActivityInterface.getLocale() == null) {
            collator = Collator.getInstance(Locale.getDefault());
        } else {
            collator = Collator.getInstance(mainActivityInterface.getLocale());
        }
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(availableSets,collator);
        return availableSets;
    }
    public void makeVariation(int position) {
        Log.d(TAG,"makeVariation");

        // Takes the chosen song and copies it to a temporary file in the Variations folder
        // This also updates the set menu to point to the new temporary item
        // This allows the user to freely edit the variation object
        // This is also called after an item is clicked in a set with a different key to the default
        // If this happens, the variation is transposed by the required amount, but is a temporary variation
        // Temporary variations keep the original song folder in the currentSet.getFolder() variable, but change in the song.getFolder()

        // The set may have been edited and then the user clicks on a song, so save the set to preferences first
        mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getCurrentSet().getSetCurrent());

        // Get the current set item
        SetItemInfo setItemInfo = mainActivityInterface.getCurrentSet().getSetItemInfo(position);
        setItemInfo.songfolder = niceCustomLocationFromFolder(folderVariations);

        // Fix the item in the set
        mainActivityInterface.getCurrentSet().setSetItemInfo(position,setItemInfo);

        // Get the uri of the original file (if it exists)
        // We receive a song object as it isn't necessarily the one loaded to MainActivity
        Uri uriOriginal = mainActivityInterface.getStorageAccess().getUriForItem("Songs", setItemInfo.songfolder, setItemInfo.songfilename);

        // Get the uri of the new variation file (Variations/filename)

        // IV - When a received song - use the stored received song filename
        if (setItemInfo.songfilename.equals("ReceivedSong")) {
            setItemInfo.songfilename = mainActivityInterface.getNearbyConnections().getReceivedSongFilename();
        }

        Uri uriVariation = mainActivityInterface.getStorageAccess().getUriForItem(folderVariations, "", setItemInfo.songfilename);

        // As long as the original and target uris are different, do the copy
        if (!uriOriginal.equals(uriVariation)) {
            // Make sure there is a file to write the output to (remove any existing first)
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" makeVariation Create Variations/"+setItemInfo.songfilename+" deleteOld=true");
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uriVariation, null, folderVariations, "", setItemInfo.songfilename);

            // Get an input/output stream reference and copy (streams are closed in copyFile())
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uriOriginal);
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uriVariation);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" makeVariation copyFile from "+uriOriginal+" to Variations/"+setItemInfo.songfilename);
            boolean success = mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
            Log.d(TAG, "file copied from " + uriOriginal + " to " + uriVariation + ": " + success);
        }
    }

    public int getItemIcon(String valueToDecideFrom) {
        Log.d(TAG,"getItemIcon()");

        int icon;
        // Get rid of ** and ../
        valueToDecideFrom = valueToDecideFrom.replace(customLocBasic,"");
        valueToDecideFrom = valueToDecideFrom.replace(customLocStart,"");

        switch (valueToDecideFrom) {
            case "Slides":
                icon = R.drawable.projector_screen;
                break;
            case "Notes":
                icon = R.drawable.note_text;
                break;
            case "Scripture":
                icon = R.drawable.bible;
                break;
            case "Images":
                icon = R.drawable.image;
                break;
            case "Variations":
            case "Variation":
                icon = R.drawable.xml;
                break;
            case "PDF":
                icon = R.drawable.pdf;
                break;
            default:
                icon = R.drawable.music_note;
                break;
        }
        return icon;
    }
    public String getIconIdentifier(String folder, String filename) {
        // If the filename is an image, we use that
        String valueToDecideFrom;
        if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("image",filename)) {
            valueToDecideFrom = "Images";
        } else if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("pdf",filename)) {
            valueToDecideFrom = "PDF";
        } else if (folder.contains(customLocStart)) {
            valueToDecideFrom = folder.replace(customLocStart,"");
        } else {
            valueToDecideFrom = "Songs";
        }
        return valueToDecideFrom;
    }

    public String createSetXML() {
        Log.d(TAG,"createSetXML");

        StringBuilder stringBuilder = new StringBuilder();

        // The starting of the xml file
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n").
                append("<set name=\"").
                append(mainActivityInterface.getProcessSong().parseToHTMLEntities(
                        mainActivityInterface.getCurrentSet().getSetCurrentLastName())).
                append("\">\n  <slide_groups>\n");

        // Now go through each set entry and build the appropriate xml
        for (int x = 0; x < mainActivityInterface.getCurrentSet().getCurrentSetSize(); x++) {
            SetItemInfo setItemInfo = mainActivityInterface.getCurrentSet().getSetItemInfo(x);
            String key = fixNull(setItemInfo.songkey);
            // If the path isn't empty, add a forward slash to the end
            if (!setItemInfo.songfolder.isEmpty()) {
                setItemInfo.songfolder = setItemInfo.songfolder + "/";
            }
            setItemInfo.songfolder = setItemInfo.songfolder.replace("//","/");

            boolean isImage = setItemInfo.songfolder.contains("**Image") ||
                    setItemInfo.songfolder.contains("**"+c.getString(R.string.image));
            boolean isVariation = setItemInfo.songfolder.contains("**Variation") ||
                    setItemInfo.songfolder.contains("**"+c.getString(R.string.variation));
            boolean isScripture = setItemInfo.songfolder.contains("**Scripture") ||
                    setItemInfo.songfolder.contains("**"+c.getString(R.string.scripture));
            boolean isSlide = setItemInfo.songfolder.contains("**Slide") ||
                    setItemInfo.songfolder.contains("**"+c.getString(R.string.slide));
            boolean isNote = setItemInfo.songfolder.contains("**Note") ||
                    setItemInfo.songfolder.contains("**"+c.getString(R.string.note));

            if (isImage) {
                // Adding an image
                Song tempSong = getTempSong("**" + folderImages + "/_cache",
                        setItemInfo.songfilename);
                stringBuilder.append(buildImage(tempSong));

            } else if (isScripture) {
                // Adding a scripture
                Song tempSong = getTempSong("**" + folderScripture + "/_cache",
                        setItemInfo.songfilename);
                stringBuilder.append(buildScripture(tempSong));

            } else if (isVariation) {
                // Adding a variation
                Song tempSong = getTempSong("**" + folderVariations,
                        setItemInfo.songfilename);
                stringBuilder.append(buildVariation(tempSong));

            } else if (isSlide) {
                // Adding a slide
                Song tempSong = getTempSong("**" + folderSlides + "/_cache",
                        setItemInfo.songfilename);
                stringBuilder.append(buildSlide(tempSong));

            } else if (isNote) {
                // Adding a note
                Song tempSong = getTempSong("**" + folderNotes + "/_cache",
                        setItemInfo.songfilename);
                stringBuilder.append(buildNote(tempSong));

            } else {
                // Adding a song
                stringBuilder.append(buildSong(setItemInfo.songfolder,setItemInfo.songfilename,key));
            }
        }
        // Now add the final part of the xml
        stringBuilder.append("</slide_groups>\n</set>");

        return stringBuilder.toString();
    }
    private Song getTempSong(String folder, String name) {
        Log.d(TAG,"getTempSong()");

        Song tempSong = mainActivityInterface.getProcessSong().initialiseSong(folder,name);
        try {
            tempSong = mainActivityInterface.getLoadSong().doLoadSong(tempSong,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tempSong.setTitle(Uri.decode(tempSong.getTitle()));
        return tempSong;
    }
    private StringBuilder buildSong(String path, String name, String key) {
        Log.d(TAG,"buildSong()");

        // If we have a key set add this as a value.  Desktop will ignore
        String keyText = "";
        if (key!=null && !key.isEmpty()) {
            keyText = " prefKey=\"" + key + "\"";
        }
        StringBuilder sb = new StringBuilder();
        String pathText;
        if (path!=null && !path.replace("/","").equals(c.getString(R.string.mainfoldername))) {
            pathText = " path=\"" + mainActivityInterface.getProcessSong().parseToHTMLEntities(path) + "\"";
        } else {
            pathText = " path=\"/\"";
        }
        sb.append("  <slide_group name=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(name))
                .append("\" type=\"song\"")
                .append(pathText)
                .append(keyText)
                .append("/>\n");
        return sb;
    }
    private StringBuilder buildScripture(Song tempSong) {
        Log.d(TAG,"buildScripture()");

        StringBuilder sb = new StringBuilder();

        // The scripture is loaded to a new, temp song object
        String scripture_lyrics = tempSong.getLyrics();

        // Parse the lyrics into individual slides;
        scripture_lyrics = scripture_lyrics.replace("[]", "_SPLITHERE_");

        String[] mySlides = scripture_lyrics.split("_SPLITHERE_");

        sb.append("<slide_group type=\"scripture\" name=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getTitle()))
                .append("\">\n")
                .append("    ")
                .append(emptyTagCheck("title",tempSong.getTitle()))
                .append("\n    ")
                .append(emptyTagCheck("subtitle",tempSong.getAuthor()))
                .append("\n");

        sb.append("    <slides>\n");
        for (String mySlide : mySlides) {
            if (mySlide != null && mySlide.length() > 0) {
                String text = mySlide.trim();
                text = text.replace(" \n","\n");
                text = text.replace("\n ","\n");
                text = text.replace("\n"," ").trim();
                sb.append("      <slide>\n")
                        .append("      ")
                        .append(emptyTagCheck("body",text))
                        .append("\n")
                        .append("      </slide>\n");
            }
        }
        sb.append("    </slides>\n")
                .append( "    <notes />\n")
                .append("  </slide_group>\n");
        return sb;
    }
    private StringBuilder buildVariation(Song tempSong) {
        Log.d(TAG,"buildVariation()");

        StringBuilder sb = new StringBuilder();

        // The variation is loaded to a new, temp song object
        // The entire song is copied to the notes as an encrypted string, and a simplified version is copied to the text

        String slide_lyrics = tempSong.getLyrics();
        try {
            byte[] data = mainActivityInterface.getProcessSong().getXML(tempSong).getBytes(tempSong.getEncoding());
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
                        .append("        ")
                        .append(emptyTagCheck("body",newslides.get(z).trim()))
                        .append("\n")
                        .append("      </slide>\n");
            }
        }

        sb.append("  <slide_group name=\"# ")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(c.getString(R.string.variation)))
                .append(" # - ")
                .append(tempSong.getFilename())
                .append("\"")
                .append(" type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\" prefKey=\"")
                .append(tempSong.getKey())
                .append("\">\n")
                .append("    ")
                .append(emptyTagCheck("title",tempSong.getTitle()))
                .append("\n    ")
                .append(emptyTagCheck("subtitle",tempSong.getAuthor()))
                .append("\n    ")
                .append(emptyTagCheck("notes",slide_lyrics))
                .append("\n    <slides>\n")
                .append(slidetexttowrite)
                .append("    </slides>\n")
                .append("  </slide_group>\n");

        return sb;
    }
    private StringBuilder buildSlide(Song tempSong) {
        Log.d(TAG,"buildSlide()");

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
                .append("\" type=\"custom\" seconds=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getUser1()))
                .append("\" loop=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getUser2()))
                .append("\" transition=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getCopyright()))
                .append("\">\n    ")
                .append(emptyTagCheck("title",tempSong.getTitle()))
                .append("\n    ")
                .append(emptyTagCheck("subtitle",tempSong.getAuthor()))
                .append("\n    <notes/>\n    <slides>\n");

        for (String mySlide : mySlides) {
            if (mySlide != null && mySlide.length() > 0) {
                sb.append("      <slide>\n        ")
                        .append(emptyTagCheck("body",mySlide.trim()))
                        .append("\n      </slide>\n");
            }
        }

        sb.append("    </slides>\n  </slide_group>\n");

        return sb;
    }
    private StringBuilder buildNote(Song tempSong) {
        Log.d(TAG,"buildNote()");

        StringBuilder sb = new StringBuilder();
        // Adding a note

        String slide_lyrics = tempSong.getLyrics();

        sb.append("  <slide_group name=\"# ")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(c.getResources().getString(R.string.note)))
                .append(" # - ")
                .append(tempSong.getFilename())
                .append("\" type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\">\n")
                .append("    ")
                .append(emptyTagCheck("title",tempSong.getTitle()))
                .append("\n    ")
                .append(emptyTagCheck("subtitle",tempSong.getAuthor()))
                .append("\n    ")
                .append(emptyTagCheck("notes",slide_lyrics))
                .append("\n")
                .append("    <slides></slides>\n")
                .append("  </slide_group>\n");

        return sb;
    }
    private StringBuilder buildImage(Song tempSong) {
        Log.d(TAG,"buildImage()");

        // Adding a custom image slide
        StringBuilder sb = new StringBuilder();

        // The mUser3 field should contain all the images
        // Break all the images into the relevant slides
        String[] separate_slide = tempSong.getUser3().trim().split("\n");

        StringBuilder slideCode = new StringBuilder();
        for (String aSeparate_slide : separate_slide) {
            String imglinetext;
            // Try to get the image into bytes
            String imgcode = mainActivityInterface.getStorageAccess().getImageSlide(
                    aSeparate_slide);
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
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getTitle()))
                .append("\" type=\"image\" print=\"true\" seconds=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getUser1()))
                .append("\" loop=\"")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(tempSong.getUser2()))
                .append("\" transition=\"0\" resize=\"screen\" keep_aspect=\"false\" link=\"false\">\n")
                .append("    ")
                .append(emptyTagCheck("title",tempSong.getTitle()))
                .append("\n    ")
                .append(emptyTagCheck("subtitle",tempSong.getAuthor()))
                .append("\n    ")
                .append(emptyTagCheck("notes",tempSong.getKey()))
                .append("\n    ")
                .append("  <slides>\n")
                .append(slideCode)
                .append("\n    ")
                .append("  </slides>\n")
                .append("  </slide_group>\n");

        return sb;
    }

    public void loadSets(ArrayList<Uri> setsToLoad, String setName) {
        Log.d(TAG,"loadSets()");

        // This is called via a new thread in the manage sets fragment
        // We can append multiple sets together
        // If the set loaded has a song has a key specified with it, we compare with our key
        // If it is different, the app creates a variation of our song then transposes it
        // Any variations bundled in the set are extracted as variations here too

        // First up, clear out our _cache folders after we remove any entries from the database
        removeCacheItemsFromDB(folderScripture);
        removeCacheItemsFromDB(folderSlides);
        removeCacheItemsFromDB(folderNotes);
        removeCacheItemsFromDB(folderImages);
        removeCacheItemsFromDB(folderVariations);

        // Create the cache directories again as we likely deleted them in SAF
        mainActivityInterface.getStorageAccess().createOrCheckRootFolders(null);

        // Initialise the arrays that will hold the loaded information
        //mainActivityInterface.getCurrentSet().initialiseTheSet();

        // Prepare the set name
        mainActivityInterface.getCurrentSet().setSetCurrentLastName(setName);

        // Now users can load multiple sets and merge them, we need to load each one it turn
        for (Uri setToLoad:setsToLoad) {
            // Pass each uri to the set extraction function and let it populate the arrays
            extractSetFile(setToLoad, false);
        }

        // Now we have the entire set contents, save it to our preferences
        saveTheSet();

        // Now update the view
        mainActivityInterface.getCurrentSet().updateSetTitleView();
    }

    public void extractSetFile(Uri uri, boolean asExport) {
        Log.d(TAG,"extractSetFile()");

        // This loads individual set files and populates the arrays
        // Set up the xml utility
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            String utf = mainActivityInterface.getStorageAccess().getUTFEncoding(uri);
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
            if (inputStream != null) {
                xpp.setInput(inputStream, utf);
                int eventType;
                eventType = xpp.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("slide_group")) {
                            // Look for the type attribute and see what type of slide it is
                            switch (xpp.getAttributeValue(null, "type")) {
                                case "song":
                                    // Get Song
                                    try {
                                        getSong(xpp,asExport);
                                    } catch (Exception e) {
                                        Log.d(TAG, "Couldn't get song location from set");
                                        e.printStackTrace();
                                    }
                                    break;
                                case "scripture":
                                    // Get Scripture
                                    try {
                                        getScripture(xpp,asExport);
                                    } catch (Exception e) {
                                        Log.d(TAG, "Couldn't get scripture from set");
                                        e.printStackTrace();
                                    }
                                    break;
                                case "custom":
                                    // Get Custom (Note or slide or variation)
                                    try {
                                        getCustom(xpp,asExport);
                                    } catch (Exception e) {
                                        Log.d(TAG, "Couldn't get custom from set");
                                        e.printStackTrace();
                                    }
                                    break;
                                case "image":
                                    // Get the Image(s)
                                    try {
                                        getImage(xpp,asExport);
                                    } catch (Exception e) {
                                        Log.d(TAG, "Couldn't get image from set");
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                        }
                    }
                    eventType = xpp.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String stripSlashes(String string) {
        Log.d(TAG,"stripSlashes()");

        if (string.startsWith("/")) {
            string = string.replaceFirst("/", "");
        }
        if (string.endsWith("/")) {
            string = string.substring(0, string.lastIndexOf("/"));
        }
        return string;
    }

    private void getSong(XmlPullParser xpp, boolean asExport)
            throws IOException, XmlPullParserException {
        Log.d(TAG,"getSong()");

        // Set this info into the current set.  We will just load our song
        // When we load, we will transpose our song if the key is different
        String path = stripSlashes(mainActivityInterface.getProcessSong().
                parseHTML(xpp.getAttributeValue(null,"path")));
        String name = stripSlashes(mainActivityInterface.getProcessSong().
                parseHTML(xpp.getAttributeValue(null,"name")));
        String key = "";
        if (xpp.getAttributeCount()>2) {
            // Assume a key has been set as well
            key = xpp.getAttributeValue("", "prefKey");
        }

        key = fixNull(key);

        if (path.isEmpty()) {
            path = c.getString(R.string.mainfoldername);
        }

        if (!asExport) {
            // Only add to the current set if we aren't just preparing an export
            mainActivityInterface.getCurrentSet().addItemToSet(path,name,name,key);
        }
        xpp.nextTag();
    }

    private void getScripture(XmlPullParser xpp, boolean asExport) throws IOException, XmlPullParserException {
        Log.d(TAG,"getScripture()");

        // Scripture entries in a set are custom slides.  Get the data and save it
        // This will ultimately be saved in our Scripture/_cache folder
        String scripture_title = "";
        String scripture_translation = "";
        StringBuilder scripture_text = new StringBuilder();
        String scripture_seconds = xpp.getAttributeValue("", "seconds");
        String scripture_loop = xpp.getAttributeValue("", "loop");
        boolean scripture_finished = false;

        while (!scripture_finished) {
            switch (xpp.getName()) {
                case "title":
                    scripture_title = safeNextText(xpp);
                    break;
                case "body":
                    scripture_text.append("\n[]\n").append(mainActivityInterface.getProcessSong().
                            parseHTML(safeNextText(xpp).trim()));
                    break;
                case "subtitle":
                    scripture_translation = mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp));
                    break;
            }

            xpp.nextTag();

            if (xpp.getEventType() == XmlPullParser.END_TAG) {
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

        // Add all the array back together and make sure no line goes above 50 characters
        // This means it won't appear tiny as the app tries to scale the lyrics
        ArrayList<String> vlines = new ArrayList<>();
        StringBuilder currline = new StringBuilder();
        for (String words : temp_text) {
            int check = currline.length();
            if (check > 50 || words.contains("[]")) {
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
        for (int i = 0; i < vlines.size(); i++) {
            String s = vlines.get(i);
            if (s != null && !s.equals("")) {
                scripture_text.append("\n").append(s);
            }
        }

        while (scripture_text.toString().contains("\\n\\n")) {
            scripture_text = new StringBuilder(scripture_text.toString().replace("\\n\\n", "\\n"));
        }

        // Make sure to safe encode the filename as it will likely have : in it
        Song tempSong = mainActivityInterface.getProcessSong().initialiseSong(
                customLocStart + folderScripture, Uri.encode(scripture_title));
        tempSong.setTitle(scripture_title);
        tempSong.setSongid(mainActivityInterface.getCommonSQL().getAnySongId(customLocStart + folderScripture, Uri.encode(scripture_title)));
        tempSong.setAuthor(scripture_translation);
        tempSong.setUser1(scripture_seconds);
        tempSong.setUser2(scripture_loop);
        tempSong.setLyrics(scripture_text.toString().trim());

        // Add to the set if we aren't exporting
        if (asExport) {
            // If we are exporting, put this file in the Export folder
            writeTempSlide(folderExport, "", tempSong);

        } else {
            mainActivityInterface.getCurrentSet().addItemToSet(tempSong.getFolder(),tempSong.getFilename(),tempSong.getTitle(),"");

            // Now create the file in the Scripture/_cache folder
            writeTempSlide(folderScripture, cache, tempSong);
        }
        xpp.nextTag();
    }

    private void getCustom(XmlPullParser xpp, boolean asExport) throws XmlPullParserException {
        Log.d(TAG,"getCustom()");

        // Could be a note or a slide or a variation
        // Notes have # Note # - in the name
        // Variations have # Variation # - in the name
        String custom_name = mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "name"));
        String custom_seconds = mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "seconds"));
        String custom_loop = mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "loop"));
        String custom_key = mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null,"prefKey"));
        String custom_title = "";
        String custom_subtitle = "";
        String custom_notes = "";
        String custom_background = "";
        StringBuilder custom_text = new StringBuilder();
        String tempcache = cache;

        boolean custom_finished = false;
        while (!custom_finished) {
            if (xpp.getEventType() == XmlPullParser.START_TAG && !xpp.isEmptyElementTag()) {
                switch (xpp.getName()) {
                    case "title":
                        if (xpp.getEventType() == XmlPullParser.START_TAG && !xpp.isEmptyElementTag()) {
                            custom_title = mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp));
                        }
                        break;
                    case "notes":
                        if (xpp.getEventType() == XmlPullParser.START_TAG && !xpp.isEmptyElementTag()) {
                            custom_notes = mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp));
                        }
                        break;
                    case "body":
                        if (xpp.getEventType() == XmlPullParser.START_TAG && !xpp.isEmptyElementTag()) {
                            try {
                                custom_text.append("\n---\n").append(mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp)));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "subtitle":
                        if (xpp.getEventType() == XmlPullParser.START_TAG && !xpp.isEmptyElementTag()) {
                            custom_subtitle = mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp));
                        }
                        break;
                    case "background":
                        if (xpp.getEventType() == XmlPullParser.START_TAG && !xpp.isEmptyElementTag()) {
                            custom_background = safeNextText(xpp);
                        }
                        break;
                    case "tabs":
                    case "song_subtitle":
                    default:
                        // Do nothing
                        break;
                }
            }

            try {
                if (xpp.getEventType() == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("slides")) {
                        custom_finished = true;
                    }
                    try {
                        xpp.nextTag();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (xpp.getEventType() == XmlPullParser.TEXT) {
                    xpp.nextTag();
                } else {
                    xpp.next();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // Remove first ---
        if (custom_text.toString().startsWith("\n---\n")) {
            custom_text = new StringBuilder(custom_text.toString().replaceFirst("\n---\n", ""));
        }
        if (custom_notes.startsWith("\n---\n")) {
            custom_notes = custom_notes.replaceFirst("\n---\n", "");
        }

        // Get a new tempSong ready for the info
        // Make sure to safe encode the filename as it might have unsafe characters
        Song tempSong = mainActivityInterface.getProcessSong().initialiseSong(
                customLocStart + folderSlides, mainActivityInterface.getStorageAccess().safeFilename(custom_title));

        if (custom_name.contains("# " + c.getResources().getString(R.string.note) + " # - ")) {
            // Prepare for a note
            custom_name = custom_name.replace("# " + c.getResources().getString(R.string.note) + " # - ", "");
            tempSong.setFolder(customLocStart + folderNotes);

        } else if (custom_name.contains("# " + c.getResources().getString(R.string.variation) + " # - ")) {
            // Prepare for a variation
            custom_name = custom_name.replace("# " + c.getResources().getString(R.string.variation) + " # - ", "");
            tempSong.setFolder(customLocStart + folderVariations);
            tempSong.setKey(custom_key);
            tempcache = "";
        }

        if (custom_text.toString().trim().isEmpty()) {
            custom_text = new StringBuilder(custom_notes);
        }

        // Make sure to safe encode the filename as it might have unsafe characters
        tempSong.setFilename(mainActivityInterface.getStorageAccess().safeFilename(custom_name));
        tempSong.setTitle(custom_name);

        if (tempSong.getFolder().contains(customLocStart + folderVariations) ||
            tempSong.getFolder().contains(customLocBasic + folderVariations)) {
            // The song is encoded in the custom_notes
            byte[] decodedString = Base64.decode(custom_notes, Base64.DEFAULT);
            String s;
            try {
                s = new String(decodedString, StandardCharsets.UTF_8);
            } catch (Exception e) {
                s = custom_notes;
                e.printStackTrace();
            }

            if (asExport) {
                mainActivityInterface.getStorageAccess().doStringWriteToFile(folderExport,"",tempSong.getFilename(),s);

            } else {
                mainActivityInterface.getStorageAccess().doStringWriteToFile(tempSong.getFolder().replace(customLocStart, ""), tempcache, tempSong.getFilename(), s);

                // Get the file
                tempSong = mainActivityInterface.getLoadSong().doLoadSongFile(tempSong, false);

                // Add the slide to the set
                mainActivityInterface.getCurrentSet().addItemToSet(tempSong);
            }
        } else {
            tempSong.setLyrics(custom_text.toString());
            tempSong.setKey(custom_key);
            tempSong.setUser1(custom_seconds);
            tempSong.setUser2(custom_loop);
            tempSong.setAuthor(custom_subtitle);
            tempSong.setAka(custom_background);
            tempSong.setHymnnum(custom_notes);

            // Add the slide to the set
            if (asExport) {
                writeTempSlide(folderExport,"",tempSong);

            } else {
                mainActivityInterface.getCurrentSet().addItemToSet(tempSong);

                // Now create the file in the appropriate location /_cache folder
                writeTempSlide(
                        tempSong.getFolder().replace(customLocStart, ""), tempcache, tempSong);

            }
        }
    }

    private void getImage(XmlPullParser xpp, boolean asExport) throws IOException, XmlPullParserException {
        Log.d(TAG,"getImage()");

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
                        image_title = new StringBuilder(mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp)));
                        break;
                    case "subtitle":
                        image_subtitle = mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp));
                        break;
                    case "notes":
                        image_notes = mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp));
                        break;
                    case "filename":
                        image_filename = mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp));
                        if (!image_filename.isEmpty()) {
                            slide_images.append(image_filename).append("\n");
                            slide_image_titles.append("[").append(c.getResources().getString(R.string.image))
                                    .append("_").append(imagenums + 1).append("]\n").append(image_filename)
                                    .append("\n\n");
                            imagenums++;
                            encodedimage = false;
                        }
                        break;
                    case "image":
                        image_content = safeNextText(xpp);
                        hymn_number_imagecode.append(image_content.trim()).append("XX_IMAGE_XX");
                        encodedimage = true;
                        break;
                    case "description":
                        String file_name = mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp));
                        if (file_name.toLowerCase(Locale.ROOT).contains(".png")) {
                            image_type = ".png";
                        } else if (file_name.toLowerCase(Locale.ROOT).contains(".gif")) {
                            image_type = ".gif";
                        } else {
                            image_type = ".jpg";
                        }

                        if (!asExport && encodedimage) {
                            // Save this image content
                            // Need to see if the image already exists
                            if (image_title.toString().equals("")) {
                                image_title = new StringBuilder(c.getResources().getString(R.string.image));
                            }

                            String safeFilename = mainActivityInterface.getStorageAccess().
                                    safeFilename(image_title.toString() + imagenums + image_type);
                            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(folderImages, cache, safeFilename);

                            // Check the uri exists for the outputstream to be valid
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" getImage Create Images/_cache/"+safeFilename+" deleteOld=true");
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null,
                                    folderImages, cache, safeFilename);

                            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                            byte[] decodedString = Base64.decode(image_content, Base64.DEFAULT);
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" getImage writeFileFromDecodedImageString "+uri+" with: "+ Arrays.toString(decodedString));
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
        // Make sure to safe encode the filename as it might have unsafe characters
        Song tempSong = mainActivityInterface.getProcessSong().initialiseSong(
                customLocStart+folderImages, Uri.encode(image_title.toString()));

        tempSong.setTitle(image_title.toString());
        tempSong.setAuthor(image_subtitle);
        tempSong.setUser1(image_seconds);
        tempSong.setUser2(image_loop);
        tempSong.setUser3(slide_images.toString().trim());
        tempSong.setAka(image_name);
        tempSong.setKey(image_notes);
        tempSong.setLyrics(slide_image_titles.toString().trim());

        if (asExport) {
            writeTempSlide(folderExport,"",tempSong);

        } else {
            // Add the set item
            mainActivityInterface.getCurrentSet().addItemToSet(tempSong);
        }
    }

    private String fixNull(String s) {
        if (s == null) {
            s = "";
        }
        return s;
    }

    private void removeCacheItemsFromDB(String folder) {
        Log.d(TAG,"removeCacheItemsFromDF");

        ArrayList<String> filesInFolder = mainActivityInterface.getStorageAccess().listFilesInFolder(folder, "_cache");
        for (String filename:filesInFolder) {
            mainActivityInterface.getSQLiteHelper().deleteSong(customLocStart+folder, filename);
        }

        // Now empty the actual folder
        mainActivityInterface.getStorageAccess().wipeFolder(folder, "_cache");

        if (folder.equals(folderVariations)) {
            // Also clear the non-cache folder
            ArrayList<String> filesInNonCacheFolder = mainActivityInterface.getStorageAccess().listFilesInFolder(folder, "");
            for (String filename:filesInNonCacheFolder) {
                mainActivityInterface.getSQLiteHelper().deleteSong(customLocStart+folder, filename);
            }

            // Now empty the actual folder
            mainActivityInterface.getStorageAccess().wipeFolder(folder, "");
        }
    }

    private void writeTempSlide(String folder, String subfolder, Song tempSong) {
        Log.d(TAG,"writeTempSlide");

        // Get the song as XML
        tempSong.setSongXML(mainActivityInterface.getProcessSong().getXML(tempSong));
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" writeTempSlide doStringWriteToFile "+folder+"/"+subfolder+"/"+tempSong.getFilename()+" with: "+tempSong.getSongXML());
        mainActivityInterface.getStorageAccess().doStringWriteToFile(folder,subfolder,tempSong.getFilename(),tempSong.getSongXML());
    }

    private String safeNextText(XmlPullParser xpp) {
        Log.d(TAG,"safeNextText()");

        try {
            if (!xpp.isEmptyElementTag()) {
                String result = xpp.nextText();
                if (xpp.getEventType() != XmlPullParser.END_TAG) {
                    xpp.nextTag();
                }
                return result;
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    private String emptyTagCheck(String tag, String value) {
        Log.d(TAG,"emptyTagCheck()");

        if (value!=null && !value.isEmpty()) {
            return "<" + tag + ">" + mainActivityInterface.getProcessSong().parseToHTMLEntities(value) + "</" + tag + ">";
        } else {
            return "<" + tag + "/>";
        }
    }

}