package com.garethevans.church.opensongtablet.songmenu;

import androidx.lifecycle.ViewModel;

import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public class SongMenuSongs extends ViewModel {

    // This holds the required data for the song menu as a view model
    // This view model is access by the SongMenuFragment and the SongListAdapter
    // Because it is lifecycle aware and all views access the same variables
    // it should help avoid inconsistencies

    private ArrayList<Song> foundSongs;

    public void updateSongs(ArrayList<Song> foundSongs) {
        this.foundSongs = foundSongs;
    }

    public ArrayList<Song> getFoundSongs() {
        return foundSongs;
    }

    public int getCount() {
        return foundSongs.size();
    }
}
