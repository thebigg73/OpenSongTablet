/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.tone;

import com.garethevans.church.opensongtablet.core.music.harmony.ChromaticInterval;
import com.garethevans.church.opensongtablet.core.music.harmony.EnharmonicStyle;
import com.garethevans.church.opensongtablet.core.music.harmony.Interval;
import com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey;
import com.garethevans.church.opensongtablet.core.music.harmony.TonalSystem;
import com.garethevans.church.opensongtablet.core.music.transpose.AbstractTransposable;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;

/**
 * A {@link TonePitch} represents a tone within a {@link MusicalKey#getChromaticScale() scale}.
 * It is based on the twelve tone music system and only represents a relative pitch within a single octave.
 * If you want to represent an absolute pitch value use {@link Tone} instead.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0
 */
public abstract class TonePitch extends AbstractTransposable<TonePitch> {

    protected final String name;

    protected final ChromaticStep step;

    protected final EnharmonicType enharmonicType;

    protected final ToneNameCase nameCase;

    protected TonePitch(String name, ChromaticStep step, ToneNameCase nameCase, EnharmonicType enharmonicType) {
        super();
        assert ((name != null) && (!name.isEmpty()));
        this.name = name;
        assert (step != null);
        this.step = step;
        assert (nameCase.convert(name).equals(name));
        this.nameCase = nameCase;
        assert (enharmonicType != null);
        this.enharmonicType = enharmonicType;
        assert (getNameStyle().checkName(name, enharmonicType)) : name + getNameStyle();
    }

    /**
     * @return the number of half-tone steps upwards from {@link TonePitchEnglish#C} in the range from {@code 0}-{@code 11}.
     */
    public ChromaticStep getStep() {
        return this.step;
    }

    /**
     * @return the name of this pitch.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the {@link ToneNameStyle} of this pitch.
     */
    public abstract ToneNameStyle getNameStyle();

    /**
     * @return the {@link EnharmonicStyle} of this pitch.
     * @see #isFlat()
     * @see #isNormal()
     * @see #isSharp()
     */
    public EnharmonicStyle getEnharmonicStyle() {
        return this.enharmonicType.getStyle();
    }

    public EnharmonicType getEnharmonicType() {
        return this.enharmonicType;
    }

    /**
     * @return the reference {@link TonePitch} this pitch is derived from. All {@link TonePitch}es of
     * the same {@link #getNameStyle() name style} with the same {@link #getStep() step} are just
     * alternate {@link #isLowercase() cases}, or {@link #getEnharmonicStyle() enharmonic changes} of the
     * semantically same pitch. Therefore they all share the same reference what is the {@link TonePitch}
     * with capital {@link #getCase() case} and in {@link #isNormal() normal} form. E.g.
     * {@code Fb}, {@code fb}, {@code D##}, {@code d##}, and {@code e} will all have the same reference
     * which is {@code E}.
     */
    public abstract TonePitch getReference();

    public int getSignCount() {
        return this.enharmonicType.getSignCount();
    }

    /**
     * @return {@code true} if this {@link TonePitch} requires a single or double sharp sign,
     * {@code false} otherwise.
     */
    public boolean isSharp() {
        return EnharmonicStyle.SHARP.equals(this.enharmonicType.getStyle());
    }

    /**
     * @return {@code true} if this {@link TonePitch} requires a single or double flat sign,
     * {@code false} otherwise.
     */
    public boolean isFlat() {
        return EnharmonicStyle.FLAT.equals(this.enharmonicType.getStyle());
    }

    public TonePitch sharpen() {
        EnharmonicType newType = this.enharmonicType.sharpen();
        if (newType == null) {
            return null;
        }
        return getNameStyle().pitch(this.step.next(), newType, this.nameCase);
    }

    public TonePitch flatten() {
        EnharmonicType newType = this.enharmonicType.flatten();
        if (newType == null) {
            return null;
        }
        return getNameStyle().pitch(this.step.previous(), newType, this.nameCase);
    }

    /**
     * @param nameStyle the {@link ToneNameStyle} of the requested pitch.
     * @return a {@link TonePitch} with the same {@link #getReference() reference} and {@link #getCase() case}
     * as this pitch but with the given {@link ToneNameStyle}.
     */
    public <P extends TonePitch> P with(ToneNameStyle<P> nameStyle) {
        return with(nameStyle, this.nameCase);
    }

    /**
     * @param nameStyle the {@link ToneNameStyle} of the requested pitch.
     * @param nameCase  the {@link #getCase() case} of the requested pitch.
     * @return a {@link TonePitch} with the same {@link #getReference() reference} and {@link #getStep() step}
     * as this pitch but with the given {@link #getCase() case} and {@link #getNameStyle() name style}.
     */
    public <P extends TonePitch> P with(ToneNameStyle<P> nameStyle, ToneNameCase nameCase) {
        return nameStyle.pitch(this.step, this.enharmonicType, nameCase);
    }

    /**
     * @param nameCase the {@link #getCase() case} of the requested pitch.
     * @return a {@link TonePitch} with the same {@link #getReference() reference} and {@link #getStep() step}
     * as this pitch but with the given {@link #getCase() case}.
     */
    public abstract TonePitch with(ToneNameCase nameCase);

    /**
     * @param enharmonicType the {@link #getEnharmonicType() enharmonic type} of the requested pitch.
     * @return a {@link TonePitch} with the same {@link #getStep() step} and {@link #getCase() case}
     * as this pitch but with the given {@link EnharmonicType}.
     */
    public TonePitch with(EnharmonicType enharmonicType) {
        return with(enharmonicType, this.nameCase);
    }

