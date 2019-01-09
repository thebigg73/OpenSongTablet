package com.garethevans.church.opensongtablet.core.parser;

/**
 * Abstract base implementation of {@link SubParser}.
 *
 * @param <T> type of the object to parse.
 */
public abstract class AbstractSubParser<T> implements SubParser<T> {

    @Override
    public T parse(String string) {
        return parse(new StringCharStream(string));
    }
}
