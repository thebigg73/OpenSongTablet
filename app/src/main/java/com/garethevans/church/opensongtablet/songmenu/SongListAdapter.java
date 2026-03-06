package com.garethevans.church.opensongtablet.songmenu;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialCheckbox;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.LinkedHashMap;
import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongItemViewHolder> {

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "SongListAdapter";
    private final MainActivityInterface mainActivityInterface;
    private final boolean showChecked;
    private boolean songMenuSortTitles;
    private float titleSize, subtitleSizeAuthor, subtitleSizeFile;
    private final SongMenuSongs songMenuSongs;

    AdapterCallback callback;

    public SongListAdapter(Context c, AdapterCallback callback, SongMenuSongs songMenuSongs) {
        mainActivityInterface = (MainActivityInterface) c;
        this.callback = callback;
        this.showChecked = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean("songMenuSetTicksShow", true);
        this.songMenuSongs = songMenuSongs;
        getUpdatedPreferences();
    }

    // If we change load in a profile, this is called
    public void getUpdatedPreferences() {
        songMenuSortTitles = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles", true);
        // Make the title text the same as the alphaIndex size
        titleSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuItemSize",14f);
        subtitleSizeAuthor = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSizeAuthor",12f);
        subtitleSizeFile = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSizeFile",12f);
    }


    public interface AdapterCallback {
        void onItemClicked(int position, String folder, String filename, String key, boolean inSet);
        void onItemLongClicked(int position, String folder, String filename, String key);
    }

    @Override
    public int getItemCount() {
        if (songMenuSongs!=null && songMenuSongs.getFoundSongs()!=null) {
            return songMenuSongs.getCount();
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
        if (songMenuSongs!=null &&
                songMenuSongs.getFoundSongs()!=null) {
            try {
                int position = songItemViewHolder.getAbsoluteAdapterPosition();
                if (position!=-1 && position < songMenuSongs.getCount()) {
                    Song song = songMenuSongs.getFoundSongs().get(position);
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
                    songItemViewHolder.itemTitle.setTextColor(mainActivityInterface.getPalette().textColor);
                    songItemViewHolder.itemTitle.setText(displayname);

                    if (subtitleSizeFile == 7) {
                        // This is the 'off' value
                        songItemViewHolder.itemFolderNamePair.setVisibility(View.GONE);
                    } else {
                        // Set the path
                        songItemViewHolder.itemFolderNamePair.setVisibility(View.VISIBLE);
                        songItemViewHolder.itemFolderNamePair.setTextSize(subtitleSizeFile);
                        songItemViewHolder.itemFolderNamePair.setTextColor(mainActivityInterface.getPalette().hintColor);
                        songItemViewHolder.itemFolderNamePair.setText(folderNamePair);
                    }

                    if (subtitleSizeAuthor == 7) {
                        // This is the 'off' value
                        songItemViewHolder.itemAuthor.setVisibility(View.GONE);
                    } else {
                        // Set the author if present
                        songItemViewHolder.itemAuthor.setTextSize(subtitleSizeAuthor);
                        songItemViewHolder.itemAuthor.setTextColor(mainActivityInterface.getPalette().hintColor);

                        if (author.isEmpty()) {
                            songItemViewHolder.itemAuthor.setVisibility(View.GONE);
                        } else {
                            // IV - Weird issue that when rapidly moving through list author can exit GONE even though not set!
                            // Seen as around 1 in 18 songs with author not showing author.  To ensure stability - set VISIBLE
                            songItemViewHolder.itemAuthor.setText(author);
                            songItemViewHolder.itemAuthor.setTextColor(mainActivityInterface.getPalette().hintColor);
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
                    String itemFilename = filename;
                    final String itemTitle = title;
                    String itemFolder = folder;
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
                            if (mainActivityInterface.getCurrentSet().getIndexSongInSet()>-1) {
                                // Use the variation if required
                                SetItemInfo setItemInfo = mainActivityInterface.getCurrentSet().getSetItemInfo(mainActivityInterface.getCurrentSet().getIndexSongInSet());
                                song.setFilename(setItemInfo.songfilename);
                                song.setFolder(setItemInfo.songfolder);
                                song.setKey(setItemInfo.songkey);
                                if (callback != null) {
                                    callback.onItemClicked(position, setItemInfo.songfolder, setItemInfo.songfilename, setItemInfo.songkey, true);
                                }
                            } else {
                                if (callback != null) {
                                    callback.onItemClicked(position, itemFolder, itemFilename, itemKey, false);
                                }
                            }
                        }
                        mainActivityInterface.notifySetFragment("highlight",-1);
                        song.setFilename(itemFilename);
                        song.setFolder(itemFolder);
                        song.setKey(itemKey);
                        if (callback != null) {
                            callback.onItemClicked(position, itemFolder, itemFilename, itemKey,false);
                        }
                    });


                    // For Chromebooks (need to be running Marshmallow or higher
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        songItemViewHolder.itemCard.setOnContextClickListener(v -> {
                            song.setFilename(itemFilename);
                            song.setFolder(itemFolder);
                            song.setKey(itemKey);

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
                        boolean isChecked = songItemViewHolder.itemChecked.isChecked();
                        if (mainActivityInterface.getSetActions().isSongInSet(finalFolderNamePair)) {
                            // This was in the set, so remove it
                            songItemViewHolder.itemChecked.setChecked(false);
                            for (int x = 0; x < mainActivityInterface.getCurrentSet().getCurrentSetSize(); x++) {
                                String setItemString = mainActivityInterface.getSetActions().
                                        getSongForSetWork(mainActivityInterface.getCurrentSet().getSetItemInfo(x));
                                String setItemStringLessWithoutKey = setItemString.substring(0,
                                        setItemString.indexOf(mainActivityInterface.getVariations().getKeyStart())) +
                                        mainActivityInterface.getVariations().getKeyStart() +
                                        mainActivityInterface.getVariations().getKeyEnd() +
                                        mainActivityInterface.getSetActions().getItemEnd();
                                if (setItemString.equals(setentry) ||
                                        setItemString.equals(setentryalt1) ||
                                        setItemString.equals(setentryalt2) ||
                                        setItemStringLessWithoutKey.equals(setentryalt2)) {
                                        // Because we clicked on a song item, we need to manually check the set index
                                    int positionInSet = mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getCurrentSet().getSetItemInfo(x));
                                    // Notify the set menu fragment which removes the entry and updates the set and inline adapters
                                    int prevSize = mainActivityInterface.getCurrentSet().getCurrentSetSize();
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
                            mainActivityInterface.getCurrentSet().addItemToSet(itemFolder, itemFilename, itemTitle, itemKey, true);

                            // Notify the set menu fragment which updates the adapters
                            mainActivityInterface.notifySetFragment("setItemInserted",-1);

                            // Save the current set
                            mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());

                            if (firstItem) {
                                // Notify the inline set to appear if required
                                mainActivityInterface.updateInlineSetVisibility();
                            }
                        }

                        // If we are already viewing this item, we need to notify that we are currently in the set
                        // This involves indexing and then updating the toolbar
                        // Because the above actions are asynchronous, delay the process below by 250ms to ensure the set has saved
                        mainActivityInterface.getMainHandler().postDelayed(() -> {
                            if (mainActivityInterface.getSong().getFolder().equals(itemFolder) &&
                                    mainActivityInterface.getSong().getFilename().equals(itemFilename)) {
                                // Index this song in the set - it is the last item
                                if (isChecked) {
                                    int i = mainActivityInterface.getCurrentSet().getCurrentSetSize() - 1;
                                    mainActivityInterface.getCurrentSet().setIndexSongInSet(i);
                                } else {
                                    mainActivityInterface.getCurrentSet().setIndexSongInSet(-1);
                                }

                                mainActivityInterface.updateSetList();

                                // Updating the toolbar with null updates the set tick as it checks the song
                                mainActivityInterface.updateToolbar(null);

                                // Rebuild the prev/next
                                mainActivityInterface.getDisplayPrevNext().setPrevNext();
                            }
                        },250);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void bindCheckBox(MyMaterialCheckbox checkBox, String folderNamePair) {
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
        if (songMenuSongs!=null && songMenuSongs.getFoundSongs()!=null &&
                songMenuSongs.getCount()>0) {
            try {
                for (int x = 0; x < songMenuSongs.getCount(); x++) {
                    if (songMenuSongs.getFoundSongs().get(x).getFilename().equals(song.getFilename()) &&
                            songMenuSongs.getFoundSongs().get(x).getFolder().equals(song.getFolder())) {
                        return x;
                    }
                }
            } catch (Exception e) {
                Log.d(TAG,"Position of song in the menu couldn't be checked just now");
                // Might happen if the menu changes mid-check
            }
        }
        // Not found;
        return -1;
    }

    public void changeCheckBox(int pos) {
        if (songMenuSongs!=null &&
                songMenuSongs.getFoundSongs()!=null &&
                songMenuSongs.getCount()>pos) {
            // Get the current value and change it
            mainActivityInterface.getMainHandler().post(() -> notifyItemChanged(pos,"checkOn"));
        }
    }

    public void updateSongMenuSortTitles(boolean songMenuSortTitles) {
        this.songMenuSortTitles = songMenuSortTitles;
    }

    public void addAllSongsToSet() {
        // This is called when the user clicks on the 'Set' text in the song menu
        if (songMenuSongs!=null && getItemCount()>0) {
            for (int i=0; i<getItemCount(); i++) {
                Song song = songMenuSongs.getFoundSongs().get(i);

                // Only proceed if this song isn't already in the set
                if (!mainActivityInterface.getSetActions().isSongInSet(song)) {
                    mainActivityInterface.getCurrentSet().addItemToSet(song);
                    notifyItemChanged(i, "checkOn");
                }
            }
        }
    }

    public LinkedHashMap<String, Integer> getAlphaIndex(List<Song> songs) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        if (songs == null) return map;

        for (int i = 0; i < songs.size(); i++) {
            String title = songs.get(i).getTitle();
            if (title == null || title.isEmpty()) continue;
            title = title.toUpperCase();

            // Add "B"
            String char1 = title.substring(0, 1);
            if (!map.containsKey(char1)) map.put(char1, i);

            // Add "Ba"
            if (title.length() >= 2) {
                String char2 = title.substring(0, 2);
                if (!map.containsKey(char2)) map.put(char2, i);
            }
        }
        return map;
    }

}
