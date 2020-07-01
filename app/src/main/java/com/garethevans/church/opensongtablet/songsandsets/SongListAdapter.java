package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.Preferences;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.SQLite;
import com.garethevans.church.opensongtablet.StaticVariables;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongItemViewHolder> implements FastScrollRecyclerView.SectionedAdapter  {

    private final List<SQLite> songList;
    private final Context c;
    private final Preferences preferences;

    AdapterCallback callback;

    public interface AdapterCallback{
        void onItemClicked(int position);
        void onItemLongClicked(int position);
    }

    public SongListAdapter(Context context, List<SQLite> songList, Preferences preferences, AdapterCallback callback) {
        this.songList = songList;
        c = context;
        this.preferences = preferences;
        this.callback = callback;
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
        SQLite song = songList.get(i);
        String filename = song.getFilename();
        //String title = song.getTitle();
        String folder = song.getFolder();
        String author = song.getAuthor();
        String key = song.getKey();
        String isinset = song.getInSet();
        boolean inset = true;
        if (isinset==null || isinset.equals("false")) {
            inset = false;
        }

        if (folder==null) {folder="";}
        if (author==null) {author="";}
        if (filename==null) {filename="";}
        if (key==null) {key="";}

        final String songfolder = folder;
        //songItemViewHolder.itemTitle.setText(filename);
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
            songItemViewHolder.itemKey.setVisibility(View.GONE);
        }

        // Set the author if it exists
        if (!author.isEmpty()) {
            songItemViewHolder.itemAuthor.setText(author);
            songItemViewHolder.itemAuthor.setVisibility(View.VISIBLE);
        } else {
            songItemViewHolder.itemAuthor.setVisibility(View.INVISIBLE);
        }

        // Set the checkbox if the song is in the set
        songItemViewHolder.itemChecked.setChecked(inset);

        String songforset;
        if (songfolder.equals(c.getString(R.string.mainfoldername)) || songfolder.equals("MAIN") || songfolder.equals("")) {
            songforset = "$**_" + filename + "_**$";
        } else {
            songforset = "$**_" + songfolder + "/" + filename + "_**$";
        }

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
        songItemViewHolder.itemCard.setOnLongClickListener(v -> {
            StaticVariables.songfilename = mfilename;
            StaticVariables.whichSongFolder = songfolder;
            StaticVariables.whatsongforsetwork = songforset;
            if (callback!=null) {
                callback.onItemClicked(i);
                callback.onItemLongClicked(i);
            }
            return true;
        });
        songItemViewHolder.itemChecked.setOnCheckedChangeListener((buttonView, isChecked) -> {
            StaticVariables.whatsongforsetwork = songforset;
            if (isChecked) {
                StaticVariables.currentSet = StaticVariables.currentSet + songforset;
                song.setInSet("true");
            } else {
                StaticVariables.currentSet = StaticVariables.currentSet.replace(songforset,"");
                song.setInSet("false");
            }
            preferences.setMyPreferenceString(c,"setCurrent",StaticVariables.currentSet);
        });
    }

    @NonNull
    @Override
    public SongItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.song_menu_itemrow, viewGroup, false);

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


    static class SongItemViewHolder extends RecyclerView.ViewHolder {

        final TextView itemTitle;
        final TextView itemFolder;
        final TextView itemAuthor;
        final TextView itemKey;
        final CheckBox itemChecked;
        final CardView itemCard;

        SongItemViewHolder(View v) {
            super(v);
            itemCard = v.findViewById(R.id.card_view);
            itemTitle = v.findViewById(R.id.cardview_title);
            itemFolder = v.findViewById(R.id.cardview_folder);
            itemAuthor = v.findViewById(R.id.cardview_author);
            itemKey = v.findViewById(R.id.cardview_key);
            itemChecked = v.findViewById(R.id.cardview_setcheck);
        }
    }

    Map<String,Integer> getAlphaIndex(List<SQLite> songlist) {
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

