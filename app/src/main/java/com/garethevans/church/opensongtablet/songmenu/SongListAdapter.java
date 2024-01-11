package com.garethevans.church.opensongtablet.songmenu;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SongListAdapter extends RecyclerView.Adapter<SongItemViewHolder> {

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "SongListAdapter";
    private final MainActivityInterface mainActivityInterface;
    private final boolean showChecked;
    private boolean songMenuSortTitles;
    private final float titleSize;
    private final float subtitleSizeAuthor, subtitleSizeFile;

    LinkedHashMap<String, Integer> linkedHashMap, linkedHashMap2;

    AdapterCallback callback;

    public SongListAdapter(Context c,
                           AdapterCallback callback) {
        mainActivityInterface = (MainActivityInterface) c;
        this.callback = callback;
        this.showChecked = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean("songMenuSetTicksShow", true);

        songMenuSortTitles = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles", true);
        // Make the title text the same as the alphaIndex size
        titleSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuItemSize",14f);
        subtitleSizeAuthor = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSizeAuthor",12f);
        subtitleSizeFile = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSizeFile",12f);
    }

    public interface AdapterCallback {
        void onItemClicked(int position, String folder, String filename, String key);
        void onItemLongClicked(int position, String folder, String filename, String key);
    }

    @Override
    public int getItemCount() {
        if (mainActivityInterface.getSongMenuFragment()!=null &&
                mainActivityInterface.getSongMenuFragment().getSongsFound() != null) {
            return mainActivityInterface.getSongMenuFragment().getSongsFound().size();
        } else {
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SongItemViewHolder holder, int position, @NonNull List<Object> payloads) {
        position = holder.getAbsoluteAdapterPosition();
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
            for (Object payload : payloads) {
                boolean changeCheck = false;
                boolean checked;
                if (payload.equals("checkOn")) {
                    changeCheck = true;
                    checked = true;
                } else if (payload.equals("checkOff")) {
                    changeCheck = true;
                    checked = false;
                } else {
                    checked = false;
                }

                if (changeCheck) {
                    // We want to update the checkbox
                    holder.itemChecked.post(()->{
                        try {
                            // Is this item in the set?
                            holder.itemChecked.setChecked(checked);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SongItemViewHolder songItemViewHolder, int z) {
        if (mainActivityInterface.getSongMenuFragment()!=null &&
                mainActivityInterface.getSongMenuFragment().getSongsFound()!=null) {
            try {
                int position = songItemViewHolder.getAbsoluteAdapterPosition();
                if (position!=-1 && position < mainActivityInterface.getSongMenuFragment().getSongsFound().size()) {
                    Song song = mainActivityInterface.getSongMenuFragment().getSongsFound().get(position);
                    String filename = song.getFilename();
                    String title = song.getTitle();
                    String displayname;
                    if (!song.getTitle().isEmpty() && songMenuSortTitles) {
                        displayname = song.getTitle();
                    } else {
                        displayname = song.getFilename();
                    }
                    String folder = song.getFolder();
                    String author = song.getAuthor();
                    String key = song.getKey();
                    String folderNamePair = song.getFolderNamePair();
                    if (song.getFolderNamePair()==null || song.getFolderNamePair().isEmpty()) {
                        folderNamePair = folder + "/" +filename;
                    }

                    if (folder == null) {
                        folder = "";
                    }
                    if (author == null) {
                        author = "";
                    }
                    if (filename == null) {
                        filename = "";
                    }
                    if (displayname == null) {
                        displayname = "";
                    }
                    if (key == null) {
                        key = "";
                    }

                    // Add the key if it exists
                    if (!key.isEmpty()) {
                        displayname += " (" + key + ")";
                    }

                    // Set the display name
                    songItemViewHolder.itemTitle.setTextSize(titleSize);
                    songItemViewHolder.itemTitle.setText(displayname);

                    if (subtitleSizeFile == 7) {
                        // This is the 'off' value
                        songItemViewHolder.itemFolderNamePair.setVisibility(View.GONE);
                    } else {
                        // Set the path
                        songItemViewHolder.itemFolderNamePair.setVisibility(View.VISIBLE);
                        songItemViewHolder.itemFolderNamePair.setTextSize(subtitleSizeFile);
                        songItemViewHolder.itemFolderNamePair.setText(folderNamePair);
                    }

                    if (subtitleSizeAuthor == 7) {
                        // This is the 'off' value
                        songItemViewHolder.itemAuthor.setVisibility(View.GONE);
                    } else {
                        // Set the author if present
                        songItemViewHolder.itemAuthor.setTextSize(subtitleSizeAuthor);
                        if (author.isEmpty()) {
                            songItemViewHolder.itemAuthor.setVisibility(View.GONE);
                        } else {
                            // IV - Weird issue that when rapidly moving through list author can exit GONE even though not set!
                            // Seen as around 1 in 18 songs with author not showing author.  To ensure stability - set VISIBLE
                            songItemViewHolder.itemAuthor.setText(author);
                            songItemViewHolder.itemAuthor.setVisibility(View.VISIBLE);
                        }
                    }

                    // Set the checkbox if the song is in the set
                    bindCheckBox(songItemViewHolder.itemChecked, folderNamePair);

                    if (showChecked) {
                        songItemViewHolder.itemChecked.setVisibility(View.VISIBLE);
                        songItemViewHolder.itemCheckedFrame.setVisibility(View.VISIBLE);
                    } else {
                        songItemViewHolder.itemChecked.setVisibility(View.GONE);
                        songItemViewHolder.itemCheckedFrame.setVisibility(View.GONE);
                    }

                    // Set the listeners
                    final String itemFilename = filename;
                    final String itemTitle = title;
                    final String itemFolder = folder;
                    final String itemKey = key;
                    final String setentryalt1 = mainActivityInterface.getSetActions().getSongForSetWork(itemFolder, itemFilename, null);
                    final String setentryalt2 = mainActivityInterface.getSetActions().getSongForSetWork(itemFolder, itemFilename, "");
                    final String setentry = mainActivityInterface.getSetActions().getSongForSetWork(itemFolder, itemFilename, itemKey).replace("***null***", "******");
                    songItemViewHolder.itemCard.setOnClickListener(v -> {
                        if (!songItemViewHolder.itemChecked.isChecked()) {
                            // Remove the indexSongInSet
                            mainActivityInterface.getCurrentSet().setIndexSongInSet(-1);
                        } else {
                            // Look for the song index based on the folder, filename and key of the song
                            mainActivityInterface.getSetActions().indexSongInSet(song);
                        }
                        mainActivityInterface.notifySetFragment("highlight",-1);
                        song.setFilename(itemFilename);
                        song.setFolder(itemFolder);
                        song.setKey(itemKey);
                        if (callback != null) {
                            callback.onItemClicked(position, itemFolder, itemFilename, itemKey);
                        }
                    });

                    // For Chromebooks (need to be running Marshmallow or higher
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        songItemViewHolder.itemCard.setOnContextClickListener(v -> {
                            song.setFilename(itemFilename);
                            song.setFolder(itemFolder);
                            song.setKey(itemKey);
                            // Look for the song index based on the folder, filename and key of the song
                            mainActivityInterface.getSetActions().indexSongInSet(song);

                            if (callback != null) {
                                callback.onItemLongClicked(position, itemFolder, itemFilename, itemKey);
                            }
                            return true;
                        });
                    }

                    songItemViewHolder.itemCard.setOnLongClickListener(v -> {
                        song.setFilename(itemFilename);
                        song.setFolder(itemFolder);
                        song.setKey(itemKey);
                        if (callback != null) {
                            callback.onItemLongClicked(position, itemFolder, itemFilename, itemKey);
                        }
                        return true;
                    });

                    String finalFolderNamePair = folderNamePair;
                    songItemViewHolder.itemChecked.setOnClickListener(v -> {
                        if (mainActivityInterface.getSetActions().isSongInSet(finalFolderNamePair)) {
                            // This was in the set, so remove it
                            songItemViewHolder.itemChecked.setChecked(false);
                            for (int x = 0; x < mainActivityInterface.getCurrentSet().getCurrentSetSize(); x++) {
                                String setItemString = mainActivityInterface.getSetActions().
                                        getSongForSetWork(mainActivityInterface.getCurrentSet().getSetItemInfo(x));
                                if (setItemString.equals(setentry) ||
                                        setItemString.equals(setentryalt1) ||
                                        setItemString.equals(setentryalt2)) {
                                    int positionInSet = mainActivityInterface.getSetActions().indexSongInSet(finalFolderNamePair);
                                    // Notify the set menu fragment which removes the entry and updates the set and inline adapters
                                    int prevSize = mainActivityInterface.getCurrentSet().getCurrentSetSize();
                                    Log.d(TAG,"about to notifySetFramgent that item removed:"+positionInSet);
                                    if (positionInSet>-1) {
                                        mainActivityInterface.notifySetFragment("setItemRemoved", positionInSet);
                                        // If the set is now empty, hide the inline set
                                        if (prevSize > 0 && mainActivityInterface.getCurrentSet().getCurrentSetSize() == 0) {
                                            mainActivityInterface.updateInlineSetVisibility();
                                        }
                                    }
                                }
                            }
                        } else {
                            // This wasn't in the set, so add it
                            boolean firstItem = mainActivityInterface.getCurrentSet().getCurrentSetSize()==0;
                            songItemViewHolder.itemChecked.setChecked(true);
                            mainActivityInterface.getCurrentSet().addItemToSet(itemFolder, itemFilename, itemTitle, itemKey);

                            // Notify the set menu fragment which updates the adapters
                            mainActivityInterface.notifySetFragment("setItemInserted",-1);

                            // Save the current set
                            mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());

                            if (firstItem) {
                                // Notify the inline set to appear if required
                                mainActivityInterface.updateInlineSetVisibility();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void bindCheckBox(CheckBox checkBox, String folderNamePair) {
        // Is this item in the set?
        try {
            checkBox.setChecked(mainActivityInterface.getSetActions().isSongInSet(folderNamePair));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public SongItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        try {
            View itemView = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.view_song_menu_item, viewGroup, false);
            return new SongItemViewHolder(itemView);
        } catch (Exception e) {
            e.printStackTrace();
            return new SongItemViewHolder((new View(viewGroup.getContext())));
        }
    }

    public int getPositionOfSong(Song song) {
        if (mainActivityInterface.getSongMenuFragment()!=null && mainActivityInterface.getSongMenuFragment().getSongsFound()!=null) {
            for (int x = 0; x < mainActivityInterface.getSongMenuFragment().getSongsFound().size(); x++) {
                if (mainActivityInterface.getSongMenuFragment().getSongsFound().get(x).getFilename().equals(song.getFilename()) &&
                        mainActivityInterface.getSongMenuFragment().getSongsFound().get(x).getFolder().equals(song.getFolder())) {
                    return x;
                }
            }
        }
        // Not found;
        return -1;
    }

    Map<String, Integer> getAlphaIndex(List<Song> songlist) {
        linkedHashMap = new LinkedHashMap<>();
        linkedHashMap2 = new LinkedHashMap<>();
        if (songlist != null && songlist.size()>0) {
            for (int i = 0; i < songlist.size(); i++) {
                String index = "";  // First letter
                String index2 = ""; // First 2 letters
                if (songMenuSortTitles && songlist.get(i) != null && songlist.get(i).getTitle() != null && !songlist.get(i).getTitle().isEmpty()) {
                    String title = songlist.get(i).getTitle().toUpperCase(mainActivityInterface.getLocale());
                    index = title.substring(0,1);
                    if (title.length()>1) {
                        index2 = title.substring(0,2);
                    } else {
                        index2 = index;
                    }
                } else if (songlist.get(i) != null && songlist.get(i).getFilename() != null && !songlist.get(i).getFilename().isEmpty()) {
                    String filename = songlist.get(i).getFilename().toUpperCase(mainActivityInterface.getLocale());
                    index = filename.substring(0,1);
                    if (filename.length()>1) {
                        index2 = filename.substring(0,2);
                    } else {
                        index2 = index;
                    }
                }

                if (linkedHashMap.get(index) == null) {
                    linkedHashMap.put(index, i);
                }

                if (linkedHashMap2.get(index2) == null) {
                    linkedHashMap2.put(index2, i);
                }

            }
        }
        return linkedHashMap;
    }
    Map<String, Integer> getAlphaIndex2() {
        return linkedHashMap2;
    }

    public int getPositionOfAlpha2fromAlpha(String index1) {
        // get the key set
        Set<String> keySet = linkedHashMap2.keySet();
        String[] keyArray = keySet.toArray(new String[0]);

        int pos = 0;
        for (int x=0; x<keyArray.length; x++) {
           if (keyArray[x]!=null && keyArray[x].startsWith(index1)) {
               pos = x;
               break;
           }
        }
        return pos;
    }

    public void changeCheckBox(int pos) {
        if (mainActivityInterface.getSongMenuFragment()!=null &&
                mainActivityInterface.getSongMenuFragment().getSongsFound()!=null &&
                mainActivityInterface.getSongMenuFragment().getSongsFound().size()>pos) {
            // Get the current value and change it
            mainActivityInterface.getMainHandler().post(() -> notifyItemChanged(pos,"checkOn"));
        }
    }

    public void updateSongMenuSortTitles(boolean songMenuSortTitles) {
        this.songMenuSortTitles = songMenuSortTitles;
    }
}
