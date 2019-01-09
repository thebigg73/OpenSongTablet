/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.rythm;

import com.garethevans.church.opensongtablet.core.ObjectHelper;
import com.garethevans.church.opensongtablet.core.music.note.Note;
import com.garethevans.church.opensongtablet.core.music.tone.Tone;
import com.garethevans.church.opensongtablet.core.music.transpose.AbstractTransposable;

import java.util.List;

/**
 * A valued item is a single item of a musical notation system such as
 * {@link com.garethevans.church.opensongtablet.core.music.stave.Stave}. It always has a {@link #getValue()
 * value} defining its duration.
 *
 * @author hohwille
 */
public abstract class ValuedItem<SELF extends ValuedItem<SELF>> extends AbstractTransposable<SELF> {

    protected final MusicalValue value;

    private final List<ValuedItemDecoration> decorations;

    /**
     * The constructor.
     *
     * @param value - the {@link #getValue() value}.
     */
    public ValuedItem(MusicalValue value, List<ValuedItemDecoration> decorations) {

        super();
        ObjectHelper.requireNonNull(value, "value");
        this.value = value;
        this.decorations = decorations;
    }

    /**
     * @return the {@link Tone} of this item or <code>null</code> if this item does not have a tone
     * (e.g. {@link Rest}).
     * @see Note
     */
    public Tone getTone() {
        return null;
    }

    /**
     * @return the {@link MusicalValue} that defines the duration of this item.
     */
    public MusicalValue getValue() {
        return this.value;
    }

    /**
     * @return the {@link List} of {@link ValuedItemDecoration}s.
     */
    public List<ValuedItemDecoration> getDecorations() {
        return this.decorations;
    }

    @Override
    public int hashCode() {
        return ObjectHelper.hash(this.value, getTone());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        ValuedItem<?> other = (ValuedItem<?>) obj;
        if (!ObjectHelper.equals(this.value, other.value)) {
            return false;
        }
        if (!ObjectHelper.equals(getTone(), other.getTone())) {
            return false;
        }
        if (!ObjectHelper.equals(this.decorations, other.decorations)) {
            return false;
        }
        return true;
    }

}
