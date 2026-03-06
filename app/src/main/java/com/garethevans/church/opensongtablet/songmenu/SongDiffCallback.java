package com.garethevans.church.opensongtablet.songmenu;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.List;

public class SongDiffCallback extends DiffUtil.Callback {

    private final List<Song> oldList;
    private final List<Song> newList;

    public SongDiffCallback(List<Song> oldList, List<Song> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Song oldSong = oldList.get(oldItemPosition);
        Song newSong = newList.get(newItemPosition);

        return oldSong.getFilename().equals(newSong.getFilename()) &&
                oldSong.getFolder().equals(newSong.getFolder());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Song oldSong = oldList.get(oldItemPosition);
        Song newSong = newList.get(newItemPosition);

        String oldAuthor = oldSong.getAuthor()==null ? "":oldSong.getAuthor();
        String newAuthor = newSong.getAuthor()==null ? "":newSong.getAuthor();
        String oldKey = oldSong.getKey()==null ? "":oldSong.getKey();
        String newKey = newSong.getKey()==null ? "":newSong.getKey();
        String oldTitle = oldSong.getTitle()==null ? "":oldSong.getTitle();
        String newTitle = newSong.getTitle()==null ? "":newSong.getTitle();

        // Check if any displayed field has changed
        return oldTitle.equals(newTitle) &&
                oldAuthor.equals(newAuthor) &&
                oldKey.equals(newKey);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Optional: If only the checkbox changed, you can return a payload here.
        // For now, returning super is fine.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}