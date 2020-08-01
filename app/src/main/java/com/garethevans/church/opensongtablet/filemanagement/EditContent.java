package com.garethevans.church.opensongtablet.filemanagement;

import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;

class EditContent {
    static String filename, title, folder, key, artist, lyrics;

    // Initialise the song edit with the saved values
    EditContent(boolean initialise){
        if (initialise) {
            filename = StaticVariables.songfilename;
            title = StaticVariables.mTitle;
            folder = StaticVariables.whichSongFolder;
            key = StaticVariables.mKey;
            artist = StaticVariables.mAuthor;
            lyrics = StaticVariables.mLyrics;
        }
    }

    EditContent() {}

    String getFilename() {return filename;}
    String getTitle() {return title;}
    String getFolder() {return folder;}
    String getKey() {return key;}
    String getArtist() {return artist;}
    String getLyrics() {return lyrics;}

    void setFilename(String s) {filename = s;}
    void setTitle(String s) {title = s;}
    void setFolder(String s) {folder = s;}
    void setKey(String s) {key = s;}
    void setArtist(String s) {artist = s;}
    void setLyrics(String s) {lyrics = s;}

    public boolean areThereChanges() {
        // Compare the originals with the current values.
        return !(StaticVariables.songfilename.equals(filename) && StaticVariables.mTitle.equals(title) &&
                StaticVariables.whichSongFolder.equals(folder) && StaticVariables.mKey.equals(key) &&
                StaticVariables.mAuthor.equals(artist) && StaticVariables.mLyrics.equals(lyrics));
    }

    public void updateStatics(ProcessSong processSong) {
        StaticVariables.mTitle = processSong.parseToHTMLEntities(title);
        StaticVariables.songfilename = processSong.parseToHTMLEntities(filename);
        StaticVariables.whichSongFolder = processSong.parseToHTMLEntities(folder);
        StaticVariables.mKey = processSong.parseToHTMLEntities(key);
        StaticVariables.mAuthor = processSong.parseToHTMLEntities(artist);
        StaticVariables.mLyrics = processSong.parseToHTMLEntities(lyrics);
    }

}
