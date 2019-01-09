package com.garethevans.church.opensongtablet.core.music.transpose;

import com.garethevans.church.opensongtablet.core.music.harmony.Interval;
import com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey;

/**
 * A {@link Transposable} is a musical object that can be {@link #transpose(int, boolean, TransposeContext)
 * transposed}. Please note that transposing is a complex operation and can behave slightly different
 * depending on the type of object (the implementing class). Atomic objects such as
 * {@link com.garethevans.church.opensongtablet.core.music.tone.TonePitch} require specific
 * contextual information while others like {@link MusicalKey} do not. To provide the transposing feature
 * via this interface as a single API a {@link TransposeContext} is used for contextual information.
 * This allows to pre-define settings from outside for transposing as well as modifying the context
 * during the recursive transposing of complex objects like e.g. a complete
 * {@link com.garethevans.church.opensongtablet.core.music.partiture.Partiture}. The drawback is that
 * some of the methods defined here like e.g. {@link #transposeChromatic(int)} will not work perfectly
 * or make sense as expected if invoked on low-level types like e.g. a
 * {@link com.garethevans.church.opensongtablet.core.music.tone.TonePitch}. In such case use
 * {@link #transpose(int, boolean, TransposeContext)} and set the {@link MusicalKey} in the
 * given {@link TransposeContext} before invoking the transpose method.
 *
 * @param <SELF> this object itself
 */
public interface Transposable<SELF extends Transposable<SELF>> {

    /**
     * Transposes this object by the given number of {@code steps} in a generic way.
     *
     * @param steps    the number of steps to transpose. A positive value transposes towards a higher
     *                 tone, a negative value transposes towards a lower tone.
     * @param diatonic {@code true} for {@link #transposeDiatonic(int) transposing diatonic}
     *                 and {@code false} for {@link #transposeChromatic(int) transposing chromatic}.
     * @param context  the {@link TransposeContext}.
     * @return the transposed "copy" of this instance.
     */
    SELF transpose(int steps, boolean diatonic, TransposeContext context);

    /**
     * Transposes this object by the given number of <em>semitone</em> {@code steps} using the
     * {@link MusicalKey#getChromaticScale() chromatic scale}.
     *
     * @param steps is the number of semitone steps to transpose. A positive value transposes towards a higher
     *              tone, a negative value transposes towards a lower tone. A value of zero ({@code 0}) will cause no change.
     * @return the transposed "copy" of this instance.
     */
    SELF transposeChromatic(int steps);

    /**
     * Transposes this object by the given number of <em>diatonic</em> {@code steps} using the
     * {@link MusicalKey#getDiatonicScale() diatonic scale}.
     *
     * @param steps is the number of diatonic steps to transpose. A positive value transposes towards a higher
     *              tone, a negative value transposes towards a lower tone. A value of zero ({@code 0})
     *              will cause no change.
     * @return the transposed "copy" of this instance.
     */
    SELF transposeDiatonic(int steps);

    /**
     * Transposes this object by the given number of {@code steps} in a generic way.
     *
     * @param interval the {@link Interval} by which to transpose.
     * @param context  the {@link TransposeContext}.
     * @return the transposed "copy" of this instance.
     */
    SELF transpose(Interval interval, TransposeContext context);

}
