package com.garethevans.church.opensongtablet.core.song;

import com.garethevans.church.opensongtablet.core.AppContext;
import com.garethevans.church.opensongtablet.core.format.SongFormat;
import com.garethevans.church.opensongtablet.core.format.SongFormatChordPro;
import com.garethevans.church.opensongtablet.core.music.partiture.Partiture;
import com.garethevans.church.opensongtablet.core.property.Property;
import com.garethevans.church.opensongtablet.core.property.listener.PropertyChangeListener;

public class SongWithContext extends Song {

    private final AppContext context;

    private Partiture partiture;

    public SongWithContext(AppContext context) {
        super();
        this.context = context;
        init();
    }

    public SongWithContext(AppContext context, Song song) {
        super(song);
        this.context = context;
        init();
    }

    private void init() {
        PropertyChangeListener<Object> listener = new PropertyChangeListener<Object>() {
            @Override
            public void onChange(Property<? extends Object> property, Object oldValue, Object newValue) {
                SongWithContext.this.partiture = null;
            }
        };
        this.lyrics.addListener(listener);
        this.format.addListener(listener);
    }

    public AppContext getContext() {
        return this.context;
    }

    public Partiture getPartiture() {
        if (this.partiture == null) {
            String lyrics = this.lyrics.getValue();
            if (lyrics == null) {
                return null;
            }
            SongFormat format = this.format.getValue();
            if (format == null) {
                format = SongFormatChordPro.INSTANCE;
            }
            this.partiture = format.parse(lyrics);
        }
        return this.partiture;
    }
}
