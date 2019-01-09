package com.garethevans.church.opensongtablet.core.music;

/**
 * A {@link PeriodType} is used for objects that may range for a period, what means the range over
 * multiple columns of a {@link com.garethevans.church.opensongtablet.core.music.partiture.Partiture}.
 *
 * @see com.garethevans.church.opensongtablet.core.music.rythm.ValuedItemDecoration#getPeriod()
 */
public enum PeriodType {

    /** Indicates the start of something. */
    START,

    /** Indicates the end of something. */
    END

}
