package com.garethevans.church.opensongtablet.core.filter;

import java.util.Locale;

/**
 * An implementation of {@link CharFilter} that holds a {@link #getChars() list of characters} that
 * are {@link #isAccepting() accepted or rejected}.
 */
public class ListCharFilter implements CharFilter {

    private static final char[] NO_CHARS = new char[0];

    /** {@link CharFilter} that {@link CharFilter#accept(char) accepts} no character at all. */
    public static final ListCharFilter NONE = new ListCharFilter(NO_CHARS, true);

    /** {@link CharFilter} that {@link CharFilter#accept(char) accepts} any character. */
    public static final ListCharFilter ANY = new ListCharFilter(NO_CHARS, false);

    /** {@link CharFilter} that {@link CharFilter#accept(char) accepts} only spaces (' '). */
    public static final ListCharFilter SPACE = new ListCharFilter(new char[] {' '}, true);

    /** {@link CharFilter} that {@link CharFilter#accept(char) accepts} only newline characters ('\n' and '\r'). */
    public static final ListCharFilter NEWLINE = new ListCharFilter(new char[] {'\n', '\r'}, true);

    /** {@link CharFilter} that {@link CharFilter#accept(char) accepts} only ASCII digits ('0'-'9'). */
    public static final ListCharFilter DIGITS = new ListCharFilter('0', '9', true);

    /** {@link CharFilter} that {@link CharFilter#accept(char) accepts} only ASCII lower case letters ('a'-'z'). */
    public static final ListCharFilter LOWER_LETTERS = new ListCharFilter('a', 'z', true);

    /** {@link CharFilter} that {@link CharFilter#accept(char) accepts} only ASCII upper case letters ('A'-'Z'). */
    public static final ListCharFilter UPPER_LETTERS = new ListCharFilter('A', 'Z', true);

    /** {@link CharFilter} that {@link CharFilter#accept(char) accepts} only ASCII letters ('a'-'Z'). */
    public static final ListCharFilter LETTERS = LOWER_LETTERS.join(UPPER_LETTERS.chars);

    /** {@link CharFilter} that {@link CharFilter#accept(char) accepts} only {@link #LETTERS} and {@link #DIGITS}. */
    public static final ListCharFilter LETTERS_AND_DIGITS = LETTERS.join(DIGITS.chars);

    private final char[] chars;

    private final boolean accepting;

    /**
     * @param chars the {@link #getChars() characters}.
     * @param accepting the {@link #isAccepting() accepting flag}.
     */
    public ListCharFilter(char[] chars, boolean accepting) {
        super();
        this.chars = chars;
        this.accepting = accepting;
    }

    /**
     * @param from the first of the {@link #getChars() characters} range.
     * @param to the last of the {@link #getChars() characters} range. Has to be greater or equal to {@code from}.
     *           Be careful when providing non-ASCII characters as unicode range can consume lots of memory.
     * @param accepting the {@link #isAccepting() accepting flag}.
     */
    public ListCharFilter(char from, char to, boolean accepting) {
        this(fromTo(from, to), accepting);
    }

    /**
     * @return {@code true} if this {@link ListCharFilter} is {@link #accept(char) accepting} the {@link #getChars() listed chars},
     * {@code false} otherwise (if rejecting them).
     */
    public boolean isAccepting() {
        return this.accepting;
    }

    /**
     * @return a copy of the {@link #isAccepting() accepted or rejected} characters.
     */
    public char[] getChars() {
        return this.chars.clone();
    }

    @Override
    public boolean accept(char c) {
        for (char current : chars) {
            if (c == current) {
                return accepting;
            }
        }
        return !accepting;
    }

    /**
     * @param moreChars the additional characters to {@link #isAccepting() accepting or reject}.
     * @return a {@link ListCharFilter} {@link #isAccepting() accepting or rejecting} both this
     * {@link #getChars() characters} and the given {@code moreChars}.
     */
    public ListCharFilter join(char... moreChars) {
        char[] joined = new char[moreChars.length + this.chars.length];
        int i = 0;
        for (char c : moreChars) {
            if (accept(c) != accepting) {
                joined[i++] = c;
            }
        }
        if (i < moreChars.length) {
            char[] reduced = new char[i + this.chars.length];
            System.arraycopy(joined, 0, reduced, 0, i);
            joined = reduced;
        }
        System.arraycopy(this.chars, 0, joined, i, this.chars.length);
        return new ListCharFilter(joined, this.accepting);
    }

