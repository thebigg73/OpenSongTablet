package com.garethevans.church.opensongtablet.filemanagement;

import com.garethevans.church.opensongtablet.preferences.StaticVariables;

class EditContent {
    static String title, folder, key, artist, lyrics;

    // Initialise the song edit with the saved values
    EditContent(boolean initialise){
        if (initialise) {
            title = StaticVariables.mTitle;
            folder = StaticVariables.whichSongFolder;
            key = StaticVariables.mKey;
            artist = StaticVariables.mAuthor;
            lyrics = StaticVariables.mLyrics;
        }
    }

    EditContent() {}

    String getTitle() {return title;}
    String getFolder() {return folder;}
    String getKey() {return key;}
    String getArtist() {return artist;}
    String getLyrics() {return lyrics;}

    void setTitle(String s) {title = s;}
    void setFolder(String s) {folder = s;}
    void setKey(String s) {key = s;}
    void setArtist(String s) {artist = s;}
    void setLyrics(String s) {lyrics = s;}

    public boolean areThereChanges() {
        // Compare the originals with the current values.
        return !(StaticVariables.mTitle.equals(title) && StaticVariables.whichSongFolder.equals(folder) &&
                StaticVariables.mKey.equals(key) && StaticVariables.mAuthor.equals(artist) &&
                StaticVariables.mLyrics.equals(lyrics));
    }

    public void updateStatics() {
        StaticVariables.mTitle = title;
        StaticVariables.songfilename = title;  // THIS HAS CHANGED FROM OLDEN DAYS!!!
        StaticVariables.whichSongFolder = folder;
        StaticVariables.mKey = key;
        StaticVariables.mAuthor = artist;
        StaticVariables.mLyrics = lyrics;
    }
}
