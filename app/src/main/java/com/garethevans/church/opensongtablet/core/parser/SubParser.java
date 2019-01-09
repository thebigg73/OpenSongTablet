package com.garethevans.church.opensongtablet.core.parser;

/**
 * A {@link Parser} responsible only for a sub-object. It will only {@link #parse(String) parse}
 * a part of the string to parse until the object to parse is completed. A parent {@link Parser} may
 * afterwards continue the parsing from where the {@link SubParser} ended.
 *
 * @param <T> type of the object to parse.
 */
public interface SubParser<T> extends Parser<T> {

    /**
     * @param chars the {@link CharStream} to parse.
     * @return the parsed object or {@code null} if no valid syntax was found for the requested object.
     */
    T parse(CharStream chars);

}