    /**
     * @param filter the {@link ListCharFilter} with the {@link #getChars() characters} to join.
     *               WARNING: it will be ignored if this filter is {@link #isAccepting() accepting}
     *               or not.
     * @return a {@link ListCharFilter} {@link #isAccepting() accepting or rejecting} both this
     * {@link #getChars() characters} and the characters from given {@code filter}.
     */
    public ListCharFilter joinChars(ListCharFilter filter) {
        return join(filter.chars);
    }

    public CharFilter or(CharFilter filter) {
        if (filter instanceof ListCharFilter) {
            return or((ListCharFilter) filter);
        }
        return CombinedCharFilter.or(this, filter);
    }

    public CharFilter or(ListCharFilter filter) {
        if (this.accepting == filter.accepting) {
            return join(filter.chars);
        }
        return CombinedCharFilter.or(this, filter);
    }

    /**
     * @return a new {@link ListCharFilter} with an inverted {@link #isAccepting() accepting flag}.
     */
    public ListCharFilter invert() {
        return new ListCharFilter(this.chars, !this.accepting);
    }

    /**
     * @param chars the characters to {@link CharFilter#accept(char) accept}.
     * @return {@link CharFilter} that only {@link CharFilter#accept(char) accepts} characters from the given array.
     */
    public static ListCharFilter allOf(char... chars) {
        return new ListCharFilter(chars, true);
    }

    /**
     * @param charsUpperCase the {@link Character#isUpperCase(char) upper case} characters to {@link CharFilter#accept(char) accept}.
     * @return {@link CharFilter} that only {@link CharFilter#accept(char) accepts} characters from the
     * given {@link String} including their {@link String#toLowerCase(Locale) lower case} variants.
     */
    public static ListCharFilter allOfAnyCase(String charsUpperCase) {
        char[] upper = charsUpperCase.toCharArray();
        String charsLowerCase = charsUpperCase.toLowerCase(Locale.US);
        char[] lower = charsLowerCase.toCharArray();
        int duplicateCount = 0;
        for (int i = 0; i < lower.length; i++) {
            char low = lower[i];
            for (char up : upper) {
                if (low == up) {
                    duplicateCount++;
                    lower[i] = 0;
                }
            }
        }
        char[] chars = new char[upper.length + lower.length - duplicateCount];
        System.arraycopy(upper, 0, chars, 0, upper.length);
        if (duplicateCount == 0) {
            System.arraycopy(lower, 0, chars, upper.length, lower.length);
        } else {
            int i = upper.length;
            for (int lowerIndex = 0; lowerIndex < lower.length; lowerIndex++) {
                char l = lower[lowerIndex];
                if (l != 0) {
                    chars[i++] = l;
                }
            }
            if (i < chars.length) {
                chars[i++] = 0;
            }
        }
        return new ListCharFilter(chars, true);
    }

    /**
     * @param chars the characters to reject (that are not {@link CharFilter#accept(char) accepted}).
     * @return {@link CharFilter} that {@link CharFilter#accept(char) accepts} all characters except
     * the ones from the given array.
     */
    public static ListCharFilter allExcept(char... chars) {
        return new ListCharFilter(chars, false);
    }

    /**
     * @param accept the only character to {@link CharFilter#accept(char) accept}.
     * @return {@link ListCharFilter} that only {@link CharFilter#accept(char) accepts} the given character.
     */
    public static ListCharFilter only(char accept) {
        return new ListCharFilter(new char[] {accept}, true);
    }

    private static char[] fromTo(char from, char to) {
        int delta = to - from;
        if (delta < 0) {
            throw new IllegalArgumentException("'" + from + "'-'" + to + "'");
        }
        char[] chars = new char[delta + 1];
        int i = 0;
        for (char c = from; c <= to; c++) {
            chars[i++] = c;
        }
        return chars;
    }

}
