package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.openchords.OpenChordsSetListItem;
import com.garethevans.church.opensongtablet.openchords.OpenChordsSetListSongItem;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.gson.Gson;

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
import java.util.UUID;

// This class interacts with the CurrentSet object and does the processing, etc.

public class SetActions {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final String itemStart = "$**_";
    private final String itemEnd = "_**$";
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
    private String useThisLastModifedDate = null;
    private final String dividerIdentifier = "---";
    public static final Gson gson = new Gson();

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
    // The currentSet can be the actual mainActivityInterface.getCurrentSet()
    // Or a temporary currentSet object used for preview parsing


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
                String key = getKey(setItem);
                setItem = setItem.replace(mainActivityInterface.getVariations().getKeyStart() +
                        key + mainActivityInterface.getVariations().getKeyEnd(),"");

                // Now get the folder and filename
                String folder = mainActivityInterface.getMainfoldername();
                if (!setItem.contains("/")) {
                    setItem = "/" + setItem;
                }
                if (setItem.lastIndexOf("/")!=0) {
                    folder = setItem.substring(0, setItem.lastIndexOf("/"));
                    setItem = setItem.replace(folder+"/","");
                }
                String foldernice = folder;
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

    private String getKey(String setItem) {
        String key = "";
        if (setItem.contains(mainActivityInterface.getVariations().getKeyStart()) &&
                setItem.contains(mainActivityInterface.getVariations().getKeyEnd()) &&
                setItem.indexOf(mainActivityInterface.getVariations().getKeyStart()) <
                        setItem.indexOf(mainActivityInterface.getVariations().getKeyEnd())) {
            key = setItem.substring(setItem.indexOf(mainActivityInterface.getVariations().getKeyStart()),
                    setItem.indexOf(mainActivityInterface.getVariations().getKeyEnd()));
            key = key.replace(mainActivityInterface.getVariations().getKeyStart(),"");
            key = key.replace(mainActivityInterface.getVariations().getKeyEnd(),"");
        }
        return key;
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
        // To avoid concurrent modification, don't use enhanced for loop
        //for (SetItemInfo setItemInfo : mainActivityInterface.getCurrentSet().getSetItemInfos()) {
        for (int x=0; x<mainActivityInterface.getCurrentSet().getSetItemInfos().size(); x++) {
            SetItemInfo setItemInfo = mainActivityInterface.getCurrentSet().getSetItemInfo(x);
            if (folderNamePair.equals(setItemInfo.songfolder + "/" + setItemInfo.songfilename)) {
                inSet = true;
                break;
            }
        }
        return inSet;
    }

    public boolean isSongInSet(Song song) {
        boolean inSet = false;
        // To avoid concurrent modification, don't use enhanced for loop
        //for (SetItemInfo setItemInfo : mainActivityInterface.getCurrentSet().getSetItemInfos()) {
        for (int x=0; x<mainActivityInterface.getCurrentSet().getSetItemInfos().size(); x++) {
            SetItemInfo setItemInfo = mainActivityInterface.getCurrentSet().getSetItemInfo(x);
            if (song.getFolder().equals(setItemInfo.songfolder) && song.getFilename().equals(setItemInfo.songfilename)) {
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
                            append(mainActivityInterface.getVariations().getKeyStart()).
                            append(key).
                            append(mainActivityInterface.getVariations().getKeyEnd()).
                            append(itemEnd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return stringBuilder.toString();
    }

    // Get a reference string for thisSong for working with the currentSet string in preferences
    public String getSongForSetWork(SetItemInfo setItemInfo) {
        if (mainActivityInterface.getVariations().getIsKeyVariation(setItemInfo.songfolder,setItemInfo.songfilename)) {
            String[] originalFolderFile = mainActivityInterface.getVariations().getPreVariationInfo(setItemInfo);
            setItemInfo.songfolder = originalFolderFile[0];
            setItemInfo.songfilename = originalFolderFile[1];
            setItemInfo.songkey = originalFolderFile[2];
        }
        return itemStart + setItemInfo.songfolder + "/" + setItemInfo.songfilename +
                mainActivityInterface.getVariations().getKeyStart() +
                setItemInfo.songkey +
                mainActivityInterface.getVariations().getKeyEnd() + itemEnd;
    }
    public String getSongForSetWork(String folder, String filename, String key) {
        return itemStart + folder + "/" + filename +
                mainActivityInterface.getVariations().getKeyStart() +
                key +
                mainActivityInterface.getVariations().getKeyEnd() + itemEnd;
    }

    // Index the song in the set
    public int indexSongInSet(SetItemInfo setItemInfo) {
        return indexSongInSet(setItemInfo.songfolder,setItemInfo.songfilename,setItemInfo.songkey);
    }
    public int indexSongInSet(Song thisSong) {
        return indexSongInSet(thisSong.getFolder(), thisSong.getFilename(), thisSong.getKey());
    }
    public int indexSongInSet(String folder, String filename, @Nullable String key) {
        int position;

        // Firstly get the key if null and is in the filename
        if (mainActivityInterface.getVariations().getIsKeyVariation(folder,filename)) {
            Log.d(TAG,"find the key from variations logic");
            String[] bits = mainActivityInterface.getVariations().getPreVariationInfo(folder, filename, key);
            folder = bits[0];
            filename = bits[1];
            key = bits[2];
            Log.d(TAG,"found key:"+key);
        }

        if (key==null) {
            key = "";
        }

        // Because set items can be stored with or without a specified key, we search for both

        // First the item with the key
        String searchItemWithKey = getSongForSetWork(folder,filename,key);
        String searchItemWithoutKey = getSongForSetWork(folder,filename,"");
        String searchItemPartialNoKey = searchItemWithoutKey.replace(mainActivityInterface.getVariations().getKeyStart(),"").
                replace(mainActivityInterface.getVariations().getKeyEnd(),"").
                replace(itemEnd,"") +
                mainActivityInterface.getVariations().getKeyStart();

        // Try the first option
        position = mainActivityInterface.getCurrentSet().getMatchingSetItem(searchItemWithKey);

        // If we didn't find it, try the second option
        if (position == -1) {
            position = mainActivityInterface.getCurrentSet().getMatchingSetItem(searchItemWithoutKey);
        }

        // If we didn't find it, try the third option
        if (position == -1) {
            position = mainActivityInterface.getCurrentSet().getPartialMatchingSetItem(searchItemPartialNoKey);
        }

        mainActivityInterface.getCurrentSet().setIndexSongInSet(position);

        return position;
    }

    // Shuffle or sort the set
    public void shuffleSet() {
        // Shuffle the currentSet item array - all entries are like $$_folder/filename_**key**__$$
        Collections.shuffle(mainActivityInterface.getCurrentSet().getSetItemInfos());

        finishChangingSet();
    }
    public void sortSet() {
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
        // Save the current set
        mainActivityInterface.getCurrentSet().updateCurrentSetPreferences();

        // Now build the individual values from the set item array which we shuffled
        parseCurrentSet();

        // Now build the modified set string for comparision for saving
        String mySetCurrent = getSetAsPreferenceString();
        mainActivityInterface.getCurrentSet().setSetCurrent(mySetCurrent);
        indexSongInSet(mainActivityInterface.getSong());

        mainActivityInterface.notifyToInsertAllInlineSet();
        mainActivityInterface.getDisplayPrevNext().setPrevNext();
    }

    public void checkMissingKeys() {
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
        return missingKeyPositions;
    }
    public void nullMissingKeyPositions() {
        missingKeyPositions = null;
    }

    public void saveTheSet() {
        // This saves the set to user preferences for loading in next time
        // Not to be confused with exporting/saving the set as a file
        String setString = getSetAsPreferenceString();
        mainActivityInterface.getCurrentSet().setSetCurrent(setString);
        mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits(setString);
        //mainActivityInterface.updateSetList();
        mainActivityInterface.updateSongList();
    }

    public ArrayList<String> getAllSets() {
        return mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "");
    }
    public ArrayList<String> getRequiredSets(boolean all) {
        // If we want all of them, we simply want all the filenames
        if (all) {
            return getAllSets();
        } else {
            // Get the sets in the current user preference category
            ArrayList<String> returnSets = new ArrayList<>();
            String category = mainActivityInterface.getPreferences().getMyPreferenceString(
                    "whichSetCategory", mainActivityInterface.getMainfoldername());
            boolean isMain = category.equals(mainActivityInterface.getMainfoldername());
            for (String set:getAllSets()) {
                if (isMain && !set.contains(setCategorySeparator)) {
                    returnSets.add(set);
                } else if (!isMain && set.startsWith(category+setCategorySeparator)) {
                    returnSets.add(set);
                }
            }
            return returnSets;
        }
    }
    public ArrayList<String> getCategories(ArrayList<String> allSets) {
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
        // Add the OpenChords category if we don't have it
        if (!categories.contains(c.getString(R.string.openchords))) {
            categories.add(0,c.getString(R.string.openchords));
        }
        // Add the MAIN category
        categories.add(0,c.getString(R.string.mainfoldername));
        return categories;
    }

    public int getItemIcon(String valueToDecideFrom) {
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
            case "Divider":
                icon = R.drawable.divider;
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
        if (folder.equals(mainActivityInterface.getSetActions().getDividerIdentifier())) {
            valueToDecideFrom = "Divider";
        } else if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("image",filename)) {
            valueToDecideFrom = "Images";
        } else if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("pdf",filename)) {
            valueToDecideFrom = "PDF";
        } else if (folder.contains(customLocStart)) {
            valueToDecideFrom = folder.replace(customLocStart, "");
        } else {
            valueToDecideFrom = "Songs";
        }
        return valueToDecideFrom;
    }

