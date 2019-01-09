package com.garethevans.church.opensongtablet.core;

import com.garethevans.church.opensongtablet.core.config.AppConfig;
import com.garethevans.church.opensongtablet.core.song.Song;
import com.garethevans.church.opensongtablet.core.song.SongWithContext;

/**
 * The context of this application from where to get access to all important state data.
 */
public class AppContext {

    private static final AppContext INSTANCE = new AppContext();

    private final AppConfig appConfig;

    private SongWithContext currentSong;

    public AppContext() {
        super();
        this.appConfig = new AppConfig();
        this.currentSong = new SongWithContext(this);
        this.currentSong.author.setValue("Gareth Evans");
    }

    public static AppContext get() {

        return INSTANCE;
    }

    public AppConfig getConfig() {
        return this.appConfig;
    }

    public SongWithContext getCurrentSong() {
        return this.currentSong;
    }

    public void setCurrentSong(SongWithContext currentSong) {
        assert (currentSong.getContext() == this);
        this.currentSong = currentSong;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = new SongWithContext(this, currentSong);
    }
}
