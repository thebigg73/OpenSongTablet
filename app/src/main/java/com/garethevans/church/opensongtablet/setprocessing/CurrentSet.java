package com.garethevans.church.opensongtablet.setprocessing;

// This is the (current) set object
// All actions related to building/processing are in the SetActions class

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialTextView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class CurrentSet {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = "CurrentSet";
    private ArrayList<SetItemInfo> setItemInfos = new ArrayList<>();
    private String setCurrent = "", setCurrentBeforeEdits, setCurrentLastName;
    private final String currentSetText, notSavedText, setTitleText;
    private int indexSongInSet;
    private ImageView asteriskView;
    private MyMaterialTextView setTitleView;
    private ExtendedFloatingActionButton saveButtonView;
    private final MainActivityInterface mainActivityInterface;

    public CurrentSet(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        currentSetText = c.getResources().getString(R.string.set_current);
        notSavedText = "(" + c.getString(R.string.not_saved) + ")";
        setTitleText = c.getString(R.string.set_name) + ": ";
    }


    // NEW - Use the SetItemInfos array for the set items
    public void initialiseTheSet() {
        // Clears ALL arraylists and values
        if (setItemInfos != null) {
            setItemInfos.clear();
        } else {
            setItemInfos = new ArrayList<>();
        }

        indexSongInSet = -1;
        updateSetTitleView();
    }


    // Get the setItemInfos
    public ArrayList<SetItemInfo> getSetItemInfos() {
        return setItemInfos;
    }

    public int getCurrentSetSize() {
        if (setItemInfos == null) {
            setItemInfos = new ArrayList<>();
        }
        return setItemInfos.size();
    }

    public SetItemInfo getSetItemInfo(int position) {
        if (getCurrentSetSize() > position) {
            return setItemInfos.get(position);
        } else {
            return new SetItemInfo();
        }
    }


    // The current set (a string of each item)
    public void loadCurrentSet() {
        setCurrent = mainActivityInterface.getPreferences().getMyPreferenceString("setCurrent", "");
        mainActivityInterface.updateSetList();
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
        setCurrentBeforeEdits = mainActivityInterface.getPreferences().getMyPreferenceString("setCurrentBeforeEdits", "");
    }

    public void setSetCurrentBeforeEdits(String setCurrentBeforeEdits) {
        this.setCurrentBeforeEdits = setCurrentBeforeEdits;
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrentBeforeEdits", setCurrentBeforeEdits);
    }


    // The set name for the most recently loaded/created set
    // An empty name is a new current set that hasn't been saved
    public void loadSetCurrentLastName() {
        setCurrentLastName = mainActivityInterface.getPreferences().getMyPreferenceString("setCurrentLastName", "");
    }

    public void setSetCurrentLastName(String setCurrentLastName) {
        this.setCurrentLastName = setCurrentLastName;
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrentLastName", setCurrentLastName);
    }

    public String getSetCurrentLastName() {
        return setCurrentLastName;
    }


    // Add items to the set (various options) - all update the preferences
    public void addItemToSet(SetItemInfo setItemInfo, boolean doSave) {
        setItemInfo.songforsetwork = mainActivityInterface.getSetActions().getSongForSetWork(setItemInfo);
        setItemInfos.add(setItemInfo);

        // Update the currentSet preferences
        if (doSave) {
            updateCurrentSetPreferences(false);
        }
    }

    public void addItemToSet(Song thisSong) {
        SetItemInfo setItemInfo = new SetItemInfo();
        setItemInfo.songfilename = thisSong.getFilename();
        setItemInfo.songfolder = thisSong.getFolder();
        setItemInfo.songfoldernice = thisSong.getFolder();
        setItemInfo.songkey = thisSong.getKey();
        setItemInfo.songtitle = thisSong.getTitle();
        setItemInfo.songforsetwork = mainActivityInterface.getSetActions().getSongForSetWork(setItemInfo);
        setItemInfos.add(setItemInfo);

        // Update the currentSet preferences
        updateCurrentSetPreferences(false);
    }

    public void addItemToSet(String folder, String filename, String title, String key) {
        SetItemInfo setItemInfo = new SetItemInfo();
        setItemInfo.songfilename = filename;
        setItemInfo.songfolder = folder;
        setItemInfo.songfoldernice = folder;
        setItemInfo.songtitle = title;
        setItemInfo.songkey = key;
        setItemInfo.songforsetwork = mainActivityInterface.getSetActions().getSongForSetWork(setItemInfo);
        setItemInfo.songitem = (getCurrentSetSize()+1);
        setItemInfos.add(setItemInfo);

        // Update the currentSet preferences
        updateCurrentSetPreferences(false);
    }


    // Remove an item from the set
    public void removeFromCurrentSet(int pos, String item) {
        if (pos == -1) {
            // Don't know, so look for it
            for (int x = 0; x < getCurrentSetSize(); x++) {
                SetItemInfo setItemInfo = setItemInfos.get(x);
                if (setItemInfo.songforsetwork != null && setItemInfo.songforsetwork.equals(item)) {
                    pos = x;
                    break;
                }
            }
        }

        if (pos != -1 && getCurrentSetSize() > pos) {
            setItemInfos.remove(pos);
        }


        // Update the currentSet preferences
        updateCurrentSetPreferences(false);
    }


    // Update an entry
    public void setSetItemInfo(int position, SetItemInfo setItemInfo) {
        if (getCurrentSetSize() > position) {
            // Check the new set entry string
            setItemInfo.songforsetwork = mainActivityInterface.getSetActions().getSongForSetWork(setItemInfo);
            setItemInfos.set(position, setItemInfo);

            // Update the currentSet preferences
            updateCurrentSetPreferences(false);
        }
    }


    // Save the currentSet preference
    public void updateCurrentSetPreferences(boolean updateSetMenu) {
        setCurrent = mainActivityInterface.getSetActions().getSetAsPreferenceString();
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrent", setCurrent);
        updateSetTitleView();

        // Refresh the set list (try as may not be initialised yet!)
        if (updateSetMenu) {
            try {
                mainActivityInterface.updateSetList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    // The currently selected song in the set
    public void setIndexSongInSet(int indexSongInSet) {
        this.indexSongInSet = indexSongInSet;
    }

    public int getIndexSongInSet() {
        return indexSongInSet;
    }


    // Update the title in the set menu (add asterisk for changes)
    public void initialiseSetTitleViews(ImageView asteriskView,
                                        MyMaterialTextView setTitleView,
                                        ExtendedFloatingActionButton saveButtonView) {
        this.asteriskView = asteriskView;
        this.setTitleView = setTitleView;
        this.saveButtonView = saveButtonView;
        asteriskView.setPadding(0, 8, 0, 0);
    }

    public void updateSetTitleView() {
        if (setCurrent!=null && setCurrentBeforeEdits!=null && setCurrentLastName!=null) {
            String changedOrEmpty = "";
            if (asteriskView != null) {
                if (!setCurrent.equals(setCurrentBeforeEdits)) {
                    asteriskView.post(() -> asteriskView.setVisibility(View.VISIBLE));
                    changedOrEmpty += notSavedText;
                } else {
                    asteriskView.post(() -> asteriskView.setVisibility(View.GONE));
                }
            }
            if (setTitleView != null) {
                String title = "";
                // Adjust for set category
                if (setCurrentLastName.contains("__")) {
                    String[] setBits = setCurrentLastName.split("__");
                    if (setBits.length > 0) {
                        title += "(" + setBits[0] + ") ";
                    }
                    if (setBits.length > 1) {
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
            if (saveButtonView != null) {
                if (changedOrEmpty.isEmpty()) {
                    saveButtonView.post(() -> saveButtonView.setVisibility(View.GONE));
                } else {
                    saveButtonView.post(() -> saveButtonView.setVisibility(View.VISIBLE));
                }
            }
        }
    }


    // Get array of set items as song objects
    // This is called for random song where we need a array of songs to choose
    public ArrayList<Song> getSetSongObject() {
        ArrayList<Song> songsInSet = new ArrayList<>();
        for (SetItemInfo setItemInfo : setItemInfos) {
            Song song = new Song();
            song.setFilename(setItemInfo.songfilename);
            song.setTitle(setItemInfo.songtitle);
            song.setFolder(setItemInfo.songfolder);
            song.setKey(setItemInfo.songkey);
            songsInSet.add(song);
        }
        return songsInSet;
    }


    public int getMatchingSetItem(String songForSetWork) {
        int pos = -1;
        for (SetItemInfo setItemInfo:setItemInfos) {
            if (setItemInfo.songforsetwork.equals(songForSetWork)) {
                return pos;
            }
        }
        return pos;
    }
    public boolean getIsMatchingSetItem(int position, String songForSetWork) {
        if (setItemInfos!=null && getCurrentSetSize()>position) {
            return setItemInfos.get(position).songforsetwork.equals(songForSetWork);
        } else {
            return false;
        }
    }


    public void swapPositions(int fromPosition, int toPosition) {
        if (setItemInfos != null && getCurrentSetSize() > fromPosition && getCurrentSetSize() > toPosition) {
            SetItemInfo fromSetItemInfo = setItemInfos.get(fromPosition);
            SetItemInfo toSetItemInfo = setItemInfos.get(toPosition);

            // Get the values
            int from_item = fromSetItemInfo.songitem;
            String from_filename = fromSetItemInfo.songfilename;
            String from_folder = fromSetItemInfo.songfolder;
            String from_key = fromSetItemInfo.songkey;
            int to_item = toSetItemInfo.songitem;
            String to_filename = toSetItemInfo.songfilename;
            String to_folder = toSetItemInfo.songfolder;
            String to_key = toSetItemInfo.songkey;

            // Update the values to their new locations
            fromSetItemInfo.songitem = to_item;
            fromSetItemInfo.songfilename = to_filename;
            fromSetItemInfo.songfolder = to_folder;
            fromSetItemInfo.songkey = to_key;
            fromSetItemInfo.songforsetwork = mainActivityInterface.getSetActions().getSongForSetWork(fromSetItemInfo);

            toSetItemInfo.songitem = from_item;
            toSetItemInfo.songfilename = from_filename;
            toSetItemInfo.songfolder = from_folder;
            toSetItemInfo.songkey = from_key;
            toSetItemInfo.songforsetwork = mainActivityInterface.getSetActions().getSongForSetWork(toSetItemInfo);

            // Put the new values back into the setitems
            setItemInfos.set(fromPosition,toSetItemInfo);
            setItemInfos.set(toPosition,fromSetItemInfo);

            // Update the preference
            updateCurrentSetPreferences(false);
        }
    }

    public void insertIntoCurrentSet(int position, SetItemInfo setItemInfo) {
        setItemInfo.songforsetwork = mainActivityInterface.getSetActions().getSongForSetWork(setItemInfo);
        setItemInfos.add(position,setItemInfo);

        // Update the preference
        updateCurrentSetPreferences(false);
    }



}