    public String getDividerIdentifier() {
        return dividerIdentifier;
    }
    public String createSetXML(CurrentSet thisCurrentSet) {
        StringBuilder stringBuilder = new StringBuilder();

        // The starting of the xml file
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n").
                append("<set name=\"").
                append(mainActivityInterface.getProcessSong().parseToHTMLEntities(
                        thisCurrentSet.getSetCurrentLastName())).
                append("\">\n<slide_groups>\n");

        // Now go through each set entry and build the appropriate xml
        for (int x = 0; x < thisCurrentSet.getCurrentSetSize(); x++) {
            SetItemInfo setItemInfo = thisCurrentSet.getSetItemInfo(x);

            String key = fixNull(setItemInfo.songkey);
            String folder = setItemInfo.songfolder;
            // If the path isn't empty, add a forward slash to the end
            if (!folder.isEmpty()) {
                folder = setItemInfo.songfolder + "/";
            }
            folder = folder.replace("//","/");

            boolean isImage = folder.contains("**Image") || folder.contains("**"+c.getString(R.string.image));
            boolean isVariation = folder.contains("**Variation") || folder.contains("**"+c.getString(R.string.variation));
            boolean isScripture = folder.contains("**Scripture") || folder.contains("**"+c.getString(R.string.scripture));
            boolean isSlide = folder.contains("**Slide") || folder.contains("**"+c.getString(R.string.slide));
            boolean isNote = folder.contains("**Note") || folder.contains("**"+c.getString(R.string.note));
            boolean isDivider = folder.contains("**Divider") || folder.contains(dividerIdentifier);
            if (isImage) {
                // Adding an image
                Song tempSong = getTempSong("**" + folderImages,
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
                tempSong.setTitle(tempSong.getFilename());
                stringBuilder.append(mainActivityInterface.getVariations().buildVariation(tempSong));

            } else if (isSlide) {
                // Adding a slide
                Song tempSong = getTempSong("**" + folderSlides + "/_cache",
                        setItemInfo.songfilename);
                stringBuilder.append(buildSlide(tempSong));

            } else if (isDivider) {
                // Dividers are actually stored as Notes
                // Adding a divider to the Notes folder
                Song tempSong = getTempSong("**" + folderNotes + "/_cache",
                        setItemInfo.songfilename);
                tempSong.setTheme("Divider");
                stringBuilder.append(buildDivider(tempSong));

            } else if (isNote) {
                // Adding a note
                Song tempSong = getTempSong("**" + folderNotes + "/_cache",
                        setItemInfo.songfilename);
                stringBuilder.append(buildNote(tempSong));

            } else {
                // Adding a song
                stringBuilder.append(buildSong(folder,setItemInfo.songfilename,key));
            }
        }
        // Now add the final part of the xml
        stringBuilder.append("</slide_groups>\n").
                append("<uuid>").append(thisCurrentSet.getUuid()).append("</uuid>\n");
        if (useThisLastModifedDate==null || useThisLastModifedDate.isEmpty()) {
            stringBuilder.append("<lastModified>").append(thisCurrentSet.getLastModified()).append("</lastModified>\n");
        } else {
            stringBuilder.append("<lastModified>").append(useThisLastModifedDate).append("</lastModified>\n");
        }
        stringBuilder.append("<notes>").append(thisCurrentSet.getNotes()).append("</notes>\n").
                append("</set>");

        return stringBuilder.toString();
    }
    private Song getTempSong(String folder, String name) {
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
            if (mySlide != null && !mySlide.isEmpty()) {
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

    private StringBuilder buildSlide(Song tempSong) {
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
            if (mySlide != null && !mySlide.isEmpty()) {
                sb.append("      <slide>\n        ")
                        .append(emptyTagCheck("body",mySlide.trim()))
                        .append("\n      </slide>\n");
            }
        }

        sb.append("    </slides>\n  </slide_group>\n");

        return sb;
    }
    private StringBuilder buildNote(Song tempSong) {
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
    private StringBuilder buildDivider(Song tempSong) {
        StringBuilder sb = new StringBuilder();
        // Adding a divider
        sb.append("    <slide_group name=\"# ")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(c.getResources().getString(R.string.divider)))
                .append(" # - ")
                .append(tempSong.getFilename())
                .append("\" type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\">\n")
                .append("    </slide_group>\n");
        return sb;
    }
    private StringBuilder buildImage(Song tempSong) {
        // Adding a custom image slide
        StringBuilder sb = new StringBuilder();

        // The mUser3 field should contain all the images
        // Break all the images into the relevant slides
        Log.d(TAG,"tempSong.getUser3()="+tempSong.getUser3());
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
                .append("  </slides>\n")
                .append("  </slide_group>\n");

        return sb;
    }

    public void loadSets(ArrayList<Uri> setsToLoad, CurrentSet currentSet, String setName) {
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

        // Prepare the set name
        mainActivityInterface.getCurrentSet().setSetCurrentLastName(setName);

        // Now users can load multiple sets and merge them, we need to load each one it turn
        for (Uri setToLoad:setsToLoad) {
            // Pass each uri to the set extraction function and let it populate the arrays
            extractSetFile(setToLoad, currentSet,false);
        }
        // Now we have the entire set contents, save it to our preferences
        saveTheSet();

        // Now update the view
        mainActivityInterface.getCurrentSet().updateSetTitleView();
    }

    public void extractSetFile(Uri uri, CurrentSet currentSet, boolean asExport) {
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
                        if (xpp.getName().equals("uuid")) {
                            getUuid(xpp,currentSet,asExport);
                        } else if (xpp.getName().equals("lastModified")) {
                            getLastModified(xpp,currentSet,asExport);
                        } else if (xpp.getName().equals("notes")) {
                            getNotes(xpp,currentSet,asExport);
                        } else if (xpp.getName().equals("slide_group")) {
                            // Look for the type attribute and see what type of slide it is
                            switch (xpp.getAttributeValue(null, "type")) {
                                case "song":
                                    // Get Song
                                    try {
                                        getSong(xpp,currentSet,asExport);
                                    } catch (Exception e) {
                                        Log.d(TAG, "Couldn't get song location from set");
                                        e.printStackTrace();
                                    }
                                    break;
                                case "scripture":
                                    // Get Scripture
                                    try {
                                        getScripture(xpp,currentSet,asExport);
                                    } catch (Exception e) {
                                        Log.d(TAG, "Couldn't get scripture from set");
                                        e.printStackTrace();
                                    }
                                    break;
                                case "custom":
                                    // Get Custom (Note or slide or variation)
                                    try {
                                        getCustom(xpp,currentSet,asExport);
                                    } catch (Exception e) {
                                        Log.d(TAG, "Couldn't get custom from set");
                                        e.printStackTrace();
                                    }
                                    break;
                                case "image":
                                    // Get the Image(s)
                                    try {
                                        getImage(xpp,currentSet,asExport);
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
        if (string.startsWith("/")) {
            string = string.replaceFirst("/", "");
        }
        if (string.endsWith("/")) {
            string = string.substring(0, string.lastIndexOf("/"));
        }
        return string;
    }

    private void getUuid(XmlPullParser xpp, CurrentSet currentSet, boolean asExport)
            throws IOException, XmlPullParserException {
        // Set this info into the current set
        if (!asExport && currentSet!=null) {
            currentSet.setUuid(xpp.nextText());
        }
        xpp.next();
    }
    private void getLastModified(XmlPullParser xpp, CurrentSet currentSet, boolean asExport)
            throws IOException, XmlPullParserException {
        // Set this info into the current set
        if (!asExport && currentSet!=null) {
            currentSet.setLastModified(xpp.nextText());
        }
        xpp.next();
    }
    private void getNotes(XmlPullParser xpp, CurrentSet currentSet, boolean asExport)
            throws IOException, XmlPullParserException {
        // Set this info into the current set
        if (!asExport && currentSet!=null) {
            currentSet.setNotes(getTrimmedOrNullText(xpp.nextText()));
        }
        xpp.next();
    }

    private void getSong(XmlPullParser xpp, CurrentSet currentSet, boolean asExport)
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
            key = xpp.getAttributeValue("", "prefKey");
        }

        key = fixNull(key);

        if (path.isEmpty()) {
            path = c.getString(R.string.mainfoldername);
        }

        if (!asExport && currentSet!=null) {
            // Only add to the current set if we aren't just preparing an export
            currentSet.addItemToSet(path,name,name,key,true);
        }
        xpp.next();
    }

    private void getScripture(XmlPullParser xpp, CurrentSet currentSet, boolean asExport) throws IOException, XmlPullParserException {
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
        ArrayList<String> vlines = getVlines(scripture_text);

        scripture_text = new StringBuilder();

        // Ok go back through the array and add the non-empty lines back up
        for (int i = 0; i < vlines.size(); i++) {
            String s = vlines.get(i);
            if (s != null && !s.isEmpty()) {
                scripture_text.append("\n").append(s);
            }
        }

        while (scripture_text.toString().contains("\\n\\n")) {
            scripture_text = new StringBuilder(scripture_text.toString().replace("\\n\\n", "\\n"));
        }

        // Make sure to safe encode the filename as it will likely have : in it
        // Make the filename safe, but not URI encoded
        String new_title = mainActivityInterface.getStorageAccess().safeFilename(scripture_title);
        new_title = Uri.decode(new_title);
        Song tempSong = mainActivityInterface.getProcessSong().initialiseSong(
                customLocStart + folderScripture, new_title);
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
            if (currentSet!=null) {
                currentSet.addItemToSet(tempSong.getFolder(), tempSong.getFilename(), tempSong.getTitle(), "", true);
            }
            // Now create the file in the Scripture/_cache folder
            writeTempSlide(folderScripture, cache, tempSong);
        }
        xpp.nextTag();
    }

    private ArrayList<String> getVlines(StringBuilder scripture_text) {
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
        return vlines;
    }

    private void getCustom(XmlPullParser xpp, CurrentSet currentSet, boolean asExport) throws XmlPullParserException {
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
        // Make the filename safe, but not URI encoded
        String new_title = mainActivityInterface.getStorageAccess().safeFilename(custom_title);
        new_title = Uri.decode(new_title);
        Song tempSong = mainActivityInterface.getProcessSong().initialiseSong(
                customLocStart + folderSlides, new_title);

        if (custom_name.contains("# " + c.getResources().getString(R.string.note) + " # - ")) {
            // Prepare for a note
            custom_name = custom_name.replace("# " + c.getResources().getString(R.string.note) + " # - ", "");
            tempSong.setFolder(customLocStart + folderNotes);

        } else if (custom_name.contains("# " + c.getResources().getString(R.string.variation) + " # - ")) {
            // Prepare for a variation
            custom_name = custom_name.replace("# " + c.getResources().getString(R.string.variation) + " # - ", "");
            // Check we have a folder, but if not add one
            if (!custom_name.contains("_")) {
                custom_name = mainActivityInterface.getMainfoldername() + "_" + custom_name;
            }
            tempSong.setTitle(custom_name);
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
                mainActivityInterface.getStorageAccess().writeFileFromString(folderExport,"",tempSong.getFilename(),s);

            } else {
                mainActivityInterface.getStorageAccess().writeFileFromString(tempSong.getFolder().replace(customLocStart, ""), tempcache, tempSong.getFilename(), s);

                // Get the file
                tempSong = mainActivityInterface.getLoadSong().doLoadSongFile(tempSong, false);

                // Add the slide to the set
                if (currentSet!=null) {
                    currentSet.addItemToSet(tempSong);
                }
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
                if (currentSet!=null) {
                    currentSet.addItemToSet(tempSong);
                }

                // Now create the file in the appropriate location /_cache folder
                writeTempSlide(
                        tempSong.getFolder().replace(customLocStart, ""), tempcache, tempSong);

            }
        }
    }

    private void getImage(XmlPullParser xpp, CurrentSet currentSet, boolean asExport) throws IOException, XmlPullParserException {
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
                            if (image_title.toString().isEmpty()) {
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

        if (image_title.toString().isEmpty()) {
            image_title = new StringBuilder(c.getResources().getString(R.string.image));
        }

        image_subtitle = fixNull(image_subtitle);
        image_seconds = fixNull(image_seconds);
        image_loop = fixNull(image_loop);
        image_name = fixNull(image_name);
        image_notes = fixNull(image_notes);

        // Get a new tempSong ready for the info
        // Make the filename safe, but not URI encoded
        String new_title = mainActivityInterface.getStorageAccess().safeFilename(image_title.toString());
        new_title = Uri.decode(new_title);
        Song tempSong = mainActivityInterface.getProcessSong().initialiseSong(
                customLocStart+folderImages, new_title);

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

        } else if (currentSet!=null) {
            // Add the set item
            currentSet.addItemToSet(tempSong);
        }
    }

    private String fixNull(String s) {
        if (s == null) {
            s = "";
        }
        return s;
    }

    private void removeCacheItemsFromDB(String folder) {
        ArrayList<String> filesInFolder = mainActivityInterface.getStorageAccess().listFilesInFolder(folder, "_cache");
        for (String filename:filesInFolder) {
            mainActivityInterface.getSQLiteHelper().deleteSong(customLocStart+folder, filename);
        }

        // Now empty the actual folder
        mainActivityInterface.getStorageAccess().wipeFolder(folder, "_cache");

        if (folder.equals(folderVariations)) {
            // Delete the Variations/_cache folder for now
            Uri variationscachefolder = mainActivityInterface.getStorageAccess().getUriForItem(folderVariations,cache,"");
            mainActivityInterface.getStorageAccess().deleteFile(variationscachefolder);

            // Also clear the non-cache folder
            ArrayList<String> filesInNonCacheFolder = mainActivityInterface.getStorageAccess().listFilesInFolder(folder, "");
            for (String filename:filesInNonCacheFolder) {
                mainActivityInterface.getSQLiteHelper().deleteSong(customLocStart+folder, filename);
            }

            // Now empty the actual folder
            mainActivityInterface.getStorageAccess().wipeFolder(folder, "");

            // Now recreate the _cache folder
            mainActivityInterface.getStorageAccess().createFolder(folder,"",cache,false);
        }
    }

    private void writeTempSlide(String folder, String subfolder, Song tempSong) {
        // Get the song as XML
        tempSong.setSongXML(mainActivityInterface.getProcessSong().getXML(tempSong));
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" writeTempSlide doStringWriteToFile "+folder+"/"+subfolder+"/"+tempSong.getFilename()+" with: "+tempSong.getSongXML());
        mainActivityInterface.getStorageAccess().writeFileFromString(folder,subfolder,tempSong.getFilename(),tempSong.getSongXML());
    }

