package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SongListAdapter extends RecyclerView.Adapter<SongItemViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private final List<Song> songList;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final SparseBooleanArray checkedArray = new SparseBooleanArray();
    private final boolean showChecked;

    AdapterCallback callback;

    public interface AdapterCallback {
        void onItemClicked(int position, String folder, String filename);

        void onItemLongClicked(int position, String folder, String filename);
    }

    public SongListAdapter(Context c, MainActivityInterface mainActivityInterface, List<Song> songList,
                           AdapterCallback callback) {
        this.songList = songList;
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        this.callback = callback;
        if (songList != null) {
            initialiseCheckedArray(mainActivityInterface.getCurrentSet());
        }
        this.showChecked = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean(c, "songMenuSetTicksShow", true);
    }

    private void initialiseCheckedArray(CurrentSet currentSet) {
        for (int i = 0; i < songList.size(); i++) {
            if (currentSet.getSetItems().contains(mainActivityInterface.getSetActions().
                    getSongForSetWork(c, songList.get(i)))) {
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
    public void onBindViewHolder(@NonNull SongItemViewHolder songItemViewHolder, int i) {
        Song song = songList.get(i);
        String filename = song.getFilename();
        String displayname;
        if (!song.getTitle().isEmpty()) {
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
        songItemViewHolder.itemTitle.setText(displayname);

        // Set the author if present
        if (author.isEmpty()) {
            songItemViewHolder.itemAuthor.setVisibility(View.GONE);
        } else {
            // IV - Weird issue that when rapidly moving through list author can exit GONE even though not set!
            // Seen as around 1 in 18 songs with author not showing author.  To ensure stability - set VISIBLE
            songItemViewHolder.itemAuthor.setText(author);
            songItemViewHolder.itemAuthor.setVisibility(View.VISIBLE);
        }

        // Set the path
        songItemViewHolder.itemFolderNamePair.setText(folderNamePair);

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
        songItemViewHolder.itemCard.setOnClickListener(v -> {
            song.setFilename(itemFilename);
            song.setFolder(itemFolder);
            mainActivityInterface.getSetActions().getSongForSetWork(c, song);
            if (callback != null) {
                callback.onItemClicked(i, itemFolder, itemFilename);
            }
        });

        // For Chromebooks (need to be running Marshmallow or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            songItemViewHolder.itemCard.setOnContextClickListener(v -> {
                song.setFilename(itemFilename);
                song.setFolder(itemFolder);
                mainActivityInterface.getSetActions().getSongForSetWork(c, song);
                if (callback != null) {
                    callback.onItemLongClicked(i, itemFolder, itemFilename);
                }
                return true;
            });
        }

        songItemViewHolder.itemCard.setOnLongClickListener(v -> {
            song.setFilename(itemFilename);
            song.setFolder(itemFolder);
            mainActivityInterface.getSetActions().getSongForSetWork(c, song);
            if (callback != null) {
                callback.onItemLongClicked(i, itemFolder, itemFilename);
            }
            return true;
        });

        songItemViewHolder.itemChecked.setOnClickListener(v -> {
            int adapterPosition = songItemViewHolder.getAbsoluteAdapterPosition();
            if (!checkedArray.get(adapterPosition, false)) {
                songItemViewHolder.itemChecked.setChecked(true);
                String dr = songList.get(adapterPosition).getFolder();
                String fl = songList.get(adapterPosition).getFilename();
                String ky = songList.get(adapterPosition).getKey();
                String setentry = mainActivityInterface.getSetActions().getSongForSetWork(dr, fl, ky);

                Log.d("SongListAdapter", "setentry=" + setentry);
                mainActivityInterface.getCurrentSet().addToCurrentSetString(setentry);
                mainActivityInterface.getCurrentSet().addSetItem(setentry);
                mainActivityInterface.getCurrentSet().addSetValues(dr, fl, ky);
                checkedArray.put(adapterPosition, true);
                mainActivityInterface.updateFragment("set_updateView", null, null);

            } else {
                songItemViewHolder.itemChecked.setChecked(false);
                checkedArray.put(adapterPosition, false);
                //mainActivityInterface.getSetActions().removeFromSet(c,mainActivityInterface,-1);
            }
            mainActivityInterface.getPreferences().setMyPreferenceString(c, "setCurrent", mainActivityInterface.getCurrentSet().getCurrentSetString());
        });
    }

    void bindCheckBox(CheckBox checkBox, int position) {
        // use the sparse boolean array to check
        checkBox.setChecked(checkedArray.get(position, false));
    }

    @NonNull
    @Override
    public SongItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.view_song_menu_item, viewGroup, false);
        return new SongItemViewHolder(itemView);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String item = songList.get(position).getTitle();
        if (item.length() > 0) {
            return item.substring(0, 1);
        } else {
            return "" + position;
        }

    }

    Map<String, Integer> getAlphaIndex(List<Song> songlist) {
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        if (songlist != null) {
            for (int i = 0; i < songlist.size(); i++) {
                String index = "";
                if (songlist.get(i) != null && songlist.get(i).getTitle() != null && !songlist.get(i).getTitle().equals("")) {
                    index = songlist.get(i).getTitle().substring(0, 1).toUpperCase(mainActivityInterface.getLocale());
                }

                if (linkedHashMap.get(index) == null) {
                    linkedHashMap.put(index, i);
                }
            }
        }
        return linkedHashMap;
    }
}
