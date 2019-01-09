/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchEnglish;

/**
 * Solmization is a system of attributing a distinct syllable to each note in a {@link MusicalKey#getDiatonicScale()
 * musical scale}. In general there are different variants of solmization grown over history. However, the most
 * interesting is the relative solmization that associates tones of a {@link MusicalKey#getDiatonicScale() scale} with
 * syllables and is therefore independent of a {@link MusicalKey}.<br/>
 * So for a {@link TonalSystem#MAJOR major} {@link MusicalKey#getDiatonicScale() scale} the {@link Solmization} order is
 * {@link #DO}, {@link #RE}, {@link #MI}, {@link #FA}, {@link #SOL}, {@link #LA}, {@link #TI}. In
 * {@link MusicalKey#C_MAJOR C-major} this would be equivalent to {@link TonePitchEnglish#C C}, {@link TonePitchEnglish#D D},
 * {@link TonePitchEnglish#E E}, {@link TonePitchEnglish#F F}, {@link TonePitchEnglish#G G}, {@link TonePitchEnglish#A A}, {@link TonePitchEnglish#B B}.<br/>
 * For a {@link TonalSystem#MINOR minor} {@link MusicalKey#getDiatonicScale() scale} the {@link Solmization} order is
 * {@link #LA}, {@link #TI}, {@link #DO}, {@link #RE}, {@link #MI}, {@link #FA}, {@link #SOL}. In
 * {@link MusicalKey#A_MINOR A-minor} this would be equivalent to {@link TonePitchEnglish#A A}, {@link TonePitchEnglish#B_SHARP B},
 * {@link TonePitchEnglish#C C}, {@link TonePitchEnglish#D D}, {@link TonePitchEnglish#E E}, {@link TonePitchEnglish#F F}, {@link TonePitchEnglish#G G}.<br/>
 *
 * @author hohwille
 */
public enum Solmization implements Interval {

  /**
   * The first tone in case of a {@link TonalSystem#MAJOR major} and the third tone in case of a
   * {@link TonalSystem#MINOR minor} {@link MusicalKey#getDiatonicScale() scale}.
   */
  DO(0, 3),

  /** The raised (sharpened) form of {@link #DO} (one semiton higher). Same as {@link #RA}. */
  DI(1, 4, 1),

  /** The lowered (flattened) form of {@link #RE} (one semiton lower). Same as {@link #DI}. */
  RA(1, 4, -1),

  /**
   * The second tone in case of a {@link TonalSystem#MAJOR major} and the fourth tone in case of a
   * {@link TonalSystem#MINOR minor} {@link MusicalKey#getDiatonicScale() scale}.
   */
  RE(2, 5),

  /** The raised (sharpened) form of {@link #RE} (one semiton higher). Same as {@link #ME}. */
  RI(3, 6, 1),

  /** The lowered (flattened) form of {@link #MI} (one semiton lower). Same as {@link #RI}. */
  ME(3, 6, -1),

  /**
   * The third tone in case of a {@link TonalSystem#MAJOR major} and the fifth tone in case of a
   * {@link TonalSystem#MINOR minor} {@link MusicalKey#getDiatonicScale() scale}.
   */
  MI(4, 7),

  /**
   * The fourth tone in case of a {@link TonalSystem#MAJOR major} and the sixth tone in case of a
   * {@link TonalSystem#MINOR minor} {@link MusicalKey#getDiatonicScale() scale}.
   */
  FA(5, 8),

  /** The raised (sharpened) form of {@link #FA} (one semiton higher). Same as {@link #SE}. */
  FI(6, 9, 1),

  /** The lowered (flattened) form of {@link #SOL} (one semiton lower). Same as {@link #FI}. */
  SE(6, 9, -1),

  /**
   * The fifth tone in case of a {@link TonalSystem#MAJOR major} and the seventh tone in case of a
   * {@link TonalSystem#MINOR minor} {@link MusicalKey#getDiatonicScale() scale}. It may also be called sol instead of
   * so.
   */
  SOL(7, 10),

