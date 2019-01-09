package com.garethevans.church.opensongtablet.core.parser;

import com.garethevans.church.opensongtablet.core.filter.CharFilter;

/**
 * A {@link SubParser} that is parsing within the bounds of a fragment that is typically terminated
 * with a separator (e.g. comma separated lists) or a stop character (e.g. ']' in "...[sub-object]...").
 *
 * @param <T> type of the object to parse.
 */
public interface FragmentParser<T> extends SubParser<T> {

    /**
     * @param chars the {@link CharStream} to parse.
     * @param stop   the character where this parser has to stop parsing.
     * @return the parsed object or {@code null} if no valid syntax was found for the requested object.
     */
    T parse(CharStream chars, char stop);

    /**
     * @param chars the {@link CharStream} to parse.
     * @param stopChars the characters where to stop parsing if any of them was reached.
     * @return the parsed object or {@code null} if no valid syntax was found for the requested object.
     */
    T parse(CharStream chars, char... stopChars);

    /**
     * @param chars the {@link CharStream} to parse.
     * @param stopFilter the {@link CharFilter} that determines which characters are
     *                   {@link CharFilter#accept(char) accepted} as characters where this parser has
     *                   to stop parsing.
     * @return the parsed object or {@code null} if no valid syntax was found for the requested object.
     */
    T parse(CharStream chars, CharFilter stopFilter);

}
