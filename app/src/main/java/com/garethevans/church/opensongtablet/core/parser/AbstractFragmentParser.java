package com.garethevans.church.opensongtablet.core.parser;

import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;

/**
 * Abstract base implementation of {@link FragmentParser}.
 *
 * @param <T> type of the object to parse.
 */
public abstract class AbstractFragmentParser<T> extends AbstractSubParser<T> implements FragmentParser<T> {

    @Override
    public T parse(CharStream chars) {
        return parse(chars, ListCharFilter.NONE);
    }

    @Override
    public T parse(CharStream chars, char stop) {
        return parse(chars, ListCharFilter.only(stop));
    }

    @Override
    public T parse(CharStream chars, char... stopChars) {
        return parse(chars, ListCharFilter.allOf(stopChars));
    }
}
