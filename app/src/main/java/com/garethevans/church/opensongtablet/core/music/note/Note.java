/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.note;

import com.garethevans.church.opensongtablet.core.ObjectHelper;
import com.garethevans.church.opensongtablet.core.music.rythm.MusicalValue;
import com.garethevans.church.opensongtablet.core.music.rythm.ValuedItemDecoration;
import com.garethevans.church.opensongtablet.core.music.rythm.ValuedItem;
import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Note} is a musical object that has a {@link #getTone() tone} and a {@link #getValue() value}.
 *
 * @author hohwille
 */
public class Note extends ValuedItem<Note> {

    private final Tone tone;

    /**
     * The constructor.
     *
     * @param tone  - the {@link #getTone() tone}.
     * @param value - the {@link #getValue() value}.
     */
    public Note(Tone tone, MusicalValue value) {
        this(tone, value, new ArrayList<ValuedItemDecoration>());
    }

    /**
     * The constructor.
     *
     * @param tone        - the {@link #getTone() tone}.
     * @param value       - the {@link #getValue() value}.
     * @param decorations - the {@link #getDecorations() decorations}.
     */
    public Note(Tone tone, MusicalValue value, List<ValuedItemDecoration> decorations) {
        super(value, decorations);
        ObjectHelper.requireNonNull(tone, "tone");
        this.tone = tone;
    }

    @Override
    public Tone getTone() {
        return this.tone;
    }

    @Override
    public Note transpose(int steps, boolean diatonic, TransposeContext context) {

        Tone newTone = this.tone.transpose(steps, diatonic, context);
        return new Note(newTone, getValue(), new ArrayList<>(getDecorations()));
    }

    @Override
    public String toString() {
        return this.tone.toString() + this.value;
    }
}