    /**
     * @param enharmonicType the {@link #getEnharmonicType() enharmonic type} of the requested pitch.
     * @param nameCase       the {@link #getCase() case} of the requested pitch.
     * @return a {@link TonePitch} with the same {@link #getStep() step} as this pitch
     * but with the given {@link #getCase() case} and (if possible) the given {@link EnharmonicType}.
     */
    public TonePitch with(EnharmonicType enharmonicType, ToneNameCase nameCase) {
        return with(getNameStyle(), enharmonicType, nameCase);
    }

    /**
     * @param enharmonicType the {@link #getEnharmonicType() enharmonic type} of the requested pitch.
     * @param nameCase       the {@link #getCase() case} of the requested pitch.
     * @return a {@link TonePitch} with the same {@link #getStep() step} as this pitch
     * but with the given {@link #getCase() case} and (if possible) the given {@link EnharmonicType}.
     */
    public <P extends TonePitch> P with(ToneNameStyle<P> nameStyle, EnharmonicType enharmonicType, ToneNameCase nameCase) {
        return nameStyle.pitch(this.step, enharmonicType, nameCase);
    }


    /**
     * @return the normal form of this {@link TonePitch} in case this is an enharmonic change. In case this {@link TonePitch}
     * itself if it is already {@link #isNormal() normal} this method will return this instance itself.
     */
    public TonePitch getNormalForm() {
        return getNameStyle().pitch(this.step, null, this.nameCase);
    }

    /**
     * @return {@code true} if this is the {@link #getNormalForm() normal form} (that would be used in
     * {@link MusicalKey#C_MAJOR}), {@code false} in case of an <em>enharmonic change</em>.
     */
    public boolean isNormal() {
        return (getNormalForm() == this);
    }

    /**
     * @return the {@link ToneNameCase} of this {@link TonePitch}.
     */
    public ToneNameCase getCase() {
        return nameCase;
    }

    /**
     * @return {@code true} if the {@link #getName() name} is entirely in {@link String#toLowerCase() lower case},
     * {@code false} otherwise (the first letter is {@link Character#isUpperCase(char) upper case}).
     */
    public boolean isLowercase() {
        return (this.nameCase == ToneNameCase.LOWER_CASE);
    }

    /**
     * @param targetTone is the target {@link TonePitch}.
     * @return the {@link ChromaticInterval} to {@link #transpose(Interval, TransposeContext) get} from this
     * {@link TonePitch} to the given {@code targetTone}.
     */
    public ChromaticInterval getInterval(TonePitch targetTone) {
        int interval = targetTone.step.get() - this.step.get();
        if (interval < 0) {
            interval = interval + 12;
        }
        return ChromaticInterval.of(interval);
    }

    @Override
    public TonePitch transpose(int steps, boolean diatonic, TransposeContext context) {
        MusicalKey key = null;
        EnharmonicStyle style = EnharmonicStyle.NORMAL;
        TonalSystem system = null;
        if (context != null) {
            key = context.getKey();
            style = context.getEnharmonicStyle();
            system = context.getTonalSystem();
        }
        if (key == null) {
            if (diatonic) {
                if (system != null) {
                    int chromaticSteps = system.getChromaticSteps(steps);
                    if (chromaticSteps != Integer.MIN_VALUE) {
                        return transposeChromatic(chromaticSteps, style);
                    }
                }
                // can not transpose properly without key, using fallback is better than exception
                return transposeDiatonic(steps, MusicalKey.C_MAJOR);
            } else {
                return transposeChromatic(steps, style);
            }
        } else if (diatonic) {
            return transposeDiatonic(steps, key);
        } else {
            return transposeChromatic(steps, key);
        }
    }

    public TonePitch transposeChromatic(int semitoneSteps, EnharmonicStyle style) {
        return transposeChromatic(semitoneSteps, style, true);
    }

    public TonePitch transposeChromatic(int semitoneSteps, EnharmonicStyle style, boolean strict) {
        ChromaticStep targetStep = this.step.add(semitoneSteps);
        TonePitch result;
        if (strict) {
            result = getNameStyle().pitch(targetStep, this.nameCase, style);
        } else {
            result = getNameStyle().pitch(targetStep, null, this.nameCase);
            EnharmonicStyle enharmonicStyle = result.getEnharmonicStyle();
            if (enharmonicStyle.isNormal() || (enharmonicStyle == style)) {
                return result;
            }
            result = getNameStyle().pitch(targetStep, this.nameCase, style);
        }
        return result;
    }

    public TonePitch transposeChromatic(int semitoneSteps, MusicalKey key) {
        ChromaticStep targetStep = this.step.add(semitoneSteps - key.getTonika().getStep().get());
        return key.getChromaticScale().get(targetStep.get()).with(getNameStyle(), this.nameCase);
    }

    public TonePitch transposeDiatonic(int diatonicSteps, MusicalKey key) {
        ChromaticStep chromaticTonikaOffset = this.step.add(-key.getTonika().getStep().get());
        int step = key.getTonalSystem().getDiatonicSteps(chromaticTonikaOffset.get(), false);
        DiatonicStep targetStep = DiatonicStep.of(step).add(diatonicSteps);
        return key.getDiatonicScale().get(targetStep.get()).with(getNameStyle(), this.nameCase);
    }

    public boolean isEqualTo(TonePitch pitch) {
        if (pitch == null) {
            return false;
        }
        return (this.step == pitch.step);
    }

    @Override
    public String toString() {
        return this.name;
    }

}
