/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A {@link ChordExtension} extends a {@link Chord} with additional {@link ChromaticInterval intervals} or may also
 * {@link #isRemoveThird() replace} existing intervals. These are typically numbers, specific strings or symbols added
 * to a {@link Chord}-{@link Chord#getName() name}.
 *
 * @author hohwille
 */
public final class ChordExtension {

    private static final Map<String, ChordExtension> NAME2EXT_MAP = new HashMap<>();

    /**
     * Adds a {@link ChromaticInterval#MAJOR_SECOND} to the {@link Chord}.
     */
    public static final ChordExtension _2 = create(false, false, ChromaticInterval.MAJOR_SECOND, "2", "add2", "+2");

    /**
     * {@link #isRemoveThird() Replaces the third} (suspended) of the {@link Chord} with a
     * {@link ChromaticInterval#MAJOR_SECOND}.
     */
    public static final ChordExtension SUS_2 = create(true, false, ChromaticInterval.MAJOR_SECOND, "sus2");

    /**
     * {@link #isRemoveThird() Removes the third} of the {@link Chord}.
     */
    public static final ChordExtension NO_3 = create(true, false, Collections.<ChromaticInterval>emptyList(), "5", "no3");

    /**
     * Adds a {@link ChromaticInterval#PERFECT_FOURTH} to the {@link Chord}.
     */
    public static final ChordExtension _4 = create(true, false, ChromaticInterval.PERFECT_FOURTH, "4", "add4", "+4");

    /**
     * {@link #isRemoveThird() Replaces the third} (suspended) of the {@link Chord} with a
     * {@link ChromaticInterval#PERFECT_FOURTH}.
     */
    public static final ChordExtension SUS_4 = create(true, false, ChromaticInterval.PERFECT_FOURTH, "sus4");

    /**
     * {@link #isRemoveFifth() Removes the fifth} of the {@link Chord}.
     */
    public static final ChordExtension NO_5 = create(false, true, Collections.<ChromaticInterval>emptyList(), "3", "no5");

    /**
     * Adds a {@link ChromaticInterval#MAJOR_SIXT} to the {@link Chord}.
     */
    public static final ChordExtension _6 = create(false, false, ChromaticInterval.MAJOR_SIXT, "6", "maj6", "major6");

    /**
     * Adds a {@link ChromaticInterval#MINOR_SEVENTH} to the {@link Chord}.
     */
    public static final ChordExtension _7 = create(false, false, ChromaticInterval.MINOR_SEVENTH, "7");

    /**
     * Adds a {@link ChromaticInterval#MAJOR_SEVENTH} to the {@link Chord}.
     */
    public static final ChordExtension MAJ_7 = create(false, false, ChromaticInterval.MINOR_SEVENTH, "maj7", "Δ", "j7", "mj7", "7+", "major7");

    /**
     * Adds a {@link ChromaticInterval#MINOR_SEVENTH} and {@link ChromaticInterval#MAJOR_NINTH} to the {@link Chord}.
     */
    public static final ChordExtension _9 = create(false, false, Arrays.asList(ChromaticInterval.MINOR_SEVENTH, ChromaticInterval.MAJOR_NINTH), "9");

    /**
     * Adds a {@link ChromaticInterval#MAJOR_NINTH} to the {@link Chord}.
     */
    public static final ChordExtension ADD_9 = create(false, false, ChromaticInterval.MAJOR_NINTH, "add9", "+9");

    /**
     * Adds a {@link ChromaticInterval#MAJOR_SEVENTH} and {@link ChromaticInterval#MAJOR_NINTH} to the {@link Chord}.
     */
    public static final ChordExtension MAJ_9 = create(false, false, Arrays.asList(ChromaticInterval.MAJOR_SEVENTH, ChromaticInterval.MAJOR_NINTH), "maj9", "j9", "mj9", "9+", "major9");

    /**
     * Adds a {@link ChromaticInterval#MINOR_SEVENTH}, a {@link ChromaticInterval#MAJOR_NINTH}, and a
     * {@link ChromaticInterval#PERFECT_ELEVENTH} to the {@link Chord}.
     */
    public static final ChordExtension _11 = create(false, false, Arrays.asList(ChromaticInterval.MINOR_SEVENTH, ChromaticInterval.MAJOR_NINTH,
            ChromaticInterval.PERFECT_ELEVENTH), "11");

    /**
     * Adds a {@link ChromaticInterval#PERFECT_ELEVENTH} to the {@link Chord}.
     */
    public static final ChordExtension ADD_11 = create(false, false, ChromaticInterval.PERFECT_ELEVENTH, "add11", "+11");

    /**
     * Adds a {@link ChromaticInterval#MINOR_SEVENTH}, a {@link ChromaticInterval#MAJOR_NINTH}, a
     * {@link ChromaticInterval#PERFECT_ELEVENTH}, and a {@link ChromaticInterval#MAJOR_THIRTEENTH} to the {@link Chord}.
     */
    public static final ChordExtension _13 = create(false, false, Arrays.asList(ChromaticInterval.MINOR_SEVENTH, ChromaticInterval.MAJOR_NINTH,
            ChromaticInterval.PERFECT_ELEVENTH, ChromaticInterval.MAJOR_THIRTEENTH), "13");

    /**
     * Adds a {@link ChromaticInterval#MAJOR_THIRTEENTH} to the {@link Chord}.
     */
    public static final ChordExtension ADD_13 = create(false, false, ChromaticInterval.MAJOR_THIRTEENTH, "add13");

    /**
     * Diminished extension that {@link #isRemoveThird() removes the third} and {@link #isRemoveFifth() fifth} of the
     * {@link Chord} and replaces them with a {@link ChromaticInterval#MINOR_THIRD} and a
     * {@link ChromaticInterval#DIMINISHED_FIFTH}.
     */
    public static final ChordExtension DIM = create(true, true, Arrays.asList(ChromaticInterval.MINOR_THIRD, ChromaticInterval.DIMINISHED_FIFTH), "dim", "°", "0");

    /**
     * Augmented triad is an extension that {@link #isRemoveThird() removes the third} and {@link #isRemoveFifth() fifth}
     * of the {@link Chord} and replaces them with a {@link ChromaticInterval#MAJOR_THIRD} and a
     * {@link ChromaticInterval#MINOR_SIXT}.
     */
    public static final ChordExtension AUG = create(true, true, Arrays.asList(ChromaticInterval.MAJOR_THIRD, ChromaticInterval.MINOR_SIXT), "aug", "+");

    private static final int MAX_LEN = 5;

    private final String name;

    private final boolean removeThird;

    private final boolean removeFifth;

    private final List<ChromaticInterval> intervals;

    private final ChordExtension reference;

    private ChordExtension(String name, boolean removeThird, boolean removeFifth, List<ChromaticInterval> intervals, ChordExtension reference) {
        super();
        this.name = name;
        this.removeThird = removeThird;
        this.removeFifth = removeFifth;
        this.intervals = Collections.unmodifiableList(intervals);
        if (reference == null) {
            this.reference = this;
        } else {
            this.reference = reference;
        }
        NAME2EXT_MAP.put(name, this);
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return {@code true} if the {@link DiatonicInterval#THIRD third} is removed from the original {@link Chord}
     * (applies to all {@link ChordExtension}s starting with "sus" for suspended but also to others),
     * {@code false} otherwise.
     * @see #isRemoveFifth()
     */
    public boolean isRemoveThird() {
        return this.removeThird;
    }

    /**
     * @return {@code true} if the {@link DiatonicInterval#FIFTH fifth} is removed from the original {@link Chord},
     * {@code false} otherwise.
     * @see #isRemoveThird()
     */
    public boolean isRemoveFifth() {
        return this.removeFifth;
    }

    /**
     * @return the {@link List} of {@link ChromaticInterval}s added by this extension.
     */
    public List<ChromaticInterval> getIntervals() {
        return this.intervals;
    }

    /**
     * @return the compact reference form of this extension. Multiple extensions exists that are semantically
     * equivalent but have different {@link #getName() names}. However, all these equivalent extensions
     * will have the same reference.
     */
    public ChordExtension getReference() {
        return this.reference;
    }

    @Override
    public String toString() {
        return this.name;
    }

    private static ChordExtension create(boolean removeThird, boolean removeFifth, ChromaticInterval interval, String name, String ... altNames) {
        return create(removeThird, removeFifth, Collections.singletonList(interval), name, altNames);
    }

    private static ChordExtension create(boolean removeThird, boolean removeFifth, List<ChromaticInterval> intervals, String name, String ... altNames) {
        ChordExtension extension = new ChordExtension(name, removeThird, removeFifth, intervals, null);
        for (String altName : altNames) {
            // automatically added to map via constructor...
            new ChordExtension(altName, removeThird, removeFifth, intervals, extension);
        }
        return extension;
    }

    /**
     * Gets the {@link ChordExtension} for the given {@code string}.
     *
     * @param string is the {@link #getName() name} of the requested {@link ChordExtension}.
     * @return the requested {@link ChordExtension} or {@code null} if no such {@link ChordExtension} exists.
     */
    public static ChordExtension of(String string) {
        ChordExtension result = null;
        if (string != null) {
            result = NAME2EXT_MAP.get(string.toLowerCase(Locale.US));
        }
        return result;
    }

}
