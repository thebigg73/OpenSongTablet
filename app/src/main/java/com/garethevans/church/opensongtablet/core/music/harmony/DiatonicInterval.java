/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.harmony;

/**
 * Enum with the {@link #getDiatonicSteps() diatonic} {@link Interval}s.
 *
 * @author hohwille
 */
public enum DiatonicInterval implements Interval {

  /** The "empty" interval called (perfect) <em>unison</em>. Also called <em>primum</em>. */
  UNISON(0),

  /** <em>secundum</em>. */
  SECOND(1),

  /** <em>tertium</em>. */
  THIRD(2),

  /** <em>quartum</em>. */
  FOURTH(3),

  /** <em>quintum</em>. */
  FIFTH(4),

  /** <em>sextum</em>. */
  SIXTH(5),

  /** <em>septum</em>. */
  SEVENTH(6),

  /** Eight or <em>octave</em>. */
  OCTAVE(7),

  /** <em>nonum</em>. */
  NINTH(8),

  /** <em>decum</em>. */
  TENTH(9),

  /** <em>eleventh</em>. */
  ELEVENTH(10),

  /** <em>twelfth</em> or <em>tritave</em>. */
  TWELFTH(11),

  /** <em>thirteenth</em> or <em>compound sixth</em>. */
  THIRTEENTH(12);

  private final int diatonicSteps;

  /**
   * The constructor.
   *
   * @param diatonicSteps - see {@link #getDiatonicSteps()}.
   */
  private DiatonicInterval(int diatonicSteps) {

    this.diatonicSteps = diatonicSteps;
  }

  @Override
  public int getChromaticSteps(TonalSystem system) {

    if (this == UNISON) {
      return 0;
    } else if (this == OCTAVE) {
      return 12;
    }
    return Integer.MIN_VALUE;
  }

  @Override
  public int getDiatonicSteps(TonalSystem system) {

    return this.diatonicSteps;
  }

  /**
   * @see #getDiatonicSteps(TonalSystem)
   *
   * @return the number of diatonic steps defined by this interval.
   */
  public int getDiatonicSteps() {

    return this.diatonicSteps;
  }


  /**
   * @param diatonicSteps the number of {@link #getDiatonicSteps() diatonic steps}.
   * @return the corresponding {@link DiatonicInterval} or <code>null</code> if no such {@link DiatonicInterval}
   * exists (given value is negative or too high).
   */
  public static DiatonicInterval ofDiatonicSteps(int diatonicSteps) {

    for (DiatonicInterval interval : values()) {
      if (interval.diatonicSteps == diatonicSteps) {
        return interval;
      }
    }
    return null;
  }

}