    private String safeNextText(XmlPullParser xpp) {
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

    public String emptyTagCheck(String tag, String value) {
        if (value!=null && !value.isEmpty()) {
            return "<" + tag + ">" + mainActivityInterface.getProcessSong().parseToHTMLEntities(value) + "</" + tag + ">";
        } else {
            return "<" + tag + "/>";
        }
    }

    public String getSetCategorySeparator() {
        return setCategorySeparator;
    }

    public String getItemStart() {
        return itemStart;
    }

    public String getItemEnd() {
        return itemEnd;
    }

    public String getNiceSetNameFromFile(String filename) {
        // If the file has a category, make it look nicer
        if (filename.contains(setCategorySeparator)) {
            String[] bits = filename.split(setCategorySeparator);
            return "(" + bits[0] + ") " + bits[bits.length-1];
        } else {
            return filename;
        }
    }

    public String[] getSetCategoryAndName(String setFilename) {
        String[] returnBits = new String[2];
        if (!setFilename.contains(setCategorySeparator)) {
            returnBits[0] = mainActivityInterface.getMainfoldername();
            returnBits[1] = setFilename;
        } else {
            String[] bits = setFilename.split(mainActivityInterface.getSetActions().getSetCategorySeparator());
            returnBits[0] = bits[0];
            returnBits[1] = bits[bits.length-1];
        }
        return returnBits;
    }

    public void setUseThisLastModifiedDate(String useThisLastModifedDate) {
        this.useThisLastModifedDate = useThisLastModifedDate;
    }


    // New logic to convert between objects and xml
    public SetObject createSetObjectFromCurrent() {
        // We need to parse the set and populate the object
        SetObject setObject = new SetObject();
        setObject.setSetName(mainActivityInterface.getCurrentSet().getSetCurrentLastName());
        setObject.setUuid(mainActivityInterface.getCurrentSet().getUuid());
        setObject.setLastModified(mainActivityInterface.getCurrentSet().getLastModified());
        setObject.setNotes(mainActivityInterface.getCurrentSet().getNotes());
        ArrayList<SetSlideGroupObject> slideGroupObjects = new ArrayList<>();
        for (SetItemInfo setItemInfo:mainActivityInterface.getCurrentSet().getSetItemInfos()) {
            SetSlideGroupObject setSlideGroupObject = new SetSlideGroupObject();
            setSlideGroupObject.setName(setItemInfo.songfilename);
            setSlideGroupObject.setPath(setItemInfo.songfolder);
            setSlideGroupObject.setPrefKey(setItemInfo.songkey);
            setSlideGroupObject.setTitle(setItemInfo.songtitle);
            if (setItemInfo.songfolder.startsWith(folderVariations)) {
                setSlideGroupObject.setType("variation");
            } else if (setItemInfo.songfolder.startsWith(folderNotes)) {
                setSlideGroupObject.setType("notes");
            } else if (setItemInfo.songfolder.startsWith(folderSlides)) {
                setSlideGroupObject.setType("slides");
            } else if (setItemInfo.songfolder.startsWith(folderScripture)) {
                setSlideGroupObject.setType("scripture");
            } else if (setItemInfo.songfolder.startsWith(folderImages)) {
                setSlideGroupObject.setType("image");
            } else {
                setSlideGroupObject.setType("song");
            }
            slideGroupObjects.add(setSlideGroupObject);
        }
        setObject.setSlideGroups(slideGroupObjects);

        return setObject;
    }


    public SetObject createSetObjectFromFilename(String setFilename) {
        // We need to parse the set xml and populate the object
        SetObject setObject = null;

        Uri setUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", setFilename);
        setObject = new SetObject();
        setObject.setSetName(setFilename);if (mainActivityInterface.getStorageAccess().uriExists(setUri) && mainActivityInterface.getStorageAccess().getFileSizeFromUri(setUri)>0) {
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                String utf = mainActivityInterface.getStorageAccess().getUTFEncoding(setUri);
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(setUri);
                if (inputStream != null) {
                    boolean set_finished = false;
                    xpp.setInput(inputStream, utf);
                    xpp.next();
                    while (!set_finished) {
                        if (xpp.getEventType() == XmlPullParser.START_TAG) {
                            String name = xpp.getName();
                            if (name != null) {
                                switch (name) {
                                    case "uuid":
                                        setObject.setUuid(xpp.nextText());
                                        break;
                                    case "lastModified":
                                    case "lastUpdated":
                                        setObject.setLastModified(xpp.nextText());
                                        break;
                                    case "notes":
                                        setObject.setNotes(getTrimmedOrNullText(xpp.nextText()));
                                        break;
                                    case "slide_group":
                                        SetSlideGroupObject setSlideGroupObject = new SetSlideGroupObject();
                                        // Look for the type attribute and see what type of slide it is
                                        switch (xpp.getAttributeValue(null, "type")) {
                                            case "song":
                                                pullXMLIntoSongSlideGroupObject(xpp, setSlideGroupObject);
                                                break;
                                            case "custom":
                                            case "scripture":
                                            case "image":
                                                setSlideGroupObject.setType(xpp.getAttributeValue(null, "type"));
                                                pullXMLIntoCustomSlideGroupObject(xpp, setSlideGroupObject);
                                                break;
                                        }
                                        // Add this item to the setObject
                                        if (setObject.getSlideGroups()==null) {
                                            setObject.setSlideGroups(new ArrayList<>());
                                        }
                                        setObject.getSlideGroups().add(setSlideGroupObject);
                                        break;
                                }
                            }
                        }

                        try {
                            xpp.next();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (xpp.getName()!=null) {
                            if (xpp.getName().equals("set")) {
                                set_finished = true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (setObject.getLastModified()==null || setObject.getLastModified().isEmpty()) {
            // First try to get the file metadata
            setObject.setLastModified(mainActivityInterface.getTimeTools().getIsoTimeFromFileMetadata("Sets","",setFilename));
        }
        return setObject;
    }
    public void createSetXMLFromSetObject(SetObject setObject, boolean updateLastModified) {
        // Now we build the xml!!
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        if (setObject!=null) {
            // set name
            if (setObject.getSetName() != null) {
                stringBuilder.append("<set name=\"").append(setObject.getSetName().trim()).append("\">\n");
            } else {
                stringBuilder.append("<set>\n");
            }

            // uuid
            if (setObject.getUuid() != null) {
                stringBuilder.append("  <uuid>").append(setObject.getUuid().trim()).append("</uuid>\n");
            } else {
                stringBuilder.append("  <uuid>").append(UUID.randomUUID()).append("</uuid>\n");
            }

            // lastModified
            if (setObject.getLastModified() != null && !updateLastModified) {
                stringBuilder.append("  <lastModified>").append(setObject.getLastModified().trim()).append("</lastModified>\n");
            } else {
                stringBuilder.append("  <lastModified>").append(mainActivityInterface.getTimeTools().getNowIsoTime()).append("</lastModified>\n");
            }

            // Notes
            if (setObject.getNotes() != null) {
                stringBuilder.append("  <notes>").append(setObject.getNotes().trim()).append("</notes>\n");
            }

            // The slide_groups
            stringBuilder.append("  <slide_groups>\n");
            if (setObject.getSlideGroups() != null) {
                for (SetSlideGroupObject setSlideGroupObject:setObject.getSlideGroups()) {

                    if (setSlideGroupObject.getType()!=null && setSlideGroupObject.getName()!=null) {
                        switch (setSlideGroupObject.getType()) {
                            case "song":
                                stringBuilder.append(getSetXMLBitForSong(setSlideGroupObject));
                                break;

                            case "scripture":
                                Log.d(TAG,"creating scripture");
                                // TODO not done yet
                                break;
                        }
                    }
                }
            }

            // The end of the xml
            stringBuilder.append("  </slide_groups>\n");
            stringBuilder.append("</set>");

        }
    }
    private String getSetXMLBitForSong(SetSlideGroupObject setSlideGroupObject) {
        // This creates the slide_group tag for a song in the following format
        // <slide_group name="FILENAME" type="song" path="PATH OR /" prefKey="KEY"/>

        StringBuilder xmlBit = new StringBuilder();
        xmlBit.append("    <slide_group");
        if (setSlideGroupObject.getName()!=null) {
            xmlBit.append(" name=\"").append(setSlideGroupObject.getName().trim()).append("\"");
        }
        xmlBit.append(" type=\"song\" ");
        if (setSlideGroupObject.getPath()!=null && !setSlideGroupObject.getPath().isEmpty()) {
            xmlBit.append(" path=\"").append(setSlideGroupObject.getPath()).append("\"");
        } else {
            xmlBit.append(" path=\"/\"");
        }
        if (setSlideGroupObject.getPrefKey()!=null) {
            xmlBit.append(" prefKey=\"").append(setSlideGroupObject.getPrefKey()).append("\"");
        } else {
            xmlBit.append(" prefKey=\"\"");
        }
        xmlBit.append("/>\n");
        return xmlBit.toString();
    }
    private String getSetXMLBitForSongFromOpenChords(OpenChordsSetListItem openChordsSetListItem) {
        /* This creates the slide_group tag for a song in the following format
           <slide_group name="FILENAME" type="song" path="PATH OR /" prefKey="KEY"/>

           The OpenChord item looks like
                "id": "F4309D47-7007-484A-B2E3-A58FE90792BE",
                "type": "song",
                "songItem": {
                     "capo": 0,
                     "songId": "F4309D47-7007-484A-B2E3-A58FE90792BE"
                }  */

        // Get a temporary song
        StringBuilder xmlBit = new StringBuilder();

        Song tempSong = mainActivityInterface.getSQLiteHelper().getOpenChordsSong(
                mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName(),
                openChordsSetListItem.getId());
        if (tempSong!=null) {
            xmlBit.append("    <slide_group");
            xmlBit.append(" name=\"").append(tempSong.getFilename()).append("\"");
            xmlBit.append(" type=\"song\" ");
            if (tempSong.getFolder().equals(mainActivityInterface.getMainfoldername())) {
                xmlBit.append(" path=\"/\"");
            } else {
                xmlBit.append(" path=\"").append(tempSong.getFolder()).append("\"");
            }
            xmlBit.append(" prefKey=\"").append(tempSong.getKey()).append("\"");
            xmlBit.append("/>");
        }
        return xmlBit.toString();
    }

    // Parse setObject (OpenSong) into OpenChordsSetList object
    public OpenChordsSetListItem getOpenChordsSetListItemForSong(SetSlideGroupObject setSlideGroupObject) {
        OpenChordsSetListItem openChordsSetListItem = null;
        OpenChordsSetListSongItem openChordsSetListSongItem;
        String prefKey = setSlideGroupObject.getPrefKey();
        String folder = setSlideGroupObject.getPath();
        String filename = setSlideGroupObject.getName();
        if (folder==null || folder.isEmpty() || folder.equals("/")) {
            folder = mainActivityInterface.getMainfoldername();
        }
        if (filename!=null) {
            // Get the song object
            Song tempSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(folder, filename);
            if (tempSong != null) {
                openChordsSetListItem = new OpenChordsSetListItem();
                openChordsSetListItem.setType("song");
                openChordsSetListSongItem = new OpenChordsSetListSongItem();
                if (tempSong.getUuid() == null || tempSong.getUuid().isEmpty()) {
                    tempSong.setUuid(String.valueOf(UUID.randomUUID()));
                }
                openChordsSetListSongItem.setSongId(tempSong.getUuid());
                if (prefKey!=null && !prefKey.isEmpty()) {
                    openChordsSetListSongItem.setTranspose(prefKey);
                }
                openChordsSetListItem.setId(tempSong.getUuid());
                openChordsSetListItem.setSongItem(openChordsSetListSongItem);
                if (tempSong.getLastModified() == null || tempSong.getLastModified().isEmpty()) {
                    tempSong.setLastModified(mainActivityInterface.getTimeTools().getIsoTimeFromSongFileMetadata(tempSong));
                }
                openChordsSetListItem.setLastUpdated(tempSong.getLastModified());
            }
        }
        return openChordsSetListItem;
    }
    public OpenChordsSetListItem getOpenChordsSetListItemForDivider(SetSlideGroupObject setSlideGroupObject) {
        /*   This is how it should look
                   "id": "249C5E5E-4E2E-498F-BC3A-32323D027D30",
                   "type": "divider",
                   "title": "Divider title"  */
        Log.d(TAG,"this is a divider!");
        OpenChordsSetListItem openChordsSetListItem = new OpenChordsSetListItem();
        openChordsSetListItem.setId(String.valueOf(UUID.randomUUID()));
        openChordsSetListItem.setType("divider");
        String folder = setSlideGroupObject.getPath();
        String filename = setSlideGroupObject.getName();
        if (folder==null || folder.isEmpty() || folder.equals("/")) {
            folder = mainActivityInterface.getMainfoldername();
        }
        Log.d(TAG,"folder for divider:"+folder);
        Log.d(TAG,"filename for divider:"+filename);
        if (setSlideGroupObject.getName()!=null && !setSlideGroupObject.getName().isEmpty()) {
            openChordsSetListItem.setTitle(setSlideGroupObject.getName());
        } else {
            openChordsSetListItem.setTitle(c.getString(R.string.divider));
        }
        openChordsSetListItem.setLastUpdated(mainActivityInterface.getTimeTools().getNowIsoTime());
        return openChordsSetListItem;
    }
    public OpenChordsSetListItem getOpenChordsSetListItemForCustom(SetSlideGroupObject setSlideGroupObject) {
        OpenChordsSetListItem openChordsSetListItem = null;

        if (setSlideGroupObject.getType()!=null) {
            openChordsSetListItem = new OpenChordsSetListItem();
            // We treat all of my custom slides as OpenChords slides with the format
            /*{
                "id": "94060C61-AC44-4109-B0FE-80229F563392",
                    "type": "slide",
                    "notes": "Detail",
                    "title": "Event"
            }*/
            openChordsSetListItem.setId(String.valueOf(UUID.randomUUID()));
            openChordsSetListItem.setType("slide");
            if (setSlideGroupObject.getName()!=null && !setSlideGroupObject.getTitle().isEmpty()) {
                openChordsSetListItem.setTitle(setSlideGroupObject.getName());
            }

            // This is where we need to get the correct content
            if (setSlideGroupObject.getType()!=null) {
                switch (setSlideGroupObject.getType()) {
                    case "variation":
                        // Song variations have the following useful info
                        // subtitle=author, notes=base64encoded song
                        String author = "";
                        if (setSlideGroupObject.getSubtitle()!=null) {
                            author = mainActivityInterface.getProcessSong().parseHTML(setSlideGroupObject.getSubtitle());
                        }
                        String lyrics = author;
                        if (setSlideGroupObject.getNotes()!=null) {
                            byte[] decodedString = Base64.decode(setSlideGroupObject.getNotes(), Base64.DEFAULT);
                            String extracted = "";
                            try {
                                extracted = new String(decodedString, StandardCharsets.UTF_8);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (extracted.contains("<lyrics>") || extracted.contains("</lyrics>")) {
                                int start = extracted.indexOf("<lyrics>")+8;
                                int end = extracted.indexOf("</lyrics>");
                                if (start<end) {
                                    Song tempSong = new Song();
                                    tempSong.setLyrics(extracted.substring(start,end));
                                    lyrics += "\n\n" + mainActivityInterface.getConvertJustChords().getJustChordsLyrics(tempSong);
                                }
                            }
                        }
                        openChordsSetListItem.setNotes(getTrimmedOrNullText(lyrics));
                        openChordsSetListItem.setCustomData(setSlideGroupObject.getNotes());
                        break;

                        case "image":
                            // Images (in Song format) have the following useful info
                            // image = base64 image - will ignore for now
                            // description = image descriptioon
                            // filename = image filename
                            if (setSlideGroupObject.getSlides()!=null) {
                                StringBuilder notes = new StringBuilder();
                                for (SetSlideObject setSlideObject : setSlideGroupObject.getSlides()) {
                                    if (setSlideObject.getFilename() != null) {
                                        notes.append(c.getString(R.string.image)).append(": ").
                                                append(setSlideObject.getFilename()).append("\n");
                                    }
                                    if (setSlideObject.getDescription() != null) {
                                        notes.append(c.getString(R.string.info_text)).append(": ").
                                                append(setSlideObject.getDescription()).append("\n");
                                    }
                                }
                                if (!notes.toString().isEmpty()) {
                                    openChordsSetListItem.setNotes(getTrimmedOrNullText(notes.toString()));
                                }
                            }
                        break;

                    case "scripture":
                    case "custom":
                    default:
                        if (setSlideGroupObject.getSlides()!=null) {
                            StringBuilder notes = new StringBuilder();
                            for (SetSlideObject setSlideObject : setSlideGroupObject.getSlides()) {
                                if (setSlideObject.getBody() != null) {
                                    notes.append(setSlideObject.getBody()).append("\n\n");
                                }
                            }
                            if (!notes.toString().isEmpty()) {
                                openChordsSetListItem.setNotes(getTrimmedOrNullText(notes.toString()));
                            }
                        }
                        break;
                }
            }
        }
        return openChordsSetListItem;
    }




    private OpenChordsSetListItem getOpenChordsEventItem(SetSlideGroupObject setSlideGroupObject) {
        /* An event with detail is a custom slide that needs the following info
            "id": "94060C61-AC44-4109-B0FE-80229F563392",
            "type": "slide",
            "notes": "Detail",
            "title": "Event"    */

        OpenChordsSetListItem openChordsSetListItem = new OpenChordsSetListItem();
        // OpenSong doens't use uuid for custom set items
        openChordsSetListItem.setId(String.valueOf((UUID.randomUUID())));
        if (setSlideGroupObject.getName() != null && !setSlideGroupObject.getName().isEmpty()) {
            openChordsSetListItem.setTitle(setSlideGroupObject.getName());
        }
        openChordsSetListItem.setType("slide");
        StringBuilder notes = new StringBuilder();
        if (setSlideGroupObject.getSlides() != null) {
            for (SetSlideObject setSlideObject : setSlideGroupObject.getSlides()) {
                if (setSlideObject.getBody() != null) {
                    notes.append(setSlideObject.getBody()).append("\n");
                }
            }
            openChordsSetListItem.setNotes(getTrimmedOrNullText(notes.toString()));
        }
        return openChordsSetListItem;
    }
    private OpenChordsSetListSongItem getOpenChordsSetListSongItem(SetSlideGroupObject setSlideGroupObject) {

            return null;
    }
    private String getXMLBitForScripture(SetSlideGroupObject setSlideGroupObject) {
        // This creates the slide_group tag for a scripture in the following format
        /*
            <slide_group name="SCRIPTURE_REF" type="scripture">
              <title>SCRIPTURE_REF</title>
              <subtitle>TRANSLATION</subtitle>
              <notes>NOTES</notes>
              <slides>
                <slide>
                  <body>SLIDE 1 CONTENT</body>
                </slide>
                <slide>
                  <body>SLIDE 2 CONTENT</body>
                </slide>
                .....
              </slides>
            </slide_group>
        */

        String xmlBit = "" +
                "    <slide_group " + "name=\"" + setSlideGroupObject.getName().trim() + "\" type=\"scripture\">\n" +
                "      <title>" + setSlideGroupObject.getTitle() + "</title>\n" +
                "      <subtitle>" + setSlideGroupObject.getSubtitle() + "</subtitle>\n" +
                "      <notes>" + setSlideGroupObject.getNotes() + "</notes>\n";

        return xmlBit;
    }
    private void pullXMLIntoSongSlideGroupObject(XmlPullParser xpp, SetSlideGroupObject setSlideGroupObject) {
        String path = stripSlashes(mainActivityInterface.getProcessSong().
                parseHTML(xpp.getAttributeValue(null,"path")));
        String name = stripSlashes(mainActivityInterface.getProcessSong().
                parseHTML(xpp.getAttributeValue(null,"name")));
        String key = null;
        if (xpp.getAttributeCount()>2) {
            // Assume a key has been set as well
            key = xpp.getAttributeValue(null, "prefKey");
        }

        if (path.isEmpty()) {
            path = "/";
        }

        setSlideGroupObject.setName(name);
        setSlideGroupObject.setPath(path);
        setSlideGroupObject.setPrefKey(key);
        setSlideGroupObject.setType("song");
    }
    private void pullXMLIntoCustomSlideGroupObject(XmlPullParser xpp, SetSlideGroupObject setSlideGroupObject) throws XmlPullParserException, IOException {
        // Could be a scripture, a note, a variation, a divider, image slides, or standard custom slides
        // Notes have # Note # - in the name
        // Variations have # Variation # - in the name
        // Dividers have # Divider # - in the name

        setSlideGroupObject.setName(xpp.getAttributeValue(null, "name"));
        setSlideGroupObject.setType(xpp.getAttributeValue(null, "type"));
        setSlideGroupObject.setSeconds(xpp.getAttributeValue(null, "seconds"));
        setSlideGroupObject.setLoop(xpp.getAttributeValue(null, "loop"));
        setSlideGroupObject.setPrefKey(xpp.getAttributeValue(null, "prefKey"));
        setSlideGroupObject.setPrint(xpp.getAttributeValue(null,"print"));

        // Now we go through the fields, stopping when we reach the slide_group end tag
        boolean end_of_slide_group = false;
        while (!end_of_slide_group) {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                switch (xpp.getName()) {
                    case "title":
                        setSlideGroupObject.setTitle(getTrimmedOrNullText(xpp.nextText()));
                        break;

                    case "notes":
                        setSlideGroupObject.setNotes(getTrimmedOrNullText(xpp.nextText()));
                        break;

                    case "slide":
                        // This is a new SetSlideObject (nested inside <slides>...</slides>)
                        SetSlideObject setSlideObject = new SetSlideObject();
                        pullXMLIntoSlideObject(xpp, setSlideObject);
                        if (setSlideGroupObject.getSlides()==null) {
                            setSlideGroupObject.setSlides(new ArrayList<>());
                        }
                        if (setSlideGroupObject.getType()!=null &&
                                setSlideGroupObject.getType().equals("scripture")) {
                            setSlideGroupObject.getSlides().addAll(fixCustomSlideScripture(setSlideObject.getBody()));
                        } else {
                            setSlideGroupObject.getSlides().add(setSlideObject);
                        }
                        break;

                    case "subtitle":
                        setSlideGroupObject.setSubtitle(getTrimmedOrNullText(xpp.nextText()));
                        break;

                }
            }
            xpp.next();
            if (xpp.getName()!=null && xpp.getName().equals("slide_group")) {
                end_of_slide_group = true;
            }
        }

        // Deal with the different OpenSongApp variations of custom slides
        String noteStringOpt1 = "# " + c.getResources().getString(R.string.note) + " # - ";
        String noteStringOpt2 = "# Note # - ";
        String variationStringOpt1 = "# " + c.getResources().getString(R.string.variation) + " # - ";
        String variationStringOpt2 = "# Variation # - ";
        String dividerStringOpt1 = "# " + c.getResources().getString(R.string.divider) + " # - ";
        String dividerStringOpt2 = "# Divider # - ";

        if (setSlideGroupObject.getName()!=null &&
                (setSlideGroupObject.getName().contains(noteStringOpt1) ||
                        setSlideGroupObject.getName().contains(noteStringOpt2))) {
            String newText = setSlideGroupObject.getName().replace(noteStringOpt1,"");
            newText = newText.replace(noteStringOpt2,"");
            setSlideGroupObject.setName(newText);
            setSlideGroupObject.setType("note");

        } else if (setSlideGroupObject.getName()!=null &&
                (setSlideGroupObject.getName().contains(variationStringOpt1) ||
                        setSlideGroupObject.getName().contains(variationStringOpt2))) {
            String newText = setSlideGroupObject.getName().replace(variationStringOpt1,"");
            newText = newText.replace(variationStringOpt2,"");
            setSlideGroupObject.setName(newText);
            setSlideGroupObject.setType("variation");

        } else if (setSlideGroupObject.getName()!=null &&
                (setSlideGroupObject.getName().contains(dividerStringOpt1) ||
                        setSlideGroupObject.getName().contains(dividerStringOpt2))) {
            String newText = setSlideGroupObject.getName().replace(dividerStringOpt1,"");
            newText = newText.replace(dividerStringOpt2,"");
            setSlideGroupObject.setName(newText);
            setSlideGroupObject.setType("divider");
        }

    }
    private void pullXMLIntoSlideObject(XmlPullParser xpp, SetSlideObject setSlideObject) throws XmlPullParserException, IOException {
        boolean slide_finished = false;
        xpp.nextTag();
        while (!slide_finished) {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                switch (xpp.getName()) {
                    case "body":
                        // This is a new SetSlideObject
                        setSlideObject.setBody(mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp)));
                        break;

                    case "image":
                        // This is a base64 encoded image
                        setSlideObject.setImage(safeNextText(xpp));
                        break;

                    case "description":
                        // This is a description (for images)
                        setSlideObject.setDescription(safeNextText(xpp));
                        break;

                    case "filename":
                        // This is an image filename
                        setSlideObject.setFilename(safeNextText(xpp));
                        break;
                }
            }
            xpp.nextTag();
            if (xpp.getName()!=null && xpp.getName().equals("slide")) {
                slide_finished = true;
            }
        }
    }
    private ArrayList<SetSlideObject> fixCustomSlideScripture(String text) {
        // This setSlideGroupObject will have one slide with one body.
        // We might need to break this into multiple slides/body
        ArrayList<SetSlideObject> slides = new ArrayList<>();
        text = "\n[]\n" + text;

        // Break the text into separate word chunks so we can check for new slided
        text = text.replace(" ", "\n");
        text = text.replace("---","[]");
        StringBuilder scripture_text = new StringBuilder();
        scripture_text.append(text);
        ArrayList<String> vlines = getVlines(scripture_text);

        // Ok go back through the array and add the non-empty lines back up
        scripture_text = new StringBuilder();
        for (int i = 0; i < vlines.size(); i++) {
            String s = vlines.get(i);
            if (s != null && !s.isEmpty()) {
                s = s.replace("[]","___NEWSLIDE___");
                s = s.replace("[ ]","___NEWSLIDE___");
                scripture_text.append("\n").append(s);
            }
        }

        // Check for lots of blank lines
        while (scripture_text.toString().contains("\\n\\n")) {
            scripture_text = new StringBuilder(scripture_text.toString().replace("\\n\\n", "\\n"));
        }

        // Now split the text into the sections
        String[] sections = scripture_text.toString().split("___NEWSLIDE___");
        for (String section:sections) {
            if (section!=null && !section.trim().isEmpty()) {
                SetSlideObject newSlide = new SetSlideObject();
                newSlide.setBody(section.trim());
                slides.add(newSlide);
            }
        }

        return slides;
    }
    private String getTrimmedOrNullText(String text) {
        if (text!=null) {
            text = text.trim();
        }
        return text;
    }
}