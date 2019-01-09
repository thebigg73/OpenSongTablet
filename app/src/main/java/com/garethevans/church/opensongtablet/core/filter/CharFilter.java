package com.garethevans.church.opensongtablet.core.filter;

/**
 * Interface to filter characters (e.g. from a {@link String}) by deciding which character is {@link #accept(char) accepted}.
 * @see ListCharFilter
 */
public interface CharFilter {

    /**
     * @param c the character to check.
     * @return {@code true} if the given character {@code c} is accepted, {@code false} otherwise (rejected).
     */
    boolean accept(char c);

}
