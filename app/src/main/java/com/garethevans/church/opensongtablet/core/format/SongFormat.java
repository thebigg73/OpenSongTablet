package com.garethevans.church.opensongtablet.core.format;

import com.garethevans.church.opensongtablet.core.music.partiture.Partiture;
import com.garethevans.church.opensongtablet.core.music.partiture.PartitureMapper;
import com.garethevans.church.opensongtablet.core.parser.Parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class SongFormat implements Parser<Partiture> {

    private static final Map<String, SongFormat> FORMAT_MAP = new HashMap<>();

    protected SongFormat() {
        super();
        FORMAT_MAP.put(getName(), this);
    }

    /**
     * @return the underlying {@link PartitureMapper} to delegate to.
     */
    protected abstract PartitureMapper getPartitureMapper();

    /**
     * @return the name of this formatSection.
     */
    public abstract String getName();

    /**
     * @param lyrics the lyrics to parse.
     * @return the {@link Partiture} parsed from the given {@code lyrics}.
     */
    @Override
    public Partiture parse(String lyrics) {
        return getPartitureMapper().parse(lyrics);
    }

    /**
     * @param partiture the {@link Partiture} to format.
     * @return the given {@link Partiture} formatted as text (lyrics) in this {@link SongFormat}.
     */
    public final String format(Partiture partiture) {
        return format(partiture, new SongFormatOptions());
    }

    /**
     * @param partiture the {@link Partiture} to format.
     * @param options the {@link SongFormatOptions}.
     * @return the given {@link Partiture} formatted as text (lyrics) in this {@link SongFormat}.
     */
    public String format(Partiture partiture, SongFormatOptions options) {
        StringBuilder buffer = new StringBuilder();
        try {
            getPartitureMapper().format(partiture, buffer, options);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return buffer.toString();
    }

    /**
     * @param name the {@link #getName() name} of the requested {@link SongFormat}.
     * @return the {@link SongFormat} with the given {@link #getName() name} or {@code null} if not exists.
     */
    public static final SongFormat get(String name) {
        return FORMAT_MAP.get(name);
    }

    @Override
    public String toString() {

        return getName();
    }
}
