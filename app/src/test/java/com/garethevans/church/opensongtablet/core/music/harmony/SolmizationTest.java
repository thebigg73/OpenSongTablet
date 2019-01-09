/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.music.harmony.Solmization;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Test of {@link Solmization}.
 *
 * @author hohwille
 */
public class SolmizationTest extends Assertions {

  /** Test of {@link Solmization#getMajorDiatonicSteps()}. */
  @Test
  public void testGetMajorDiatonicSteps() {

    assertThat(Solmization.DO.getMajorDiatonicSteps()).isEqualTo(0);
    assertThat(Solmization.DI.getMajorDiatonicSteps()).isEqualTo(0);
    assertThat(Solmization.RA.getMajorDiatonicSteps()).isEqualTo(1);
    assertThat(Solmization.RE.getMajorDiatonicSteps()).isEqualTo(1);
    assertThat(Solmization.RI.getMajorDiatonicSteps()).isEqualTo(1);
    assertThat(Solmization.ME.getMajorDiatonicSteps()).isEqualTo(2);
    assertThat(Solmization.MI.getMajorDiatonicSteps()).isEqualTo(2);
    assertThat(Solmization.FA.getMajorDiatonicSteps()).isEqualTo(3);
    assertThat(Solmization.FI.getMajorDiatonicSteps()).isEqualTo(3);
    assertThat(Solmization.SE.getMajorDiatonicSteps()).isEqualTo(4);
    assertThat(Solmization.SOL.getMajorDiatonicSteps()).isEqualTo(4);
    assertThat(Solmization.SI.getMajorDiatonicSteps()).isEqualTo(4);
    assertThat(Solmization.LE.getMajorDiatonicSteps()).isEqualTo(5);
    assertThat(Solmization.LA.getMajorDiatonicSteps()).isEqualTo(5);
    assertThat(Solmization.LI.getMajorDiatonicSteps()).isEqualTo(5);
    assertThat(Solmization.TE.getMajorDiatonicSteps()).isEqualTo(6);
    assertThat(Solmization.TI.getMajorDiatonicSteps()).isEqualTo(6);
  }

  /** Test of {@link Solmization#getMajorDiatonicSteps()}. */
  @Test
  public void testGetMajorChromaticSteps() {

    assertThat(Solmization.DO.getMajorChromaticSteps()).isEqualTo(0);
    assertThat(Solmization.DI.getMajorChromaticSteps()).isEqualTo(1);
    assertThat(Solmization.RA.getMajorChromaticSteps()).isEqualTo(1);
    assertThat(Solmization.RE.getMajorChromaticSteps()).isEqualTo(2);
    assertThat(Solmization.RI.getMajorChromaticSteps()).isEqualTo(3);
    assertThat(Solmization.ME.getMajorChromaticSteps()).isEqualTo(3);
    assertThat(Solmization.MI.getMajorChromaticSteps()).isEqualTo(4);
    assertThat(Solmization.FA.getMajorChromaticSteps()).isEqualTo(5);
    assertThat(Solmization.FI.getMajorChromaticSteps()).isEqualTo(6);
    assertThat(Solmization.SE.getMajorChromaticSteps()).isEqualTo(6);
    assertThat(Solmization.SOL.getMajorChromaticSteps()).isEqualTo(7);
    assertThat(Solmization.SI.getMajorChromaticSteps()).isEqualTo(8);
    assertThat(Solmization.LE.getMajorChromaticSteps()).isEqualTo(8);
    assertThat(Solmization.LA.getMajorChromaticSteps()).isEqualTo(9);
    assertThat(Solmization.LI.getMajorChromaticSteps()).isEqualTo(10);
    assertThat(Solmization.TE.getMajorChromaticSteps()).isEqualTo(10);
    assertThat(Solmization.TI.getMajorChromaticSteps()).isEqualTo(11);
  }

  /** Test of {@link Solmization#getMinorDiatonicSteps()}. */
  @Test
  public void testGetMinorDiatonicSteps() {

    assertThat(Solmization.LE.getMinorDiatonicSteps()).isEqualTo(0);
    assertThat(Solmization.LA.getMinorDiatonicSteps()).isEqualTo(0);
    assertThat(Solmization.LI.getMinorDiatonicSteps()).isEqualTo(0);
    assertThat(Solmization.TE.getMinorDiatonicSteps()).isEqualTo(1);
    assertThat(Solmization.TI.getMinorDiatonicSteps()).isEqualTo(1);
    assertThat(Solmization.DO.getMinorDiatonicSteps()).isEqualTo(2);
    assertThat(Solmization.DI.getMinorDiatonicSteps()).isEqualTo(2);
    assertThat(Solmization.RA.getMinorDiatonicSteps()).isEqualTo(3);
    assertThat(Solmization.RE.getMinorDiatonicSteps()).isEqualTo(3);
    assertThat(Solmization.RI.getMinorDiatonicSteps()).isEqualTo(3);
    assertThat(Solmization.ME.getMinorDiatonicSteps()).isEqualTo(4);
    assertThat(Solmization.MI.getMinorDiatonicSteps()).isEqualTo(4);
    assertThat(Solmization.FA.getMinorDiatonicSteps()).isEqualTo(5);
    assertThat(Solmization.FI.getMinorDiatonicSteps()).isEqualTo(5);
    assertThat(Solmization.SE.getMinorDiatonicSteps()).isEqualTo(6);
    assertThat(Solmization.SOL.getMinorDiatonicSteps()).isEqualTo(6);
    assertThat(Solmization.SI.getMinorDiatonicSteps()).isEqualTo(6);
  }

  /** Test of {@link Solmization#getMinorChromaticSteps()}. */
  @Test
  public void testGetMinorChromaticSteps() {

    assertThat(Solmization.LA.getMinorChromaticSteps()).isEqualTo(0);
    assertThat(Solmization.LI.getMinorChromaticSteps()).isEqualTo(1);
    assertThat(Solmization.TE.getMinorChromaticSteps()).isEqualTo(1);
    assertThat(Solmization.TI.getMinorChromaticSteps()).isEqualTo(2);
    assertThat(Solmization.DO.getMinorChromaticSteps()).isEqualTo(3);
    assertThat(Solmization.DI.getMinorChromaticSteps()).isEqualTo(4);
    assertThat(Solmization.RA.getMinorChromaticSteps()).isEqualTo(4);
    assertThat(Solmization.RE.getMinorChromaticSteps()).isEqualTo(5);
    assertThat(Solmization.RI.getMinorChromaticSteps()).isEqualTo(6);
    assertThat(Solmization.ME.getMinorChromaticSteps()).isEqualTo(6);
    assertThat(Solmization.MI.getMinorChromaticSteps()).isEqualTo(7);
    assertThat(Solmization.FA.getMinorChromaticSteps()).isEqualTo(8);
    assertThat(Solmization.FI.getMinorChromaticSteps()).isEqualTo(9);
    assertThat(Solmization.SE.getMinorChromaticSteps()).isEqualTo(9);
    assertThat(Solmization.SOL.getMinorChromaticSteps()).isEqualTo(10);
    assertThat(Solmization.SI.getMinorChromaticSteps()).isEqualTo(11);
    assertThat(Solmization.LE.getMinorChromaticSteps()).isEqualTo(11);
  }

}