  /** The raised (sharpened) form of {@link #SOL} (one semiton higher). Same as {@link #LE}. */
  SI(8, 11, 1),

  /** The lowered (flattened) form of {@link #LA} (one semiton lower). Same as {@link #SI}. */
  LE(8, 11, -1),

  /**
   * The sixth tone in case of a {@link TonalSystem#MAJOR major} and the first tone in case of a
   * {@link TonalSystem#MINOR minor} {@link MusicalKey#getDiatonicScale() scale}.
   */
  LA(9, 0),

  /** The raised (sharpened) form of {@link #LA} (one semiton higher). Same as {@link #TE}. */
  LI(10, 1, 1),

  /** The lowered (flattened) form of {@link #TI} (one semiton lower). Same as {@link #LI}. */
  TE(10, 1, -1),

  /**
   * The seventh tone in case of a {@link TonalSystem#MAJOR major} and the second tone in case of a
   * {@link TonalSystem#MINOR minor} {@link MusicalKey#getDiatonicScale() scale}.
   */
  TI(11, 2);

  /** @see #getMajorChromaticSteps() */
  private final int majorChromaticSteps;

  /** @see #getMinorChromaticSteps() */
  private final int minorChromaticSteps;

  private final int semitone;

  private Solmization(int majorStep, int minorStep) {

    this.majorChromaticSteps = majorStep;
    this.minorChromaticSteps = minorStep;
    this.semitone = 0;
  }

  private Solmization(int majorStep, int minorStep, int semitone) {

    this.majorChromaticSteps = majorStep;
    this.minorChromaticSteps = minorStep;
    this.semitone = semitone;
  }

  /**
   * @return the number of semitone steps from {@link MusicalKey#getTonika() tonika} in a {@link TonalSystem#MAJOR
   *         major} {@link MusicalKey#getDiatonicScale() scale}.
   */
  public int getMajorChromaticSteps() {

    return this.majorChromaticSteps;
  }

  /**
   * @return the number of semitone steps from {@link MusicalKey#getTonika() tonika} in a {@link TonalSystem#MINOR
   *         minor} {@link MusicalKey#getDiatonicScale() scale}.
   */
  public int getMinorChromaticSteps() {

    return this.minorChromaticSteps;
  }

  /**
   * @return the number of diatonic steps from {@link MusicalKey#getTonika() tonika} in a {@link TonalSystem#MAJOR
   *         major} {@link MusicalKey#getDiatonicScale() scale}.
   */
  public int getMajorDiatonicSteps() {

    return (this.majorChromaticSteps + 1 - this.semitone) / 2;
  }

  /**
   * @return the number of diatonic steps from {@link MusicalKey#getTonika() tonika} in a {@link TonalSystem#MINOR
   *         minor} {@link MusicalKey#getDiatonicScale() scale}.
   */
  public int getMinorDiatonicSteps() {

    int chromaticSteps = this.minorChromaticSteps + 1 - this.semitone;
    if (this.minorChromaticSteps >= 8) {
      chromaticSteps++;
    }
    if (chromaticSteps > 12) {
      return 0;
    }
    return chromaticSteps / 2;
  }

  @Override
  public int getChromaticSteps(TonalSystem system) {

    if (system == TonalSystem.MAJOR) {
      return getMajorChromaticSteps();
    } else if (system == TonalSystem.MINOR) {
      return getMinorChromaticSteps();
    } else {
      return Integer.MIN_VALUE;
    }
  }

  @Override
  public int getDiatonicSteps(TonalSystem system) {

    if (system == TonalSystem.MAJOR) {
      return getMajorDiatonicSteps();
    } else if (system == TonalSystem.MINOR) {
      return getMinorDiatonicSteps();
    } else {
      return Integer.MIN_VALUE;
    }
  }

}
