package com.garethevans.church.opensongtablet.core.format;

import java.io.IOException;

public interface Formatter<T> {

    /**
     * @param object  the object to format.
     * @param buffer  the {@link Appendable} (e.g. {@link StringBuilder}) where to
     *                {@link Appendable#append(CharSequence) append} the formatted text.
     * @param options the {@link SongFormatOptions}.
     * @throws IOException on IO error.
     */
    void format(T object, Appendable buffer, SongFormatOptions options) throws IOException;

}
