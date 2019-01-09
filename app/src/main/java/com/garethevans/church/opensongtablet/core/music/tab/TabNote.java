/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.tab;

import com.garethevans.church.opensongtablet.core.music.harmony.Interval;
import com.garethevans.church.opensongtablet.core.music.rythm.MusicalValue;
import com.garethevans.church.opensongtablet.core.music.harmony.TonalSystem;
import com.garethevans.church.opensongtablet.core.music.note.Note;
import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.tone.ToneNameStyle;

import java.util.Set;

/**
 * A {@link Note} in a {@link Tab}.
 */
public class TabNote {

    private final MusicalValue value;

    private final int fret;

    private final int targetFret;

    private final Set<TabEffect> effects;

    /**
     * The constructor.
     *
     * @param value - the {@link #getValue() value}.
     * @param fret  - the {@link #getFret() fret}.
     */
    private TabNote(MusicalValue value, int fret) {

        this(value, fret, fret, TabEffect.FX_NONE);
    }

    /**
     * The constructor.
     *
     * @param value      - the {@link #getValue() value}.
     * @param fret       - the {@link #getFret() fret}.
     * @param targetFret - the {@link #getTargetFret() target fret}.
     * @param effects    - the {@link #getEffects() effects}.
     */
    private TabNote(MusicalValue value, int fret, int targetFret, Set<TabEffect> effects) {

        super();
        this.value = value;
        this.fret = fret;
        this.targetFret = targetFret;
        this.effects = effects;
    }

    /**
     * @return the {@link MusicalValue}.
     */
    public MusicalValue getValue() {
        return this.value;
    }

    /**
     * @return the fret which represents the {@link Interval#getChromaticSteps(TonalSystem) chromatic
     *         interval} of the {@link Tone} relative
     *         to the {@link TabString#getTone() base tone} of the {@link TabString string}. Should
     *         not be negative.
     */
    public int getFret() {

        return this.fret;
    }

    /**
     * @return the {@link #getFret() fret} of the tone to play during a potential effect such as a
     *         {@link #isSlide() slide} or {@link #isBend() bend}.
     */
    public int getTargetFret() {
        return this.targetFret;
    }

    /**
     * @return the {@link java.util.Collections#unmodifiableSet(Set) unmodifiable} {@link Set} of
     *         {@link TabEffect}s.
     */
    public Set<TabEffect> getEffects() {
        return this.effects;
    }

    /**
     * @return {@code true} in case of a {@link TabEffect#SLIDE slide} from the source {@link #getFret()
     *         fret} to {@link #getTargetFret() target fret}.
     */
    public boolean isSlide() {
        return this.effects.contains(TabEffect.SLIDE);
    }

    /**
     * @return {@code true} in case of a {@link TabEffect#BEND bend} from the source {@link #getFret()
     *         fret} up to the tone that would be produced by playing the {@link #getTargetFret() target
     *         fret}.
     */
    public boolean isBend() {
        return this.effects.contains(TabEffect.BEND);
    }

    /**
     * @return {@code true} in case of a {@link TabEffect#HAMMER_ON hammer on} effect.
     */
    public boolean isHammerOn() {
        return this.effects.contains(TabEffect.HAMMER_ON);
    }

    /**
     * @return {@code true} in case of a {@link TabEffect#PULL_OFF pull off} effect.
     */
    public boolean isPullOff() {
        return this.effects.contains(TabEffect.PULL_OFF);
    }

    public static TabNote of(MusicalValue value, int fret) {

        return new TabNote(value, fret);
    }

    public static TabNote ofHamer(MusicalValue value, int fret) {

        return new TabNote(value, fret, fret, TabEffect.FX_HAMMER);
    }

    public static TabNote ofPull(MusicalValue value, int fret) {

        return new TabNote(value, fret, fret, TabEffect.FX_PULL);
    }

    public static TabNote ofHammerAndPull(MusicalValue value, int fret) {

        return new TabNote(value, fret, fret, TabEffect.FX_HAMMER_AND_PULL);
    }

    public static TabNote ofSlide(MusicalValue value, int fret, int targetFret) {

        return new TabNote(value, fret, targetFret, TabEffect.FX_SLIDE);
    }

    public static TabNote ofBend(MusicalValue value, int fret, int targetFret) {

        return new TabNote(value, fret, targetFret, TabEffect.FX_BEND);
    }

    public static TabNote ofHammerAndSlide(MusicalValue value, int fret, int targetFret) {

        return new TabNote(value, fret, targetFret, TabEffect.FX_HAMMER_AND_SLIDE);
    }

    public static TabNote ofHammerAndBend(MusicalValue value, int fret, int targetFret) {

        return new TabNote(value, fret, targetFret, TabEffect.FX_HAMMER_AND_BEND);
    }

    public static TabNote ofSlideAndPull(MusicalValue value, int fret, int targetFret) {

        return new TabNote(value, fret, targetFret, TabEffect.FX_SLIDE_AND_PULL);
    }

    public static TabNote ofBendAndPull(MusicalValue value, int fret, int targetFret) {

        return new TabNote(value, fret, targetFret, TabEffect.FX_BEND_AND_PULL);
    }

    public static TabNote ofHammerAndSlideAndPull(MusicalValue value, int fret, int targetFret) {

        return new TabNote(value, fret, targetFret, TabEffect.FX_HAMMER_AND_SLIDE_AND_PULL);
    }

    public static TabNote ofHammerAndBendAndPull(MusicalValue value, int fret, int targetFret) {

        return new TabNote(value, fret, targetFret, TabEffect.FX_HAMMER_AND_BEND_AND_PULL);
    }

    public static TabNote of(MusicalValue value, int fret, int targetFret, Set<TabEffect> effects) {

        return new TabNote(value, fret, targetFret, effects);
    }
}
