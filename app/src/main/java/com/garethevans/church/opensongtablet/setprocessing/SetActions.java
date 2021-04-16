package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
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
    public void setStringToSet(Context c, MainActivityInterface mainActivityInterface) {
        ArrayList<String> parsedSetArray = new ArrayList<>();
        if (mainActivityInterface.getCurrentSet().getCurrentSetString()!=null) {
            String currentSetString = mainActivityInterface.getCurrentSet().getCurrentSetString();

            // Remove blank entries
            currentSetString = removeBlankEntries(currentSetString);
            mainActivityInterface.getCurrentSet().setCurrentSetString(currentSetString);

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
        mainActivityInterface.getCurrentSet().setCurrentSet(parsedSetArray);
    }
    public void setToSetString(Context c, MainActivityInterface mainActivityInterface) {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> arraySet = mainActivityInterface.getCurrentSet().getCurrentSet();
        if (arraySet!=null) {
            for (String item:arraySet) {
                // Add the start and end
                stringBuilder.append(start).append(replaceMainFolder(c,item)).append(end);
            }
        }
        String currentSetString = stringBuilder.toString();
        currentSetString = removeBlankEntries(currentSetString);
        mainActivityInterface.getCurrentSet().setCurrentSetString(currentSetString);
    }
    public String setSongForSetWork(Context c, Song thisSong) {
        if (thisSong.getFolder().equals(c.getString(R.string.mainfoldername)) ||
                thisSong.getFolder().equals("MAIN") ||
                thisSong.getFolder().equals("")) {
            songForSetWork = start + thisSong.getFilename() + end;
        } else {
            songForSetWork = start + thisSong.getFolder() +
                    "/" + thisSong.getFilename() + end;
        }
        songForSetWork = start + thisSong.getFilename() + end;
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
    public void indexSongInSet(MainActivityInterface mainActivityInterface) {
        // TODO decide if we should find the first or last entry
        int current = mainActivityInterface.getCurrentSet().getCurrentSet().indexOf(mainActivityInterface.getSong().getFolderNamePair());
        int prev = 0;
        int next = current;
        if (current>1) {
            prev = current-1;
        }
        if (current<mainActivityInterface.getCurrentSet().getCurrentSet().size()-1) {
            next = current+1;
        }
        mainActivityInterface.getCurrentSet().setIndexSongInSet(current);
        mainActivityInterface.getCurrentSet().setPreviousSongInSet(mainActivityInterface.getCurrentSet().getCurrentSet().get(prev));
        mainActivityInterface.getCurrentSet().setNextSongInSet(mainActivityInterface.getCurrentSet().getCurrentSet().get(next));
    }
    public void prepareSetList(Context c, MainActivityInterface mainActivityInterface) {
        String tempSetString = mainActivityInterface.getPreferences().getMyPreferenceString(c,"setCurrent","");
        tempSetString = removeBlankEntries(tempSetString);
        mainActivityInterface.getCurrentSet().setCurrentSetString(tempSetString);
        setStringToSet(c,mainActivityInterface);
        updateSetItems(c,mainActivityInterface);
        indexSongInSet(mainActivityInterface);
    }
    public void shuffleSet(Context c, MainActivityInterface mainActivityInterface) {
        Collections.shuffle(mainActivityInterface.getCurrentSet().getCurrentSet());
        updateSetItems(c,mainActivityInterface);
    }
    public String getSetItem_Folder(Context c, MainActivityInterface mainActivityInterface, int setIndex) {
        // Check the array is correct and not null
        checkArraysMatch(c,mainActivityInterface);
        if (mainActivityInterface.getCurrentSet().getCurrentSet_Folder().size()>setIndex) {
            return mainActivityInterface.getCurrentSet().getCurrentSet_Folder().get(setIndex);
        } else {
            return c.getString(R.string.mainfoldername);
        }
    }
    public String getSetItem_Filename(Context c, MainActivityInterface mainActivityInterface, int setIndex) {
        // Check the array is correct and not null
        checkArraysMatch(c,mainActivityInterface);
        if ( mainActivityInterface.getCurrentSet().getCurrentSet_Filename().size()>setIndex) {
            return  mainActivityInterface.getCurrentSet().getCurrentSet_Filename().get(setIndex);
        } else {
            return "Welcome to OpenSongApp";
        }
    }
    public void checkArraysMatch(Context c, MainActivityInterface mainActivityInterface) {
        if ( mainActivityInterface.getCurrentSet().getCurrentSet()!=null &&
                mainActivityInterface.getCurrentSet().getCurrentSet_Folder()!=null &&
                mainActivityInterface.getCurrentSet().getCurrentSet_Filename()!=null &&
                mainActivityInterface.getCurrentSet().getCurrentSet_Key()!=null) {
            if ( mainActivityInterface.getCurrentSet().getCurrentSet().size() !=
                    mainActivityInterface.getCurrentSet().getCurrentSet_Folder().size() ||
                    mainActivityInterface.getCurrentSet().getCurrentSet().size() !=
                            mainActivityInterface.getCurrentSet().getCurrentSet_Filename().size()) {
                updateSetItems(c,mainActivityInterface);
            }
        } else {
            resetCurrentSet(mainActivityInterface);
        }
    }
    public void updateSetItems(Context c, MainActivityInterface mainActivityInterface) {
        // Split the set into folder/songs
        ArrayList<String> currentSet_Folder = new ArrayList<>();
        ArrayList<String> currentSet_Filename = new ArrayList<>();
        ArrayList<String> currentSet_Key = new ArrayList<>();

        if (mainActivityInterface.getCurrentSet().getCurrentSet()!=null && mainActivityInterface.getCurrentSet().getCurrentSet().size()>0) {
            for (String item : mainActivityInterface.getCurrentSet().getCurrentSet()) {
                if (!item.contains("/")) {
                    item = "/"+item;
                }
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
                currentSet_Key.add(mainActivityInterface.getLoadSong().grabNextSongInSetKey(c,mainActivityInterface,folder,filename));
            }
        }
        mainActivityInterface.getCurrentSet().setCurrentSet_Folder(currentSet_Folder);
        mainActivityInterface.getCurrentSet().setCurrentSet_Filename(currentSet_Filename);
        mainActivityInterface.getCurrentSet().setCurrentSet_Key(currentSet_Key);
    }
    public void addToSet(Context c, MainActivityInterface mainActivityInterface) {
        setSongForSetWork(c,mainActivityInterface.getSong());
        mainActivityInterface.getCurrentSet().addToCurrentSetString(songForSetWork);
        mainActivityInterface.getCurrentSet().addToCurrentSet(mainActivityInterface.getSong().getSongid());
        mainActivityInterface.getCurrentSet().addToCurrentSet_Folder(mainActivityInterface.getSong().getFolder());
        mainActivityInterface.getCurrentSet().addToCurrentSet_Filename(mainActivityInterface.getSong().getFilename());
        mainActivityInterface.getCurrentSet().addToCurrentSet_Key(mainActivityInterface.getSong().getKey());
        mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrent", mainActivityInterface.getCurrentSet().getCurrentSetString());
    }

    public void removeFromSet(Context c, MainActivityInterface mainActivityInterface, int mypos) {
        setSongForSetWork(c,mainActivityInterface.getSong());
        int pos =  mainActivityInterface.getCurrentSet().removeFromCurrentSet(mypos, mainActivityInterface.getSong().getSongid());
        setToSetString(c, mainActivityInterface);
        mainActivityInterface.getCurrentSet().removeFromCurrentSet_Folder(pos);
        mainActivityInterface.getCurrentSet().removeFromCurrentSet_Filename(pos);
        mainActivityInterface.getCurrentSet().removeFromCurrentSet_Key(pos);
        mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrent", mainActivityInterface.getCurrentSet().getCurrentSetString());
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

    public void resetCurrentSet(MainActivityInterface mainActivityInterface) {
        mainActivityInterface.getCurrentSet().setCurrentSetString("");
        mainActivityInterface.getCurrentSet().setCurrentSet(new ArrayList<>());
        mainActivityInterface.getCurrentSet().setCurrentSet_Folder(new ArrayList<>());
        mainActivityInterface.getCurrentSet().setCurrentSet_Filename(new ArrayList<>());
        mainActivityInterface.getCurrentSet().setIndexSongInSet(-1);
        mainActivityInterface.getCurrentSet().setNextSongInSet("");
        mainActivityInterface.getCurrentSet().setPreviousSongInSet("");
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

    public void makeVariation(Context c, MainActivityInterface mainActivityInterface) {

        // Original file
        Uri uriOriginal = mainActivityInterface.getStorageAccess().getUriForItem(c,
                mainActivityInterface.getPreferences(), "Songs",
                mainActivityInterface.getSong().getFolder(),
                mainActivityInterface.getSong().getFilename());

        // Copy the file into the variations folder
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, uriOriginal);

        // If the file already exists, add _ to the filename
        StringBuilder newsongname = new StringBuilder(mainActivityInterface.getSong().getFilename());
        Uri uriVariation = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface.getPreferences(), "Variations", "",
                newsongname.toString());
        while (mainActivityInterface.getStorageAccess().uriExists(c,uriVariation)) {
            newsongname.append("_");
            uriVariation = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface.getPreferences(), "Variations", "",
                    newsongname.toString());
        }

        // Check the uri exists for the outputstream to be valid
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c, mainActivityInterface.getPreferences(), uriVariation, null,
                "Variations", "", newsongname.toString());

        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, uriVariation);
        mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);

        // Fix the song name and folder for loading
        mainActivityInterface.getSong().setFilename(newsongname.toString());
        mainActivityInterface.getSong().setFolder("../Variations");
        songForSetWork = start + "**" + c.getString(R.string.variation) + "/" +
                mainActivityInterface.getSong().getFilename() + end;

        // Replace the set item with the variation
        mainActivityInterface.getCurrentSet().getCurrentSet().
                set(mainActivityInterface.getCurrentSet().getIndexSongInSet(),songForSetWork);
        setToSetString(c, mainActivityInterface);
        updateSetItems(c,mainActivityInterface);

        mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrent",
                mainActivityInterface.getCurrentSet().getCurrentSetString());

        mainActivityInterface.getShowToast().doIt(c, c.getResources().getString(R.string.variation_edit));

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