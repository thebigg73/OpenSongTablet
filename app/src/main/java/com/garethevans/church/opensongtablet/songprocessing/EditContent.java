package com.garethevans.church.opensongtablet.songprocessing;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class EditContent {

    private Song editedSong;

    public Song getEditedSong() {return editedSong;}
    public void setEditedSong(Song editedSong) {this.editedSong = editedSong;}

    public boolean songChanged(MainActivityInterface mainActivityInterface) {
        return editedSong == mainActivityInterface.getSong();
    }

}