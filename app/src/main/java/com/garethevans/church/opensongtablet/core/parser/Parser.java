package com.garethevans.church.opensongtablet.core.parser;

/**
 * Interface for a parser that can rebuild an object from a {@link #toString() string representation}.
 *
 * @param <T> type of the object to parse.
 */
public interface Parser<T> {

    /**
     * @param string the {@link String} containing the object to parse ({@code <T>}} in is
     *               {@link #toString() string representation}.
     * @return the parsed object or {@code null} if no valid syntax was found for the requested object.
     */
    T parse(String string);

}
