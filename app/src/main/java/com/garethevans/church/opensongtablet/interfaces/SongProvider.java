package com.garethevans.church.opensongtablet.interfaces;

import com.garethevans.church.opensongtablet.importsongs.SongMatch;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.List;

public interface SongProvider {
    String getName();
    void searchSongs(String query, OnSearchResultsListener listener);
    void fetchAndParseSong(String url, Song targetSong, MainActivityInterface mainActivityInterface, OnSongParsedListener listener);

    interface OnSearchResultsListener {
        void onResultsLoaded(List<SongMatch> matches);
    }

    interface OnSongParsedListener {
        void onSongParsed(Song song);
    }
}