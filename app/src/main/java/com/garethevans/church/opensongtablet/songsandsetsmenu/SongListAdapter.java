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

public class SongListAdapter extends RecyclerView.Adapter<SongItemViewHolder> implements FastScrollRecyclerView.SectionedAdapter  {

    private final List<Song> songList;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final SparseBooleanArray checkedArray = new SparseBooleanArray();

    AdapterCallback callback;

    public interface AdapterCallback{
        void onItemClicked(int position,String folder,String filename);
        void onItemLongClicked(int position,String folder, String filename);
    }

    public SongListAdapter(Context c, MainActivityInterface mainActivityInterface, List<Song> songList,
                           AdapterCallback callback) {
        this.songList = songList;
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        this.callback = callback;
        if (songList!=null) {
            initialiseCheckedArray(mainActivityInterface.getCurrentSet());
        }
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
        if (author.isEmpty()) {
            songItemViewHolder.itemAuthor.setVisibility(View.GONE);
        } else {
            songItemViewHolder.itemAuthor.setText(author);
        }

        // Set the key if it exists
        if (!key.isEmpty()) {
            key = c.getString(R.string.key) + ": " + key;
            songItemViewHolder.itemKey.setText(key);
            songItemViewHolder.itemKey.setVisibility(View.VISIBLE);
        } else {
            songItemViewHolder.itemKey.setVisibility(View.GONE);
        }

        // Set the author if it exists
        if (!author.isEmpty()) {
            songItemViewHolder.itemAuthor.setText(author);
            songItemViewHolder.itemAuthor.setVisibility(View.VISIBLE);
        } else {
            songItemViewHolder.itemAuthor.setVisibility(View.GONE);
        }

        // Set the checkbox if the song is in the set
        bindCheckBox(songItemViewHolder.itemChecked,i);

        // Set the listener
        final String mfilename = filename;
        songItemViewHolder.itemCard.setOnClickListener(v -> {
            song.setFilename(mfilename);
            song.setFolder(songfolder);
            mainActivityInterface.getSetActions().getSongForSetWork(c,song);
            if (callback!=null) {
                callback.onItemClicked(i,songfolder,mfilename);
            }
        });

        // For Chromebooks (need to be running Marshmallow or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            songItemViewHolder.itemCard.setOnContextClickListener(v -> {
                song.setFilename(mfilename);
                song.setFolder(songfolder);
                mainActivityInterface.getSetActions().getSongForSetWork(c,song);
                if (callback!=null) {
                    callback.onItemLongClicked(i,songfolder,mfilename);
                }
                return true;
            });
        }

        songItemViewHolder.itemCard.setOnLongClickListener(v -> {
            song.setFilename(mfilename);
            song.setFolder(songfolder);
            mainActivityInterface.getSetActions().getSongForSetWork(c,song);
            if (callback!=null) {
                callback.onItemLongClicked(i,songfolder,mfilename);
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
                String setentry = mainActivityInterface.getSetActions().getSongForSetWork(dr,fl,ky);

                Log.d("SongListAdapter","setentry="+setentry);
                mainActivityInterface.getCurrentSet().addToCurrentSetString(setentry);
                mainActivityInterface.getCurrentSet().addSetItem(setentry);
                mainActivityInterface.getCurrentSet().addSetValues(dr,fl,ky);
                checkedArray.put(adapterPosition, true);
                mainActivityInterface.updateFragment("set_updateView",null,null);

            }
            else  {
                songItemViewHolder.itemChecked.setChecked(false);
                checkedArray.put(adapterPosition, false);
                //mainActivityInterface.getSetActions().removeFromSet(c,mainActivityInterface,-1);
            }
            mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrent",mainActivityInterface.getCurrentSet().getCurrentSetString());
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
                    index = songlist.get(i).getFilename().substring(0, 1).toUpperCase(mainActivityInterface.getLocale());
                }

                if (linkedHashMap.get(index) == null) {
                    linkedHashMap.put(index, i);
                }
            }
        }
        return linkedHashMap;
    }
}
