package com.garethevans.church.opensongtablet.core.song;

import com.garethevans.church.opensongtablet.core.bean.Bean;
import com.garethevans.church.opensongtablet.core.format.SongFormat;
import com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey;
import com.garethevans.church.opensongtablet.core.music.partiture.Partiture;
import com.garethevans.church.opensongtablet.core.property.IntProperty;
import com.garethevans.church.opensongtablet.core.property.MusicalKeyProperty;
import com.garethevans.church.opensongtablet.core.property.SongFormatProperty;
import com.garethevans.church.opensongtablet.core.property.StringProperty;

/**
 * Representation of a song as a simple Java bean. This is a stupid data container with no logic.
 * For advanced logic see {@link SongWithContext}.
 */
public class Song extends Bean {

    /** The display title of the song (e.g. "The winner takes it all"). */
    public final StringProperty title;

    /** The author of the song. Typically used for the band (e.g. "Abba"). */
    public final StringProperty author;

    /** the additional copyright information. Typically the original artists (song composer, lyrics writer). */
    public final StringProperty copyright;

    public final StringProperty ccli;

    /**
     * The lyrics of the song potentially annotated with metadata such as chords, tabs, or partiture.
     * @see Partiture
     * @see SongWithContext#getPartiture()
     */
    public final StringProperty lyrics;

    /** The {@link SongFormat} of the {@link #lyrics}. */
    public final SongFormatProperty format;

    /** The optional {@link MusicalKey}. */
    public final MusicalKeyProperty key;

    /** The pre delay in seconds before the song starts scrolling. */
    public final IntProperty preDelay;

    /** The duration of the song in seconds for scrolling. */
    public final IntProperty duration;

    /** The fret where to place the capo (to play in original or preferred key). */
    public final IntProperty capo;

    public Song() {
        super();
        this.title = register(new StringProperty("title"));
        this.author = register(new StringProperty("author"));
        this.copyright = register(new StringProperty("copyright"));
        this.ccli = register(new StringProperty("ccli"));
        this.lyrics = register(new StringProperty("lyrics"));
        this.format = register(new SongFormatProperty("formatSection"));
        this.key = register(new MusicalKeyProperty("key"));
        this.preDelay = register(new IntProperty("preDelay"));
        this.duration = register(new IntProperty("duration"));
        this.capo = register(new IntProperty("capo"));
    }

    /**
     * @param song the {@link Song} to copy.
     */
    public Song(Song song) {
        this();
        copy(song);
    }
}
