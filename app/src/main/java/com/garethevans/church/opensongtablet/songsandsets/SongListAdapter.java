package com.garethevans.church.opensongtablet.songsandsets;

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
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SongListAdapter extends RecyclerView.Adapter<SongItemViewHolder> implements FastScrollRecyclerView.SectionedAdapter  {

    private final List<Song> songList;
    private final Context c;
    private final Preferences preferences;
    private final SongForSet songForSet;
    private SparseBooleanArray checkedArray = new SparseBooleanArray();

    AdapterCallback callback;

    public interface AdapterCallback{
        void onItemClicked(int position);
        void onItemLongClicked(int position);
    }

    public SongListAdapter(Context context, List<Song> songList, Preferences preferences,
                           SongForSet songForSet, AdapterCallback callback) {
        this.songList = songList;
        c = context;
        this.preferences = preferences;
        this.callback = callback;
        this.songForSet = songForSet;
        if (songList!=null) {
            initialiseCheckedArray();
        }
    }

    private void initialiseCheckedArray() {
        for (int i = 0; i < songList.size(); i++) {
            if (StaticVariables.currentSet.contains(songForSet.getSongForSet(c, songList.get(i).getFolder(), songList.get(i).getFilename()))) {
                checkedArray.put(i, true);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (songList!=null) {
            return songList.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SongItemViewHolder songItemViewHolder, int i) {
        Song song = songList.get(i);
        String filename = song.getFilename();
        String folder = song.getFolder();
        String author = song.getAuthor();
        String key = song.getKey();

        if (folder==null) {folder="";}
        if (author==null) {author="";}
        if (filename==null) {filename="";}
        if (key==null) {key="";}

        final String songfolder = folder;
        if (folder.startsWith("**")) {
            folder = folder.replace("**","");
        }
        songItemViewHolder.itemTitle.setText(filename);
        songItemViewHolder.itemFolder.setText(folder);
        songItemViewHolder.itemAuthor.setText(author);

        // Set the key if it exists
        if (!key.isEmpty()) {
            key = c.getString(R.string.edit_song_key) + ": " + key;
            songItemViewHolder.itemKey.setText(key);
            songItemViewHolder.itemKey.setVisibility(View.VISIBLE);
        } else {
            songItemViewHolder.itemKey.setVisibility(View.INVISIBLE);
        }

        // Set the author if it exists
        if (!author.isEmpty()) {
            songItemViewHolder.itemAuthor.setText(author);
            songItemViewHolder.itemAuthor.setVisibility(View.VISIBLE);
        } else {
            songItemViewHolder.itemAuthor.setVisibility(View.INVISIBLE);
        }

        // Set the checkbox if the song is in the set
        String songforset = songForSet.getSongForSet(c,songfolder,filename);
        bindCheckBox(songItemViewHolder.itemChecked,i);

        // Set the listener
        final String mfilename = filename;
        songItemViewHolder.itemCard.setOnClickListener(v -> {
            StaticVariables.songfilename = mfilename;
            StaticVariables.whichSongFolder = songfolder;
            StaticVariables.whatsongforsetwork = songforset;
            if (callback!=null) {
                callback.onItemClicked(i);
            }
        });

        // For Chromebooks (need to be running Marshmallow or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            songItemViewHolder.itemCard.setOnContextClickListener(v -> {
                StaticVariables.songfilename = mfilename;
                StaticVariables.whichSongFolder = songfolder;
                StaticVariables.whatsongforsetwork = songforset;
                if (callback!=null) {
                    callback.onItemLongClicked(i);
                }
                return true;
            });
        }

        songItemViewHolder.itemCard.setOnLongClickListener(v -> {
            StaticVariables.songfilename = mfilename;
            StaticVariables.whichSongFolder = songfolder;
            StaticVariables.whatsongforsetwork = songforset;
            if (callback!=null) {
                callback.onItemLongClicked(i);
            }
            return true;
        });

        songItemViewHolder.itemChecked.setOnClickListener(v -> {
            int adapterPosition = songItemViewHolder.getAdapterPosition();
            if (!checkedArray.get(adapterPosition, false)) {
                songItemViewHolder.itemChecked.setChecked(true);
                checkedArray.put(adapterPosition, true);
                StaticVariables.currentSet = StaticVariables.currentSet + songforset;
            }
            else  {
                songItemViewHolder.itemChecked.setChecked(false);
                checkedArray.put(adapterPosition, false);
                StaticVariables.currentSet = StaticVariables.currentSet.replace(songforset,"");
            }
            preferences.setMyPreferenceString(c,"setCurrent",StaticVariables.currentSet);
        });
    }

    void bindCheckBox(CheckBox checkBox, int position) {
        // use the sparse boolean array to check
        if (!checkedArray.get(position, false)) {
            checkBox.setChecked(false);
        } else {
            checkBox.setChecked(true);
        }
    }

    @NonNull
    @Override
    public SongItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.menu_songs_itemrow, viewGroup, false);

        return new SongItemViewHolder(itemView);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String item = songList.get(position).getFilename();
        if (item.length()>0) {
            return item.substring(0,1);
        } else {
            return ""+position;
        }

    }

    Map<String,Integer> getAlphaIndex(List<Song> songlist) {
        Map<String,Integer> linkedHashMap = new LinkedHashMap<>();
        if (songlist!=null) {
            for (int i=0; i<songlist.size(); i++) {
                String index = "";
                if (songlist.get(i)!=null && songlist.get(i).getFilename()!=null && !songlist.get(i).getFilename().equals("")) {
                    index = songlist.get(i).getFilename().substring(0, 1).toUpperCase(StaticVariables.locale);
                }

                if (linkedHashMap.get(index) == null) {
                    linkedHashMap.put(index, i);
                }
            }
        }
        return linkedHashMap;
    }
}
