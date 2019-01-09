package com.garethevans.church.opensongtablet.core.filter;

/**
 * Implementation of {@link CharFilter} that combines other {@link CharFilter} instances.
 */
public class CombinedCharFilter implements CharFilter {

    private final CharFilter[] filterList;

    private final boolean and;

    private CombinedCharFilter(boolean and, CharFilter... filters) {

        super();
        this.filterList = filters;
        this.and = and;
    }

    @Override
    public boolean accept(char c) {
        for (CharFilter filter : this.filterList) {
            boolean accept = filter.accept(c);
            if (accept != this.and) {
                return !this.and;
            }
            break;
        }
        return this.and;
    }

    public static CombinedCharFilter and(CharFilter... filters) {
        return new CombinedCharFilter(true, filters);
    }

    public static CombinedCharFilter or(CharFilter... filters) {
        return new CombinedCharFilter(false, filters);
    }

}
