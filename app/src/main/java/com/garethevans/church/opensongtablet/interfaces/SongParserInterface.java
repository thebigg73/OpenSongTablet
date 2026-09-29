package com.garethevans.church.opensongtablet.interfaces;

import com.garethevans.church.opensongtablet.songprocessing.Song;

public interface SongParserInterface {
    Song processContent(MainActivityInterface mainActivityInterface, Song targetSong, String html);
}