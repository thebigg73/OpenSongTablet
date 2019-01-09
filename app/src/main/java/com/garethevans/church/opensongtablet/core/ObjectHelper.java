package com.garethevans.church.opensongtablet.core;

import java.util.Arrays;

/**
 * General purpose utility as alternative to {@link java.util.Objects} that would require higher android API.
 */
public class ObjectHelper {

    public static int hash(Object... objects) {
        return Arrays.hashCode(objects);
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        } else if ((o1 == null) || (o2 == null)) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }

    public static void requireNonNull(Object o, String message) {
        if (o == null) {
            throw new NullPointerException(message);
        }
    }
}
