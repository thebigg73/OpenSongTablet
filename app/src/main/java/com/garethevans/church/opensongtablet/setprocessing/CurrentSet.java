package com.garethevans.church.opensongtablet.setprocessing;

// This is the (current) set object
// All actions related to building/processing are in the SetActions class

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialTextView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class CurrentSet {

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "CurrentSet";
    private ArrayList<String> setItems = new ArrayList<>();      // The set item $**_folder/filename_***key***__**$ or $**_**{customsfolder}/filename_******__**$
    private ArrayList<String> setFolders = new ArrayList<>();    // The folder only
    private ArrayList<String> setFilenames = new ArrayList<>();  // The filename only
    private ArrayList<String> setKeys = new ArrayList<>();       // The key only
    private int indexSongInSet;
    private String setCurrent, setCurrentBeforeEdits, setCurrentLastName;
    private ImageView asteriskView;
    private MyMaterialTextView setTitleView;
    private ExtendedFloatingActionButton saveButtonView;
    private final MainActivityInterface mainActivityInterface;
    private final String currentSetText, notSavedText, setTitleText;

    public CurrentSet(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        currentSetText = c.getResources().getString(R.string.set_current);
        notSavedText = "(" + c.getString(R.string.not_saved) + ")";
        setTitleText = c.getString(R.string.set_name) + ": ";
        loadCurrentSet();
        loadSetCurrentBeforeEdits();
        loadSetCurrentLastName();
    }

    // The current set is a combination of array lists
    // They are built on app start by parsing the set string from preferences
    public void initialiseTheSet() {
        // Clears ALL arraylists and values
        if (setItems!=null) {
            setItems.clear();
            setItems = null;
        }
        setItems = new ArrayList<>();

        indexSongInSet = -1;
        initialiseTheSpecifics();
        updateSetTitleView();
    }
    public void initialiseTheSpecifics() {
        // Kept separate as when shuffling, we only call this not the initialiseTheSet()
        if (setFilenames!=null) {
            setFilenames.clear();
            setFilenames = null;
        }
        setFilenames = new ArrayList<>();
        if (setFolders!=null) {
            setFolders.clear();
            setFolders = null;
        }
        setFolders = new ArrayList<>();
        if (setKeys!=null) {
            setKeys.clear();
            setKeys = null;
        }
        setKeys = new ArrayList<>();
    }


    // The current set (a string of each item)
    public void loadCurrentSet() {
        setCurrent = mainActivityInterface.getPreferences().getMyPreferenceString("setCurrent","");
    }
    public void setSetCurrent(String setCurrent) {
        // Keep a reference
        this.setCurrent = setCurrent;

        // Save the user preference
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrent", setCurrent);
        // Check if we need to update the set menu title
        updateSetTitleView();
    }
    public String getSetCurrent() {
        return setCurrent;
    }

    // The last loaded set before any changes.  Used for comparison to signify changes
    public void loadSetCurrentBeforeEdits() {
        setCurrentBeforeEdits = mainActivityInterface.getPreferences().getMyPreferenceString("setCurrentBeforeEdits","");
    }
    public void setSetCurrentBeforeEdits(String setCurrentBeforeEdits) {
        this.setCurrentBeforeEdits = setCurrentBeforeEdits;
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrentBeforeEdits",setCurrentBeforeEdits);
    }

    // The set name for the most recently loaded/created set
    // An empty name is a new current set that hasn't been saved
    public void loadSetCurrentLastName() {
        setCurrentLastName = mainActivityInterface.getPreferences().getMyPreferenceString("setCurrentLastName","");
    }
    public void setSetCurrentLastName(String setCurrentLastName) {
        this.setCurrentLastName = setCurrentLastName;
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrentLastName",setCurrentLastName);
    }
    public String getSetCurrentLastName() {
        return setCurrentLastName;
    }

    // Add items to the set
    public void addSetItem(String item) {
        setItems.add(item);
    }
    public void addSetValues(String folder, String filename, String key) {
        setFolders.add(folder);
        setFilenames.add(filename);
        setKeys.add(key);
    }
    public void addSetValues(Song thisSong) {
        setFolders.add(thisSong.getFolder());
        setFilenames.add(thisSong.getFilename());
        setKeys.add(thisSong.getKey());
    }

    // Get items from the set
    public ArrayList<String> getSetItems() {
        return setItems;
    }
    public String getItem(int position) {
        return getValueAtPosition("item",position);
    }
    public String getFolder(int position) {
        return getValueAtPosition("folder",position);
    }
    public String getFilename(int position) {
        return getValueAtPosition("filename",position);
    }
    public String getTitle(int position) {
        // If we have a valid database and item exists, look it up, otherwise, use the filename
        String filename = getFilename(position);
        String folder = getFolder(position);
        Song tempSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(folder,filename);
        return (tempSong!=null && tempSong.getTitle()!=null) ? tempSong.getTitle() : filename;
    }

    public String getKey(int position) {
        String thisKey = getValueAtPosition("key",position);
        if (thisKey==null) {
            return "";
        } else {
            return thisKey;
        }
    }
    private String getValueAtPosition(String what, int position) {
        String value = "";
        if (position>=0 && position<setItems.size()) {
            try {
                switch (what) {
                    case "item":
                        value = setItems.get(position);
                        break;
                    case "folder":
                        value = setFolders.get(position);
                        break;
                    case "filename":
                        if (setFilenames!=null && setFilenames.size()>position) {
                            value = setFilenames.get(position);
                        }
                        break;
                    case "key":
                        value = setKeys.get(position);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    // Set or update items in the set
    public void setItem(int position, String value) {
        try {
            if (setItems!=null && setItems.size()>position) {
                setItems.set(position, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setFolder(int position, String value) {
        setFolders.set(position, value);
    }
    public void setFilename(int position, String value) {
        setFilenames.set(position, value);
    }
    public void setKey(int position, String value) {
        if (value==null) {
            value="";
        }
        setKeys.set(position, value);
    }

    // Get array of set items as song objects
    public ArrayList<Song> getSetSongObject() {
        // This is called for random song where we need a array of songs to choose
        ArrayList<Song> songsInSet = new ArrayList<>();
        for (int i=0; i<setFilenames.size(); i++) {
            Song song = new Song();
            song.setFilename(setFilenames.get(i));
            song.setTitle(setFilenames.get(i));
            song.setFolder(setFolders.get(i));
            songsInSet.add(song);
        }
        return songsInSet;
    }

    public int getIndexSongInSet() {
        return indexSongInSet;
    }
    public ArrayList<String> getSetFolders() {
        return setFolders;
    }
    public ArrayList<String> getSetFilenames() {
        return setFilenames;
    }
    public ArrayList<String> getSetKeys() {
        return setKeys;
    }

    public void swapPositions(int fromPosition, int toPosition) {
        if (setItems!=null && setItems.size()>fromPosition && setItems.size()>toPosition) {
            // Get the values
            String from_item = setItems.get(fromPosition);
            String from_filename = setFilenames.get(fromPosition);
            String from_folder = setFolders.get(fromPosition);
            String from_key = setKeys.get(fromPosition);
            String to_item = setItems.get(toPosition);
            String to_filename = setFilenames.get(toPosition);
            String to_folder = setFolders.get(toPosition);
            String to_key = setKeys.get(toPosition);

            // Update the values to their new locations
            setItems.set(fromPosition, to_item);
            setItems.set(toPosition, from_item);
            setFilenames.set(fromPosition, to_filename);
            setFilenames.set(toPosition, from_filename);
            setFolders.set(fromPosition, to_folder);
            setFolders.set(toPosition, from_folder);
            setKeys.set(fromPosition, to_key);
            setKeys.set(toPosition, from_key);
        }
    }
    public void addToCurrentSetString(String item) {
        setCurrent = setCurrent + item;
    }

    public void insertIntoCurrentSet(int position, String item, String folder, String filename, String key) {
        if (setItems.size()>position) {
            setItems.add(position, item);
            setFolders.add(position, folder);
            setFilenames.add(position, filename);
            setKeys.add(position, key);
        }
    }

    public void removeFromCurrentSet(int pos, String item) {
        if (pos==-1) {
            // Don't know, so look for it
            pos = setItems.indexOf(item);
        }

        if (pos!=-1 && setItems.size()>pos) {
            setItems.remove(pos);
            setFilenames.remove(pos);
            setFolders.remove(pos);
            setKeys.remove(pos);
        }
    }

    public void setIndexSongInSet(int indexSongInSet) {
        this.indexSongInSet = indexSongInSet;
    }

    // Update the title in the set menu (add asterisk for changes)
    public void initialiseSetTitleViews(ImageView asteriskView,
                                        MyMaterialTextView setTitleView,
                                        ExtendedFloatingActionButton saveButtonView) {
        this.asteriskView = asteriskView;
        this.setTitleView = setTitleView;
        this.saveButtonView = saveButtonView;
        asteriskView.setPadding(0,8,0,0);
    }
    public void updateSetTitleView() {
        String changedOrEmpty = "";
        if (asteriskView!=null) {
            if (!setCurrent.equals(setCurrentBeforeEdits)) {
                asteriskView.post(() -> asteriskView.setVisibility(View.VISIBLE));
                changedOrEmpty += notSavedText;
            } else {
                asteriskView.post(() -> asteriskView.setVisibility(View.GONE));
            }
        }
        if (setTitleView!=null) {
            String title = "";
            // Adjust for set category
            if (setCurrentLastName.contains("__")) {
                String[] setBits = setCurrentLastName.split("__");
                if (setBits.length>0) {
                    title += "(" + setBits[0] + ") ";
                }
                if (setBits.length>1) {
                    title += setBits[1];
                } else {
                    title = setCurrentLastName;
                }
            } else {
                title = setCurrentLastName;
            }

            String changed = changedOrEmpty;
            if (setCurrentLastName == null || setCurrentLastName.isEmpty()) {
                title = currentSetText;
            } else {
                title = setTitleText + title;
            }
            String finalTitle = title;

            setTitleView.post(() -> {
                setTitleView.setText(finalTitle);
                if (changed.isEmpty()) {
                    setTitleView.setHint(null);
                } else {
                    setTitleView.setHint(changed);
                }
            });
        }
        if (saveButtonView!=null) {
            if (changedOrEmpty.isEmpty()) {
                saveButtonView.post(() -> saveButtonView.setVisibility(View.GONE));
            } else {
                saveButtonView.post(() -> saveButtonView.setVisibility(View.VISIBLE));
            }
        }
    }

}
