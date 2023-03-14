package com.garethevans.church.opensongtablet.tags;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TagSongListAdapter extends RecyclerView.Adapter<TagViewHolder> {

    private ArrayList<TagsInfo> songInfos = new ArrayList<>();
    private final MainActivityInterface mainActivityInterface;
    private final RecyclerView recyclerView;
    private String currentTag = null;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "TagSongListAdapter";
    private SparseBooleanArray checkedArray = new SparseBooleanArray();
    private final int highlightOn, highlightOff;
    TagSongListAdapter(Context c, RecyclerView recyclerView) {
        mainActivityInterface = (MainActivityInterface) c;
        this.recyclerView = recyclerView;
        highlightOff = c.getResources().getColor(R.color.transparent);
        highlightOn = c.getResources().getColor(R.color.colorSecondary);
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.view_tag_song, parent, false);
            return new TagViewHolder(itemView);
        } catch (Exception e) {
            e.printStackTrace();
            return new TagViewHolder((new View(parent.getContext())));
        }
    }

    // For updating the currently clicked item only
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
            for (Object payload : payloads) {
                if (payload.equals("updateTag")) {
                    // We want to update the tag text
                    String tag = songInfos.get(position).tag;
                    if (tag.isEmpty() || tag.equals(";")) {
                        holder.songInfo.setHint(null);
                    } else {
                        holder.songInfo.setHint(fixTagForDisplay(fixTagStringForSaving(tag)));
                    }
                    if (checkedArray.get(position,false)) {
                        setColor(holder.cardView,highlightOn);
                    } else {
                        setColor(holder.cardView,highlightOff);
                    }
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        TagsInfo tagInfo = songInfos.get(position);
        try {
                String filename = tagInfo.filename;
                String title = tagInfo.title;
                String folder = tagInfo.folder;
                String tags = tagInfo.tag;

                if (title.isEmpty()) {
                    title = filename;
                }

                // Set the display name
                holder.songInfo.setText(title);

                // If ths song has the current tag, highlight it
                if (checkedArray.get(position,false)) {
                    setColor(holder.cardView,highlightOn);
                } else {
                    setColor(holder.cardView,highlightOff);
                }
                if (currentTag.trim().isEmpty()) {
                    holder.cardView.setEnabled(false);
                } else {
                    holder.cardView.setEnabled(true);
                    holder.cardView.setOnClickListener(v -> {
                        boolean checked = checkedArray.get(position,false);
                        checkedArray.put(position,!checked);
                        updateSongTags(folder, filename, !checked, position);
                    });
                }

                // Set the tags if they exist
                if (tags==null || tags.isEmpty() || tags.equals(";")) {
                    holder.songInfo.setHint(null);
                } else {
                    tags = fixTagForDisplay(fixTagStringForSaving(tags));
                    holder.songInfo.setHint(tags);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (songInfos==null) {
            songInfos = new ArrayList<>();
        }
        return songInfos.size();
    }

    private void setColor(MaterialCardView cardView, int cardColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardView.setBackgroundTintList(ColorStateList.valueOf(cardColor));
        } else {
            cardView.setBackgroundColor(cardColor);
        }
    }
    public void updateSongsFound(String currentTag, boolean songListSearchByFolder, boolean songListSearchByArtist,
                                 boolean songListSearchByKey, boolean songListSearchByTag,
                                 boolean songListSearchByFilter, boolean songListSearchByTitle,
                                 String folderSearchVal, String artistSearchVal, String keySearchVal,
                                 String tagSearchVal, String filterSearchVal, String titleSearchVal) {
        notifyItemRangeRemoved(0,getItemCount());
        this.currentTag = currentTag;
        ArrayList<Song> songsFound = mainActivityInterface.getSQLiteHelper().
                getSongsByFilters(songListSearchByFolder, songListSearchByArtist,
                        songListSearchByKey, songListSearchByTag, songListSearchByFilter,
                        songListSearchByTitle, folderSearchVal, artistSearchVal, keySearchVal,
                        tagSearchVal, filterSearchVal, titleSearchVal,
                        mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true));

        // Fix the checkedArray
        checkedArray = new SparseBooleanArray();
        songInfos = new ArrayList<>();
        for (int x = 0; x< songsFound.size(); x++) {
            TagsInfo tagsInfo = new TagsInfo();
            tagsInfo.filename = songsFound.get(x).getFilename();
            tagsInfo.folder = songsFound.get(x).getFolder();
            tagsInfo.title = songsFound.get(x).getTitle();
            String thisSongTag = fixTagStringForSaving(songsFound.get(x).getTheme());
            String thisSongAltTag = fixTagStringForSaving(songsFound.get(x).getAlttheme());
            tagsInfo.tag = thisSongTag;
            tagsInfo.alttag = thisSongAltTag;

            songInfos.add(x,tagsInfo);
            thisSongTag = ";" + thisSongTag;
            thisSongAltTag = ";" + thisSongAltTag;
            if (!currentTag.isEmpty() &&
                    (thisSongTag.contains(";"+currentTag+";") ||
                            thisSongAltTag.contains(";"+currentTag+";"))) {
                checkedArray.put(x,true);
            }
        }

        // Update the list
        notifyItemRangeInserted(0,songInfos.size());
        notifyItemRangeChanged(0,songInfos.size());
    }

    public void updateSongTags(String folder, String filename, boolean isChecked, int position) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Song tempSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(folder,filename);
            String thisTheme = fixTagStringForSaving(tempSong.getTheme());
            String thisAltTheme = fixTagStringForSaving(tempSong.getAlttheme());

            if (isChecked && !thisTheme.contains(currentTag+";")) {
                // Add the current theme if it isn't already there
                thisTheme = thisTheme + currentTag + ";";
                tempSong.setTheme(thisTheme);
            } else {
                // Remove the current theme (from both theme and alttheme)
                thisTheme = thisTheme.replace(currentTag+";","");
                tempSong.setTheme(thisTheme);
                tempSong.setAlttheme((thisAltTheme.replace(currentTag+";","")));
            }
            Log.d(TAG,"after update:"+tempSong.getTitle()+"  theme="+tempSong.getTheme());

            songInfos.get(position).tag = thisTheme;
            recyclerView.post(()->{
                try {
                    notifyItemChanged(position, "updateTag");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            mainActivityInterface.getSaveSong().updateSong(tempSong,false);
        });
    }

    private String fixTagStringForSaving(String thisTag) {
        // Put at least one ; in the theme/tag
        if (thisTag==null || thisTag.isEmpty()) {
            thisTag = ";";
        }
        // Split up and trim entries and reformat
        StringBuilder stringBuilder = new StringBuilder();
        String[] bits = thisTag.split(";");
        for (String bit:bits) {
            if (bit != null && !bit.trim().isEmpty()) {
                stringBuilder.append(bit.trim()).append(";");
            }
        }
        thisTag = stringBuilder.toString();
        // Make sure we end with a ;
        if (!thisTag.endsWith(";")) {
            thisTag = thisTag + ";";
        }
        return thisTag.trim();
    }

    private String fixTagForDisplay(String thisTag) {
        thisTag = thisTag.replace(";","\n");
        return thisTag.trim();
    }
}
