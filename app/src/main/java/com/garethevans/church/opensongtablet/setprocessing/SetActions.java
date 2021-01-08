package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class SetActions {

    private final String start = "$_**";
    private final String end = "**_$";
    private String songForSetWork;

    // The setters
    public void setStringToSet(Context c, CurrentSet currentSet) {
        ArrayList<String> parsedSetArray = new ArrayList<>();
        if (currentSet.getCurrentSetString()!=null) {
            String currentSetString = currentSet.getCurrentSetString();

            // Remove blank entries
            currentSetString = removeBlankEntries(currentSetString);
            currentSet.setCurrentSetString(currentSetString);

            // Add a delimiter and split by it
            String delimiter = "%%%";
            currentSetString = currentSetString.replace(end+start, end+ delimiter +start);
            String[] setbits = currentSetString.split(delimiter);

            // Go through each entry
            for (String item:setbits) {
                // Get rid of the start and end encapsulating strings
                // Each entry in the set should provide us with a folder and a filename.
                item = item.replace(start,"").replace(end,"");
                item = replaceMainFolder(c,item);
                parsedSetArray.add(item);
            }
        }
        currentSet.setCurrentSet(parsedSetArray);
    }
    public void setToSetString(Context c, CurrentSet currentSet) {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> arraySet = currentSet.getCurrentSet();
        if (arraySet!=null) {
            for (String item:arraySet) {
                // Add the start and end
                stringBuilder.append(start).append(replaceMainFolder(c,item)).append(end);
            }
        }
        String currentSetString = stringBuilder.toString();
        currentSetString = removeBlankEntries(currentSetString);
        currentSet.setCurrentSetString(currentSetString);
    }
    public String setSongForSetWork(Context c, String songfolder, String songname) {
        if (songfolder.equals(c.getString(R.string.mainfoldername)) || songfolder.equals("MAIN") || songfolder.equals("")) {
            songForSetWork = start + songname + end;
        } else {
            songForSetWork = start + songfolder + "/" + songname + end;
        }
        songForSetWork = start + songname + end;
        return songForSetWork;
    }

    // The getters
    public String getSongForSetWork() {
        return songForSetWork;
    }

    // Common functions
    private String removeBlankEntries(String string) {
        return string.replace(start+end, "");
    }
    private String replaceMainFolder(Context c, String string) {
        return string.replace(c.getString(R.string.mainfoldername)+"/","").
                replace("MAIN/","");
    }

    // Calculations
    public void indexSongInSet(CurrentSet currentSet, Song song) {
        // TODO decide if we should find the first or last entry
        int current = currentSet.getCurrentSet().indexOf(song.getFolderNamePair());
        int prev = 0;
        int next = current;
        if (current>1) {
            prev = current-1;
        }
        if (current<currentSet.getCurrentSet().size()-1) {
            next = current+1;
        }
        currentSet.setIndexSongInSet(current);
        currentSet.setPreviousSongInSet(currentSet.getCurrentSet().get(prev));
        currentSet.setNextSongInSet(currentSet.getCurrentSet().get(next));
    }
    public void prepareSetList(Context c, StorageAccess storageAccess, Preferences preferences,
                               ProcessSong processSong, LoadSong loadSong, CurrentSet currentSet, Song song) {
        String tempSetString = preferences.getMyPreferenceString(c,"setCurrent","");
        tempSetString = removeBlankEntries(tempSetString);
        currentSet.setCurrentSetString(tempSetString);
        setStringToSet(c,currentSet);
        updateSetItems(c,storageAccess,preferences,processSong,currentSet,loadSong);
        indexSongInSet(currentSet,song);
    }
    public void shuffleSet(Context c, StorageAccess storageAccess, Preferences preferences,
                           ProcessSong processSong, CurrentSet currentSet, LoadSong loadSong) {
        Collections.shuffle(currentSet.getCurrentSet());
        updateSetItems(c,storageAccess,preferences,processSong,currentSet,loadSong);
    }
    public String getSetItem_Folder(Context c, StorageAccess storageAccess, Preferences preferences,
                                    ProcessSong processSong, LoadSong loadSong,
                                    CurrentSet currentSet, int setIndex) {
        // Check the array is correct and not null
        checkArraysMatch(c,storageAccess,preferences,processSong,loadSong,currentSet);
        if (currentSet.getCurrentSet_Folder().size()>setIndex) {
            return currentSet.getCurrentSet_Folder().get(setIndex);
        } else {
            return c.getString(R.string.mainfoldername);
        }
    }
    public String getSetItem_Filename(Context c, StorageAccess storageAccess, Preferences preferences,
                                      ProcessSong processSong, LoadSong loadSong,
                                      CurrentSet currentSet, int setIndex) {
        // Check the array is correct and not null
        checkArraysMatch(c,storageAccess,preferences,processSong,loadSong,currentSet);
        if (currentSet.getCurrentSet_Filename().size()>setIndex) {
            return currentSet.getCurrentSet_Filename().get(setIndex);
        } else {
            return "Welcome to OpenSongApp";
        }
    }
    public void checkArraysMatch(Context c, StorageAccess storageAccess, Preferences preferences,
                                 ProcessSong processSong, LoadSong loadSong, CurrentSet currentSet) {
        if (currentSet.getCurrentSet()!=null && currentSet.getCurrentSet_Folder()!=null &&
                currentSet.getCurrentSet_Filename()!=null && currentSet.getCurrentSet_Key()!=null) {
            if (currentSet.getCurrentSet().size() != currentSet.getCurrentSet_Folder().size() ||
                    currentSet.getCurrentSet().size() != currentSet.getCurrentSet_Filename().size()) {
                updateSetItems(c,storageAccess,preferences,processSong,currentSet,loadSong);
            }
        } else {
            resetCurrentSet(currentSet);
        }
    }
    public void updateSetItems(Context c, StorageAccess storageAccess, Preferences preferences,
                               ProcessSong processSong, CurrentSet currentSet, LoadSong loadSong) {
        // Split the set into folder/songs
        ArrayList<String> currentSet_Folder = new ArrayList<>();
        ArrayList<String> currentSet_Filename = new ArrayList<>();
        ArrayList<String> currentSet_Key = new ArrayList<>();

        if (currentSet.getCurrentSet()!=null && currentSet.getCurrentSet().size()>0) {
            for (String item : currentSet.getCurrentSet()) {
                String folder = item.substring(0, item.lastIndexOf("/"));
                String filename = item.substring(item.lastIndexOf("/"));
                if (folder.startsWith("/")) {
                    folder = folder.replaceFirst("/", "");
                }
                if (folder.endsWith("/")) {
                    folder = folder.substring(0, folder.lastIndexOf("/"));
                }
                if (filename.startsWith("/")) {
                    filename = filename.replaceFirst("/", "");
                }
                if (filename.endsWith("/")) {
                    filename = filename.substring(0, filename.lastIndexOf("/"));
                }

                currentSet_Folder.add(folder);
                currentSet_Filename.add(filename);
                currentSet_Key.add(loadSong.grabNextSongInSetKey(c,preferences,storageAccess,processSong,folder,filename));
            }
        }
        currentSet.setCurrentSet_Folder(currentSet_Folder);
        currentSet.setCurrentSet_Filename(currentSet_Filename);
        currentSet.setCurrentSet_Key(currentSet_Key);
    }
    public void addToSet(Context c, Preferences preferences, CurrentSet currentSet, Song song) {
        setSongForSetWork(c,song.getFolder(),song.getFilename());
        currentSet.addToCurrentSetString(songForSetWork);
        currentSet.addToCurrentSet(song.getSongid());
        currentSet.addToCurrentSet_Folder(song.getFolder());
        currentSet.addToCurrentSet_Filename(song.getFilename());
        currentSet.addToCurrentSet_Key(song.getKey());
        preferences.setMyPreferenceString(c,"setCurrent",currentSet.getCurrentSetString());
    }

    public void removeFromSet(Context c, Preferences preferences, CurrentSet currentSet, Song song, int mypos) {
        setSongForSetWork(c,song.getFolder(),song.getFilename());
        int pos = currentSet.removeFromCurrentSet(mypos, song.getSongid());
        setToSetString(c,currentSet);
        currentSet.removeFromCurrentSet_Folder(pos);
        currentSet.removeFromCurrentSet_Filename(pos);
        currentSet.removeFromCurrentSet_Key(pos);
        preferences.setMyPreferenceString(c,"setCurrent",currentSet.getCurrentSetString());
    }

    public String currentSetNameForMenu(Context c, Preferences preferences) {
        // This decides on the set name to display as a title
        // If it is a new set (unsaved), it will be called 'current (unsaved)'
        // If it is a non-modified loaded set, it will be called 'set name'
        // If it is a modified, unsaved, loaded set, it will be called 'set name (unsaved)'

        String title;
        String lastSetName = preferences.getMyPreferenceString(c, "setCurrentLastName", "");
        if (lastSetName == null || lastSetName.equals("")) {
            title = c.getString(R.string.set_current) +
                    " (" + c.getString(R.string.not_saved) + ")";
        } else {
            title = lastSetName.replace("__", "/");
            if (!preferences.getMyPreferenceString(c, "setCurrent", "")
                    .equals(preferences.getMyPreferenceString(c, "setCurrentBeforeEdits", ""))) {
                title += " (" + c.getString(R.string.not_saved) + ")";
            }
        }
        return title;
    }

    public void resetCurrentSet(CurrentSet currentSet) {
        currentSet.setCurrentSetString("");
        currentSet.setCurrentSet(new ArrayList<>());
        currentSet.setCurrentSet_Folder(new ArrayList<>());
        currentSet.setCurrentSet_Filename(new ArrayList<>());
        currentSet.setIndexSongInSet(-1);
        currentSet.setNextSongInSet("");
        currentSet.setPreviousSongInSet("");
    }

    public String whatToLookFor(Context c, String folder, String filename) {
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

    public void makeVariation(Context c, StorageAccess storageAccess, Preferences preferences,
                              CurrentSet currentSet, ProcessSong processSong, ShowToast showToast,
                              Song song, LoadSong loadSong) {

        // Original file
        Uri uriOriginal = storageAccess.getUriForItem(c, preferences, "Songs", song.getFolder(),
                song.getFilename());

        // Copy the file into the variations folder
        InputStream inputStream = storageAccess.getInputStream(c, uriOriginal);

        // If the file already exists, add _ to the filename
        StringBuilder newsongname = new StringBuilder(song.getFilename());
        Uri uriVariation = storageAccess.getUriForItem(c, preferences, "Variations", "",
                newsongname.toString());
        while (storageAccess.uriExists(c,uriVariation)) {
            newsongname.append("_");
            uriVariation = storageAccess.getUriForItem(c, preferences, "Variations", "",
                    newsongname.toString());
        }

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(c, preferences, uriVariation, null,
                "Variations", "", newsongname.toString());

        OutputStream outputStream = storageAccess.getOutputStream(c, uriVariation);
        storageAccess.copyFile(inputStream, outputStream);

        // Fix the song name and folder for loading
        song.setFilename(newsongname.toString());
        song.setFolder("../Variations");
        songForSetWork = start + "**" + c.getString(R.string.variation) + "/" + song.getFilename() + end;

        // Replace the set item with the variation
        currentSet.getCurrentSet().set(currentSet.getIndexSongInSet(),songForSetWork);
        setToSetString(c, currentSet);
        updateSetItems(c,storageAccess,preferences,processSong,currentSet,loadSong);

        preferences.setMyPreferenceString(c,"setCurrent",currentSet.getCurrentSetString());

        showToast.doIt(c, c.getResources().getString(R.string.variation_edit));

        // Now load the new variation item up
        // TODO calling activity should do this!

    }

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