package com.garethevans.church.opensongtablet.core;

import com.garethevans.church.opensongtablet.core.filter.CharFilter;
import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;

import java.io.IOException;

/**
 * Helper class providing static methods for generic operations on {@link String} and related classes.
 */
public class StringHelper {

    private static final String[] SPACES = new String[]{"", // 0 spaces
            " ", // 1 spaces
            "  ", // 2 spaces
            "   ", // 3 spaces
            "    ", // 4 spaces
            "     ", // 5 spaces
            "      ", // 6 spaces
            "       ", // 7 spaces
            "        " // 8 spaces
    };

    /**
     * @param buffer the {@link Appendable} to fill with the given number of spaces.
     * @param count  the desired number of spaces.
     */
    public static void appendSpaces(Appendable buffer, int count) throws IOException {
        int delta = count;
        if (delta <= 0) { // already long enough?
            return;
        }
        while (delta >= 8) {
            buffer.append(SPACES[8]);
            delta = delta - 8;
        }
        buffer.append(SPACES[delta]);
    }

    /**
     * @param sb     the {@link StringBuilder} to fill with spaces until the given {@code length} is reached.
     * @param length the desired {@link StringBuilder#length() length}.
     */
    public static void appendSpacesUntilLength(StringBuilder sb, int length) {
        try {
            appendSpaces(sb, length - sb.length());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param string the {@link String} to "{@link String#trim() trim}".
     * @param length the preferred {@link String#length() length}.
     * @return the given {@link String} "{@link String#trim() trimmed}" as far as possible to reach
     * the preferred {@code length}. If the {@link String#length() length} of the regularly
     * {@link String#trim() trimmed} {@link String} is greater or equal to the given {@code length}
     * it is returned by this method.
     */
    public static String trimToLength(String string, int length) {

        if ((string == null) || string.isEmpty()) {
            return string;
        }
        int stringLength = string.length();
        int delta = stringLength - length;
        if (delta <= 0) {
            return string;
        }
        int end = stringLength - 1;
        while ((delta > 0) && (end > 0) && (string.charAt(end) == ' ')) {
            end--;
            delta--;
        }
        int start = 0;
        while ((delta > 0) && (start < end) && (string.charAt(start) == ' ')) {
            start++;
            delta--;
        }
        return string.substring(start, end + 1);
    }
}
