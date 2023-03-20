package com.garethevans.church.opensongtablet.songmenu;

import android.content.Context;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.FastScroller;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SongListAdapter extends RecyclerView.Adapter<SongItemViewHolder> implements FastScroller.SectionIndexer {

    private final List<Song> songList;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "SongListAdapter";
    private final MainActivityInterface mainActivityInterface;
    private final SparseBooleanArray checkedArray = new SparseBooleanArray();
    private final boolean showChecked;
    private boolean songMenuSortTitles;
    private final float titleSize;
    private final float subtitleSize;

    LinkedHashMap<String, Integer> linkedHashMap, linkedHashMap2;

    AdapterCallback callback;

    public SongListAdapter(Context c, List<Song> songList,
                           AdapterCallback callback) {
        this.songList = songList;
        mainActivityInterface = (MainActivityInterface) c;
        this.callback = callback;
        if (songList != null) {
            initialiseCheckedArray(mainActivityInterface.getCurrentSet());
        }
        this.showChecked = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean("songMenuSetTicksShow", true);

        songMenuSortTitles = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles", true);
        // Make the title text the same as the alphaIndex size
        titleSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuItemSize",14f);
        subtitleSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSize",12f);
    }

    @Override
    public CharSequence getSectionText(int position) {
        if (songList!=null && songList.size()>position) {
            String item = songList.get(position).getTitle();
            if (item.length() > 0) {
                return item.substring(0, 1);
            } else {
                return "" + position;
            }
        } else {
            return "" + position;
        }
    }

    public interface AdapterCallback {
        void onItemClicked(int position, String folder, String filename, String key);
        void onItemLongClicked(int position, String folder, String filename, String key);
    }

    public void initialiseCheckedArray(CurrentSet currentSet) {
        for (int i = 0; i < songList.size(); i++) {
            String filename = songList.get(i).getFilename();
            String folder = songList.get(i).getFolder();
            String key = songList.get(i).getKey();
            String item = mainActivityInterface.getSetActions().getSongForSetWork(folder,filename,key).replace("***null***","******");
            String itemalt1 = mainActivityInterface.getSetActions().getSongForSetWork(folder,filename,"");
            String itemalt2 = mainActivityInterface.getSetActions().getSongForSetWork(folder,filename,null);

            if (currentSet.getSetItems().contains(item) || currentSet.getSetItems().contains(itemalt1) ||
            currentSet.getSetItems().contains(itemalt2)) {
                checkedArray.put(i, true);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (songList != null) {
            return songList.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SongItemViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
            for (Object payload : payloads) {
                if (payload.equals("checkChange")) {
                    // We want to update the checkbox
                    holder.itemChecked.post(()->{
                        try {
                            holder.itemChecked.setChecked(checkedArray.get(position));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SongItemViewHolder songItemViewHolder, int i) {
        try {
            if (i < songList.size()) {
                Song song = songList.get(i);
                String filename = song.getFilename();
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


                if (subtitleSize==7) {
                    // This is the 'off' value
                    songItemViewHolder.itemAuthor.setVisibility(View.GONE);
                    songItemViewHolder.itemFolderNamePair.setVisibility(View.GONE);
                } else {
                    // Set the path
                    songItemViewHolder.itemFolderNamePair.setVisibility(View.VISIBLE);
                    songItemViewHolder.itemFolderNamePair.setTextSize(subtitleSize);
                    songItemViewHolder.itemFolderNamePair.setText(folderNamePair);

                    // Set the author if present
                    songItemViewHolder.itemAuthor.setTextSize(subtitleSize);
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
                bindCheckBox(songItemViewHolder.itemChecked, i);

                if (showChecked) {
                    songItemViewHolder.itemChecked.setVisibility(View.VISIBLE);
                } else {
                    songItemViewHolder.itemChecked.setVisibility(View.GONE);
                }

                // Set the listeners
                final String itemFilename = filename;
                final String itemFolder = folder;
                final String itemKey = key;
                final String setentryalt1 = mainActivityInterface.getSetActions().getSongForSetWork(itemFolder, itemFilename, null);
                final String setentryalt2 = mainActivityInterface.getSetActions().getSongForSetWork(itemFolder, itemFilename, "");
                final String setentry = mainActivityInterface.getSetActions().getSongForSetWork(itemFolder, itemFilename, itemKey).replace("***null***","******");

                songItemViewHolder.itemCard.setOnClickListener(v -> {
                    song.setFilename(itemFilename);
                    song.setFolder(itemFolder);
                    song.setKey(itemKey);
                    // Since we clicked on a song in the song list, check for it in the set
                    mainActivityInterface.getCurrentSet().setIndexSongInSet(mainActivityInterface.getSetActions().indexSongInSet(song));
                    if (callback != null) {
                        callback.onItemClicked(i, itemFolder, itemFilename, itemKey);
                    }
                });

                // For Chromebooks (need to be running Marshmallow or higher
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    songItemViewHolder.itemCard.setOnContextClickListener(v -> {
                        song.setFilename(itemFilename);
                        song.setFolder(itemFolder);
                        // Since we clicked on a song in the song list, check for it in the set
                        mainActivityInterface.getCurrentSet().setIndexSongInSet(mainActivityInterface.getSetActions().indexSongInSet(song));
                        if (callback != null) {
                            callback.onItemLongClicked(i, itemFolder, itemFilename, itemKey);
                        }
                        return true;
                    });
                }

                songItemViewHolder.itemCard.setOnLongClickListener(v -> {
                    song.setFilename(itemFilename);
                    song.setFolder(itemFolder);
                    if (callback != null) {
                        callback.onItemLongClicked(i, itemFolder, itemFilename, itemKey);
                    }
                    return true;
                });

                songItemViewHolder.itemChecked.setOnClickListener(v -> {
                    int adapterPosition = songItemViewHolder.getAbsoluteAdapterPosition();
                    if (!checkedArray.get(adapterPosition, false)) {
                        songItemViewHolder.itemChecked.setChecked(true);

                        mainActivityInterface.getCurrentSet().addToCurrentSetString(setentry);
                        mainActivityInterface.getCurrentSet().addSetItem(setentry);
                        mainActivityInterface.getCurrentSet().addSetValues(itemFolder, itemFilename, itemKey);
                        checkedArray.put(adapterPosition, true);
                        mainActivityInterface.addSetItem(mainActivityInterface.getCurrentSet().getSetItems().size() - 1);

                    } else {
                        songItemViewHolder.itemChecked.setChecked(false);
                        checkedArray.put(adapterPosition, false);
                        // Remove all entries of this song from the set
                        // Check for entries with actual, empty or null keys
                        for (int x = 0; x < mainActivityInterface.getCurrentSet().getSetItems().size(); x++) {
                            if (mainActivityInterface.getCurrentSet().getItem(x).equals(setentry) ||
                                    mainActivityInterface.getCurrentSet().getItem(x).equals(setentryalt1) ||
                                    mainActivityInterface.getCurrentSet().getItem(x).equals(setentryalt2)) {
                                mainActivityInterface.getCurrentSet().removeFromCurrentSet(x, null);
                                mainActivityInterface.removeSetItem(x);
                            }
                        }
                    }
                    mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void bindCheckBox(CheckBox checkBox, int position) {
        // use the sparse boolean array to check
        checkBox.setChecked(checkedArray.get(position, false));
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
        if (songList!=null && songList.size()>0) {
            for (int x = 0; x < songList.size(); x++) {
                if (songList.get(x).getFilename().equals(song.getFilename()) &&
                        songList.get(x).getFolder().equals(song.getFolder())) {
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
        if (songList.size()>pos) {
            // Get the current value and change it
            try {
                checkedArray.put(pos, !checkedArray.get(pos));
                notifyItemChanged(pos, "checkChange");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateSongMenuSortTitles(boolean songMenuSortTitles) {
        this.songMenuSortTitles = songMenuSortTitles;
    }
}
