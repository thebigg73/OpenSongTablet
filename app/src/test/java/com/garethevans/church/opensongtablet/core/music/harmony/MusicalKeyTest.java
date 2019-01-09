/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchEnglish;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Test of {@link MusicalKey}.
 */
public class MusicalKeyTest extends Assertions {

    /**
     * Test of {@link MusicalKey#C_MAJOR}.
     */
    @Test
    public void testCMajor() {

        assertThat(MusicalKey.C_MAJOR.getName()).isEqualTo("C");
        assertThat(MusicalKey.C_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.C_MAJOR.getTonika()).isSameAs(TonePitchEnglish.C);
        assertThat(MusicalKey.C_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.NORMAL);
        assertThat(MusicalKey.C_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.C, TonePitchEnglish.D, TonePitchEnglish.E,
                TonePitchEnglish.F, TonePitchEnglish.G, TonePitchEnglish.A, TonePitchEnglish.B);
        assertThat(MusicalKey.C_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.C, TonePitchEnglish.C_SHARP, TonePitchEnglish.D,
                TonePitchEnglish.D_SHARP, TonePitchEnglish.E, TonePitchEnglish.F, TonePitchEnglish.F_SHARP, TonePitchEnglish.G, TonePitchEnglish.G_SHARP, TonePitchEnglish.A,
                TonePitchEnglish.B_FLAT, TonePitchEnglish.B);
        assertThat(MusicalKey.C_MAJOR.getChromaticSignTones()).isEmpty();
    }

    /**
     * Test of {@link MusicalKey#C_SHARP_MAJOR}.
     */
    @Test
    public void testCSharpMajor() {

        assertThat(MusicalKey.C_SHARP_MAJOR.getName()).isEqualTo("C#");
        assertThat(MusicalKey.C_SHARP_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.C_SHARP_MAJOR.getTonika()).isSameAs(TonePitchEnglish.C_SHARP);
        assertThat(MusicalKey.C_SHARP_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.C_SHARP_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.C_SHARP,
                TonePitchEnglish.D_SHARP, TonePitchEnglish.E_SHARP, TonePitchEnglish.F_SHARP,
                TonePitchEnglish.G_SHARP, TonePitchEnglish.A_SHARP, TonePitchEnglish.B_SHARP);
        assertThat(MusicalKey.C_SHARP_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.C_SHARP,
                TonePitchEnglish.D, TonePitchEnglish.D_SHARP, TonePitchEnglish.E, TonePitchEnglish.E_SHARP,
                TonePitchEnglish.F_SHARP, TonePitchEnglish.G, TonePitchEnglish.G_SHARP, TonePitchEnglish.A,
                TonePitchEnglish.A_SHARP, TonePitchEnglish.B, TonePitchEnglish.B_SHARP);
        assertThat(MusicalKey.C_SHARP_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.G_SHARP, TonePitchEnglish.D_SHARP,
                TonePitchEnglish.A_SHARP, TonePitchEnglish.E_SHARP, TonePitchEnglish.B_SHARP);
    }

    /**
     * Test of {@link MusicalKey#D_FLAT_MAJOR}.
     */
    @Test
    public void testDFlatMajor() {

        assertThat(MusicalKey.D_FLAT_MAJOR.getName()).isEqualTo("Db");
        assertThat(MusicalKey.D_FLAT_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.D_FLAT_MAJOR.getTonika()).isSameAs(TonePitchEnglish.D_FLAT);
        assertThat(MusicalKey.D_FLAT_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.D_FLAT_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.D_FLAT,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.F, TonePitchEnglish.G_FLAT,
                TonePitchEnglish.A_FLAT, TonePitchEnglish.B_FLAT, TonePitchEnglish.C);
        assertThat(MusicalKey.D_FLAT_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.D_FLAT,
                TonePitchEnglish.D, TonePitchEnglish.E_FLAT, TonePitchEnglish.E, TonePitchEnglish.F,
                TonePitchEnglish.G_FLAT, TonePitchEnglish.G, TonePitchEnglish.A_FLAT, TonePitchEnglish.A,
                TonePitchEnglish.B_FLAT, TonePitchEnglish.B, TonePitchEnglish.C);
        assertThat(MusicalKey.D_FLAT_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.A_FLAT, TonePitchEnglish.D_FLAT, TonePitchEnglish.G_FLAT);
    }

    /**
     * Test of {@link MusicalKey#D_MAJOR}.
     */
    @Test
    public void testDMajor() {

        assertThat(MusicalKey.D_MAJOR.getName()).isEqualTo("D");
        assertThat(MusicalKey.D_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.D_MAJOR.getTonika()).isSameAs(TonePitchEnglish.D);
        assertThat(MusicalKey.D_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.D_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.D,
                TonePitchEnglish.E, TonePitchEnglish.F_SHARP, TonePitchEnglish.G,
                TonePitchEnglish.A, TonePitchEnglish.B, TonePitchEnglish.C_SHARP);
        assertThat(MusicalKey.D_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.D,
                TonePitchEnglish.D_SHARP, TonePitchEnglish.E, TonePitchEnglish.F, TonePitchEnglish.F_SHARP,
                TonePitchEnglish.G, TonePitchEnglish.G_SHARP, TonePitchEnglish.A, TonePitchEnglish.A_SHARP,
                TonePitchEnglish.B, TonePitchEnglish.C, TonePitchEnglish.C_SHARP);
        assertThat(MusicalKey.D_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP, TonePitchEnglish.C_SHARP);
    }

    /**
     * Test of {@link MusicalKey#E_FLAT_MAJOR}.
     */
    @Test
    public void testEFlatMajor() {

        assertThat(MusicalKey.E_FLAT_MAJOR.getName()).isEqualTo("Eb");
        assertThat(MusicalKey.E_FLAT_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.E_FLAT_MAJOR.getTonika()).isSameAs(TonePitchEnglish.E_FLAT);
        assertThat(MusicalKey.E_FLAT_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.E_FLAT_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.E_FLAT,
                TonePitchEnglish.F, TonePitchEnglish.G, TonePitchEnglish.A_FLAT,
                TonePitchEnglish.B_FLAT, TonePitchEnglish.C, TonePitchEnglish.D);
        assertThat(MusicalKey.E_FLAT_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.E_FLAT,
                TonePitchEnglish.E, TonePitchEnglish.F, TonePitchEnglish.G_FLAT, TonePitchEnglish.G,
                TonePitchEnglish.A_FLAT, TonePitchEnglish.A, TonePitchEnglish.B_FLAT, TonePitchEnglish.B,
                TonePitchEnglish.C, TonePitchEnglish.D_FLAT, TonePitchEnglish.D);
        assertThat(MusicalKey.E_FLAT_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.A_FLAT);
    }

    /**
     * Test of {@link MusicalKey#E_MAJOR}.
     */
    @Test
    public void testEMajor() {

        assertThat(MusicalKey.E_MAJOR.getName()).isEqualTo("E");
        assertThat(MusicalKey.E_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.E_MAJOR.getTonika()).isSameAs(TonePitchEnglish.E);
        assertThat(MusicalKey.E_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.E_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.E,
                TonePitchEnglish.F_SHARP, TonePitchEnglish.G_SHARP, TonePitchEnglish.A,
                TonePitchEnglish.B, TonePitchEnglish.C_SHARP, TonePitchEnglish.D_SHARP);
        assertThat(MusicalKey.E_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.E,
                TonePitchEnglish.F, TonePitchEnglish.F_SHARP, TonePitchEnglish.G, TonePitchEnglish.G_SHARP,
                TonePitchEnglish.A, TonePitchEnglish.A_SHARP, TonePitchEnglish.B, TonePitchEnglish.C,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.D, TonePitchEnglish.D_SHARP);
        assertThat(MusicalKey.E_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.G_SHARP, TonePitchEnglish.D_SHARP);
    }

    /**
     * Test of {@link MusicalKey#F_MAJOR}.
     */
    @Test
    public void testFMajor() {

        assertThat(MusicalKey.F_MAJOR.getName()).isEqualTo("F");
        assertThat(MusicalKey.F_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.F_MAJOR.getTonika()).isSameAs(TonePitchEnglish.F);
        assertThat(MusicalKey.F_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.F_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.F,
                TonePitchEnglish.G, TonePitchEnglish.A, TonePitchEnglish.B_FLAT,
                TonePitchEnglish.C, TonePitchEnglish.D, TonePitchEnglish.E);
        assertThat(MusicalKey.F_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.F,
                TonePitchEnglish.G_FLAT, TonePitchEnglish.G, TonePitchEnglish.A_FLAT, TonePitchEnglish.A,
                TonePitchEnglish.B_FLAT, TonePitchEnglish.B, TonePitchEnglish.C, TonePitchEnglish.D_FLAT,
                TonePitchEnglish.D, TonePitchEnglish.E_FLAT, TonePitchEnglish.E);
        assertThat(MusicalKey.F_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT);
    }

    /**
     * Test of {@link MusicalKey#F_SHARP_MAJOR}.
     */
    @Test
    public void testFSharpMajor() {

        assertThat(MusicalKey.F_SHARP_MAJOR.getName()).isEqualTo("F#");
        assertThat(MusicalKey.F_SHARP_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.F_SHARP_MAJOR.getTonika()).isSameAs(TonePitchEnglish.F_SHARP);
        assertThat(MusicalKey.F_SHARP_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.F_SHARP_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.G_SHARP, TonePitchEnglish.A_SHARP, TonePitchEnglish.B,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.D_SHARP, TonePitchEnglish.E_SHARP);
        assertThat(MusicalKey.F_SHARP_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.G, TonePitchEnglish.G_SHARP, TonePitchEnglish.A, TonePitchEnglish.A_SHARP,
                TonePitchEnglish.B, TonePitchEnglish.C, TonePitchEnglish.C_SHARP, TonePitchEnglish.D,
                TonePitchEnglish.D_SHARP, TonePitchEnglish.E, TonePitchEnglish.E_SHARP);
        assertThat(MusicalKey.F_SHARP_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.G_SHARP, TonePitchEnglish.D_SHARP,
                TonePitchEnglish.A_SHARP, TonePitchEnglish.E_SHARP);
    }

    /**
     * Test of {@link MusicalKey#G_FLAT_MAJOR}.
     */
    @Test
    public void testGFlatMajor() {

        assertThat(MusicalKey.G_FLAT_MAJOR.getName()).isEqualTo("Gb");
        assertThat(MusicalKey.G_FLAT_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.G_FLAT_MAJOR.getTonika()).isSameAs(TonePitchEnglish.G_FLAT);
        assertThat(MusicalKey.G_FLAT_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.G_FLAT_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.G_FLAT,
                TonePitchEnglish.A_FLAT, TonePitchEnglish.B_FLAT, TonePitchEnglish.C_FLAT,
                TonePitchEnglish.D_FLAT, TonePitchEnglish.E_FLAT, TonePitchEnglish.F);
        assertThat(MusicalKey.G_FLAT_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.G_FLAT,
                TonePitchEnglish.G, TonePitchEnglish.A_FLAT, TonePitchEnglish.A, TonePitchEnglish.B_FLAT,
                TonePitchEnglish.C_FLAT, TonePitchEnglish.C, TonePitchEnglish.D_FLAT, TonePitchEnglish.D,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.E, TonePitchEnglish.F);
        assertThat(MusicalKey.G_FLAT_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.A_FLAT, TonePitchEnglish.D_FLAT,
                TonePitchEnglish.G_FLAT, TonePitchEnglish.C_FLAT);
    }

    /**
     * Test of {@link MusicalKey#G_MAJOR}.
     */
    @Test
    public void testGMajor() {

        assertThat(MusicalKey.G_MAJOR.getName()).isEqualTo("G");
        assertThat(MusicalKey.G_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.G_MAJOR.getTonika()).isSameAs(TonePitchEnglish.G);
        assertThat(MusicalKey.G_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.G_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.G,
                TonePitchEnglish.A, TonePitchEnglish.B, TonePitchEnglish.C,
                TonePitchEnglish.D, TonePitchEnglish.E, TonePitchEnglish.F_SHARP);
        assertThat(MusicalKey.G_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.G,
                TonePitchEnglish.G_SHARP, TonePitchEnglish.A, TonePitchEnglish.A_SHARP, TonePitchEnglish.B,
                TonePitchEnglish.C, TonePitchEnglish.C_SHARP, TonePitchEnglish.D, TonePitchEnglish.D_SHARP,
                TonePitchEnglish.E, TonePitchEnglish.F, TonePitchEnglish.F_SHARP);
        assertThat(MusicalKey.G_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP);
    }

    /**
     * Test of {@link MusicalKey#A_FLAT_MAJOR}.
     */
    @Test
    public void testAFlatMajor() {

        assertThat(MusicalKey.A_FLAT_MAJOR.getName()).isEqualTo("Ab");
        assertThat(MusicalKey.A_FLAT_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.A_FLAT_MAJOR.getTonika()).isSameAs(TonePitchEnglish.A_FLAT);
        assertThat(MusicalKey.A_FLAT_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.A_FLAT_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.A_FLAT,
                TonePitchEnglish.B_FLAT, TonePitchEnglish.C, TonePitchEnglish.D_FLAT,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.F, TonePitchEnglish.G);
        assertThat(MusicalKey.A_FLAT_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.A_FLAT,
                TonePitchEnglish.A, TonePitchEnglish.B_FLAT, TonePitchEnglish.B, TonePitchEnglish.C,
                TonePitchEnglish.D_FLAT, TonePitchEnglish.D, TonePitchEnglish.E_FLAT, TonePitchEnglish.E,
                TonePitchEnglish.F, TonePitchEnglish.G_FLAT, TonePitchEnglish.G);
        assertThat(MusicalKey.A_FLAT_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.A_FLAT, TonePitchEnglish.D_FLAT);
    }

    /**
     * Test of {@link MusicalKey#A_MAJOR}.
     */
    @Test
    public void testAMajor() {

        assertThat(MusicalKey.A_MAJOR.getName()).isEqualTo("A");
        assertThat(MusicalKey.A_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.A_MAJOR.getTonika()).isSameAs(TonePitchEnglish.A);
        assertThat(MusicalKey.A_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.A_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.A,
                TonePitchEnglish.B, TonePitchEnglish.C_SHARP, TonePitchEnglish.D,
                TonePitchEnglish.E, TonePitchEnglish.F_SHARP, TonePitchEnglish.G_SHARP);
        assertThat(MusicalKey.A_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.A,
                TonePitchEnglish.A_SHARP, TonePitchEnglish.B, TonePitchEnglish.C, TonePitchEnglish.C_SHARP,
                TonePitchEnglish.D, TonePitchEnglish.D_SHARP, TonePitchEnglish.E, TonePitchEnglish.F,
                TonePitchEnglish.F_SHARP, TonePitchEnglish.G, TonePitchEnglish.G_SHARP);
        assertThat(MusicalKey.A_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.G_SHARP);
    }

    /**
     * Test of {@link MusicalKey#B_FLAT_MAJOR}.
     */
    @Test
    public void testBFlatMajor() {

        assertThat(MusicalKey.B_FLAT_MAJOR.getName()).isEqualTo("Bb");
        assertThat(MusicalKey.B_FLAT_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.B_FLAT_MAJOR.getTonika()).isSameAs(TonePitchEnglish.B_FLAT);
        assertThat(MusicalKey.B_FLAT_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.B_FLAT_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.B_FLAT,
                TonePitchEnglish.C, TonePitchEnglish.D, TonePitchEnglish.E_FLAT,
                TonePitchEnglish.F, TonePitchEnglish.G, TonePitchEnglish.A);
        assertThat(MusicalKey.B_FLAT_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.B_FLAT,
                TonePitchEnglish.B, TonePitchEnglish.C, TonePitchEnglish.D_FLAT, TonePitchEnglish.D,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.E, TonePitchEnglish.F, TonePitchEnglish.G_FLAT,
                TonePitchEnglish.G, TonePitchEnglish.A_FLAT, TonePitchEnglish.A);
        assertThat(MusicalKey.B_FLAT_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT, TonePitchEnglish.E_FLAT);
    }

    /**
     * Test of {@link MusicalKey#B_MAJOR}.
     */
    @Test
    public void testBMajor() {

        assertThat(MusicalKey.B_MAJOR.getName()).isEqualTo("B");
        assertThat(MusicalKey.B_MAJOR.getTonalSystem()).isSameAs(TonalSystem.MAJOR);
        assertThat(MusicalKey.B_MAJOR.getTonika()).isSameAs(TonePitchEnglish.B);
        assertThat(MusicalKey.B_MAJOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.B_MAJOR.getDiatonicScale()).containsExactly(TonePitchEnglish.B,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.D_SHARP, TonePitchEnglish.E,
                TonePitchEnglish.F_SHARP, TonePitchEnglish.G_SHARP, TonePitchEnglish.A_SHARP);
        assertThat(MusicalKey.B_MAJOR.getChromaticScale()).containsExactly(TonePitchEnglish.B,
                TonePitchEnglish.C, TonePitchEnglish.C_SHARP, TonePitchEnglish.D, TonePitchEnglish.D_SHARP,
                TonePitchEnglish.E, TonePitchEnglish.F, TonePitchEnglish.F_SHARP, TonePitchEnglish.G,
                TonePitchEnglish.G_SHARP, TonePitchEnglish.A, TonePitchEnglish.A_SHARP);
        assertThat(MusicalKey.B_MAJOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.G_SHARP, TonePitchEnglish.D_SHARP, TonePitchEnglish.A_SHARP);
    }

    /**
     * Test of {@link MusicalKey#C_MINOR}.
     */
    @Test
    public void testCMinor() {

        assertThat(MusicalKey.C_MINOR.getName()).isEqualTo("c");
        assertThat(MusicalKey.C_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.C_MINOR.getTonika()).isSameAs(TonePitchEnglish.C);
        assertThat(MusicalKey.C_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.C_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.C,
                TonePitchEnglish.D, TonePitchEnglish.E_FLAT, TonePitchEnglish.F,
                TonePitchEnglish.G, TonePitchEnglish.A_FLAT, TonePitchEnglish.B_FLAT);
        assertThat(MusicalKey.C_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.C,
                TonePitchEnglish.D_FLAT, TonePitchEnglish.D, TonePitchEnglish.E_FLAT, TonePitchEnglish.E,
                TonePitchEnglish.F, TonePitchEnglish.G_FLAT, TonePitchEnglish.G, TonePitchEnglish.A_FLAT,
                TonePitchEnglish.A, TonePitchEnglish.B_FLAT, TonePitchEnglish.B);
        assertThat(MusicalKey.C_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.A_FLAT);
    }

    /**
     * Test of {@link MusicalKey#C_SHARP_MINOR}.
     */
    @Test
    public void testCSharpMinor() {

        assertThat(MusicalKey.C_SHARP_MINOR.getName()).isEqualTo("c#");
        assertThat(MusicalKey.C_SHARP_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.C_SHARP_MINOR.getTonika()).isSameAs(TonePitchEnglish.C_SHARP);
        assertThat(MusicalKey.C_SHARP_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.C_SHARP_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.C_SHARP,
                TonePitchEnglish.D_SHARP, TonePitchEnglish.E, TonePitchEnglish.F_SHARP,
                TonePitchEnglish.G_SHARP, TonePitchEnglish.A, TonePitchEnglish.B);
        assertThat(MusicalKey.C_SHARP_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.C_SHARP,
                TonePitchEnglish.D, TonePitchEnglish.D_SHARP, TonePitchEnglish.E, TonePitchEnglish.F,
                TonePitchEnglish.F_SHARP, TonePitchEnglish.G, TonePitchEnglish.G_SHARP, TonePitchEnglish.A,
                TonePitchEnglish.A_SHARP, TonePitchEnglish.B, TonePitchEnglish.C);
        assertThat(MusicalKey.C_SHARP_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.G_SHARP, TonePitchEnglish.D_SHARP);
    }

    /**
     * Test of {@link MusicalKey#D_MINOR}.
     */
    @Test
    public void testDMinor() {

        assertThat(MusicalKey.D_MINOR.getName()).isEqualTo("d");
        assertThat(MusicalKey.D_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.D_MINOR.getTonika()).isSameAs(TonePitchEnglish.D);
        assertThat(MusicalKey.D_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.D_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.D,
                TonePitchEnglish.E, TonePitchEnglish.F, TonePitchEnglish.G,
                TonePitchEnglish.A, TonePitchEnglish.B_FLAT, TonePitchEnglish.C);
        assertThat(MusicalKey.D_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.D,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.E, TonePitchEnglish.F, TonePitchEnglish.G_FLAT,
                TonePitchEnglish.G, TonePitchEnglish.A_FLAT, TonePitchEnglish.A, TonePitchEnglish.B_FLAT,
                TonePitchEnglish.B, TonePitchEnglish.C, TonePitchEnglish.D_FLAT);
        assertThat(MusicalKey.D_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT);
    }

    /**
     * Test of {@link MusicalKey#D_SHARP_MINOR}.
     */
    @Test
    public void testDSharpMinor() {

        assertThat(MusicalKey.D_SHARP_MINOR.getName()).isEqualTo("d#");
        assertThat(MusicalKey.D_SHARP_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.D_SHARP_MINOR.getTonika()).isSameAs(TonePitchEnglish.D_SHARP);
        assertThat(MusicalKey.D_SHARP_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.D_SHARP_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.D_SHARP,
                TonePitchEnglish.E_SHARP, TonePitchEnglish.F_SHARP, TonePitchEnglish.G_SHARP,
                TonePitchEnglish.A_SHARP, TonePitchEnglish.B, TonePitchEnglish.C_SHARP);
        assertThat(MusicalKey.D_SHARP_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.D_SHARP,
                TonePitchEnglish.E, TonePitchEnglish.E_SHARP, TonePitchEnglish.F_SHARP, TonePitchEnglish.G,
                TonePitchEnglish.G_SHARP, TonePitchEnglish.A, TonePitchEnglish.A_SHARP, TonePitchEnglish.B,
                TonePitchEnglish.C, TonePitchEnglish.C_SHARP, TonePitchEnglish.D);
        assertThat(MusicalKey.D_SHARP_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.G_SHARP, TonePitchEnglish.D_SHARP,
                TonePitchEnglish.A_SHARP, TonePitchEnglish.E_SHARP);
    }

    /**
     * Test of {@link MusicalKey#E_FLAT_MINOR}.
     */
    @Test
    public void testEFlatMinor() {

        assertThat(MusicalKey.E_FLAT_MINOR.getName()).isEqualTo("eb");
        assertThat(MusicalKey.E_FLAT_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.E_FLAT_MINOR.getTonika()).isSameAs(TonePitchEnglish.E_FLAT);
        assertThat(MusicalKey.E_FLAT_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.E_FLAT_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.E_FLAT,
                TonePitchEnglish.F, TonePitchEnglish.G_FLAT, TonePitchEnglish.A_FLAT,
                TonePitchEnglish.B_FLAT, TonePitchEnglish.C_FLAT, TonePitchEnglish.D_FLAT);
        assertThat(MusicalKey.E_FLAT_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.E_FLAT,
                TonePitchEnglish.E, TonePitchEnglish.F, TonePitchEnglish.G_FLAT, TonePitchEnglish.G,
                TonePitchEnglish.A_FLAT, TonePitchEnglish.A, TonePitchEnglish.B_FLAT, TonePitchEnglish.C_FLAT,
                TonePitchEnglish.C, TonePitchEnglish.D_FLAT, TonePitchEnglish.D);
        assertThat(MusicalKey.E_FLAT_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.A_FLAT, TonePitchEnglish.D_FLAT,
                TonePitchEnglish.G_FLAT, TonePitchEnglish.C_FLAT);
    }

    /**
     * Test of {@link MusicalKey#E_MINOR}.
     */
    @Test
    public void testEMinor() {

        assertThat(MusicalKey.E_MINOR.getName()).isEqualTo("e");
        assertThat(MusicalKey.E_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.E_MINOR.getTonika()).isSameAs(TonePitchEnglish.E);
        assertThat(MusicalKey.E_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.E_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.E,
                TonePitchEnglish.F_SHARP, TonePitchEnglish.G, TonePitchEnglish.A,
                TonePitchEnglish.B, TonePitchEnglish.C, TonePitchEnglish.D);
        assertThat(MusicalKey.E_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.E,
                TonePitchEnglish.F, TonePitchEnglish.F_SHARP, TonePitchEnglish.G, TonePitchEnglish.G_SHARP,
                TonePitchEnglish.A, TonePitchEnglish.A_SHARP, TonePitchEnglish.B, TonePitchEnglish.C,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.D, TonePitchEnglish.D_SHARP);
        assertThat(MusicalKey.E_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP);
    }

    /**
     * Test of {@link MusicalKey#F_MINOR}.
     */
    @Test
    public void testFMinor() {

        assertThat(MusicalKey.F_MINOR.getName()).isEqualTo("f");
        assertThat(MusicalKey.F_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.F_MINOR.getTonika()).isSameAs(TonePitchEnglish.F);
        assertThat(MusicalKey.F_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.F_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.F,
                TonePitchEnglish.G, TonePitchEnglish.A_FLAT, TonePitchEnglish.B_FLAT,
                TonePitchEnglish.C, TonePitchEnglish.D_FLAT, TonePitchEnglish.E_FLAT);
        assertThat(MusicalKey.F_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.F,
                TonePitchEnglish.G_FLAT, TonePitchEnglish.G, TonePitchEnglish.A_FLAT, TonePitchEnglish.A,
                TonePitchEnglish.B_FLAT, TonePitchEnglish.B, TonePitchEnglish.C, TonePitchEnglish.D_FLAT,
                TonePitchEnglish.D, TonePitchEnglish.E_FLAT, TonePitchEnglish.E);
        assertThat(MusicalKey.F_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.A_FLAT, TonePitchEnglish.D_FLAT);
    }

    /**
     * Test of {@link MusicalKey#F_SHARP_MINOR}.
     */
    @Test
    public void testFSharpMinor() {

        assertThat(MusicalKey.F_SHARP_MINOR.getName()).isEqualTo("f#");
        assertThat(MusicalKey.F_SHARP_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.F_SHARP_MINOR.getTonika()).isSameAs(TonePitchEnglish.F_SHARP);
        assertThat(MusicalKey.F_SHARP_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.F_SHARP_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.G_SHARP, TonePitchEnglish.A, TonePitchEnglish.B,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.D, TonePitchEnglish.E);
        assertThat(MusicalKey.F_SHARP_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.G, TonePitchEnglish.G_SHARP, TonePitchEnglish.A, TonePitchEnglish.A_SHARP,
                TonePitchEnglish.B, TonePitchEnglish.C, TonePitchEnglish.C_SHARP, TonePitchEnglish.D,
                TonePitchEnglish.D_SHARP, TonePitchEnglish.E, TonePitchEnglish.F);
        assertThat(MusicalKey.F_SHARP_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.G_SHARP);
    }

    /**
     * Test of {@link MusicalKey#G_MINOR}.
     */
    @Test
    public void testGMinor() {

        assertThat(MusicalKey.G_MINOR.getName()).isEqualTo("g");
        assertThat(MusicalKey.G_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.G_MINOR.getTonika()).isSameAs(TonePitchEnglish.G);
        assertThat(MusicalKey.G_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.G_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.G,
                TonePitchEnglish.A, TonePitchEnglish.B_FLAT, TonePitchEnglish.C,
                TonePitchEnglish.D, TonePitchEnglish.E_FLAT, TonePitchEnglish.F);
        assertThat(MusicalKey.G_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.G,
                TonePitchEnglish.A_FLAT, TonePitchEnglish.A, TonePitchEnglish.B_FLAT, TonePitchEnglish.B,
                TonePitchEnglish.C, TonePitchEnglish.D_FLAT, TonePitchEnglish.D, TonePitchEnglish.E_FLAT,
                TonePitchEnglish.E, TonePitchEnglish.F, TonePitchEnglish.G_FLAT);
        assertThat(MusicalKey.G_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT, TonePitchEnglish.E_FLAT);
    }

    /**
     * Test of {@link MusicalKey#G_SHARP_MINOR}.
     */
    @Test
    public void testGSharpMinor() {

        assertThat(MusicalKey.G_SHARP_MINOR.getName()).isEqualTo("g#");
        assertThat(MusicalKey.G_SHARP_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.G_SHARP_MINOR.getTonika()).isSameAs(TonePitchEnglish.G_SHARP);
        assertThat(MusicalKey.G_SHARP_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.G_SHARP_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.G_SHARP,
                TonePitchEnglish.A_SHARP, TonePitchEnglish.B, TonePitchEnglish.C_SHARP,
                TonePitchEnglish.D_SHARP, TonePitchEnglish.E, TonePitchEnglish.F_SHARP);
        assertThat(MusicalKey.G_SHARP_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.G_SHARP,
                TonePitchEnglish.A, TonePitchEnglish.A_SHARP, TonePitchEnglish.B, TonePitchEnglish.C,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.D, TonePitchEnglish.D_SHARP, TonePitchEnglish.E,
                TonePitchEnglish.F, TonePitchEnglish.F_SHARP, TonePitchEnglish.G);
        assertThat(MusicalKey.G_SHARP_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.G_SHARP, TonePitchEnglish.D_SHARP, TonePitchEnglish.A_SHARP);
    }

    /**
     * Test of {@link MusicalKey#A_FLAT_MINOR}.
     */
    @Test
    public void testAFlatMinor() {

        assertThat(MusicalKey.A_FLAT_MINOR.getName()).isEqualTo("ab");
        assertThat(MusicalKey.A_FLAT_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.A_FLAT_MINOR.getTonika()).isSameAs(TonePitchEnglish.A_FLAT);
        assertThat(MusicalKey.A_FLAT_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.FLAT);
        assertThat(MusicalKey.A_FLAT_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.A_FLAT,
                TonePitchEnglish.B_FLAT, TonePitchEnglish.C_FLAT, TonePitchEnglish.D_FLAT,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.F_FLAT, TonePitchEnglish.G_FLAT);
        assertThat(MusicalKey.A_FLAT_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.A_FLAT,
                TonePitchEnglish.A, TonePitchEnglish.B_FLAT, TonePitchEnglish.C_FLAT, TonePitchEnglish.C,
                TonePitchEnglish.D_FLAT, TonePitchEnglish.D, TonePitchEnglish.E_FLAT, TonePitchEnglish.F_FLAT,
                TonePitchEnglish.F, TonePitchEnglish.G_FLAT, TonePitchEnglish.G);
        assertThat(MusicalKey.A_FLAT_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.B_FLAT,
                TonePitchEnglish.E_FLAT, TonePitchEnglish.A_FLAT, TonePitchEnglish.D_FLAT,
                TonePitchEnglish.G_FLAT, TonePitchEnglish.C_FLAT, TonePitchEnglish.F_FLAT);
    }

    /**
     * Test of {@link MusicalKey#A_MINOR}.
     */
    @Test
    public void testAMinor() {

        assertThat(MusicalKey.A_MINOR.getName()).isEqualTo("a");
        assertThat(MusicalKey.A_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.A_MINOR.getTonika()).isSameAs(TonePitchEnglish.A);
        assertThat(MusicalKey.A_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.NORMAL);
        assertThat(MusicalKey.A_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.A,
                TonePitchEnglish.B, TonePitchEnglish.C, TonePitchEnglish.D,
                TonePitchEnglish.E, TonePitchEnglish.F, TonePitchEnglish.G);
        assertThat(MusicalKey.A_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.A,
                TonePitchEnglish.B_FLAT, TonePitchEnglish.B, TonePitchEnglish.C, TonePitchEnglish.C_SHARP,
                TonePitchEnglish.D, TonePitchEnglish.D_SHARP, TonePitchEnglish.E, TonePitchEnglish.F,
                TonePitchEnglish.F_SHARP, TonePitchEnglish.G, TonePitchEnglish.G_SHARP);
        assertThat(MusicalKey.A_MINOR.getChromaticSignTones()).isEmpty();
    }

    /**
     * Test of {@link MusicalKey#A_SHARP_MINOR}.
     */
    @Test
    public void testASharpMinor() {

        assertThat(MusicalKey.A_SHARP_MINOR.getName()).isEqualTo("a#");
        assertThat(MusicalKey.A_SHARP_MINOR.getTonalSystem()).isSameAs(TonalSystem.MINOR);
        assertThat(MusicalKey.A_SHARP_MINOR.getTonika()).isSameAs(TonePitchEnglish.A_SHARP);
        assertThat(MusicalKey.A_SHARP_MINOR.getEnharmonicStyle()).isSameAs(EnharmonicStyle.SHARP);
        assertThat(MusicalKey.A_SHARP_MINOR.getDiatonicScale()).containsExactly(TonePitchEnglish.A_SHARP,
                TonePitchEnglish.B_SHARP, TonePitchEnglish.C_SHARP, TonePitchEnglish.D_SHARP,
                TonePitchEnglish.E_SHARP, TonePitchEnglish.F_SHARP, TonePitchEnglish.G_SHARP);
        assertThat(MusicalKey.A_SHARP_MINOR.getChromaticScale()).containsExactly(TonePitchEnglish.A_SHARP,
                TonePitchEnglish.B, TonePitchEnglish.B_SHARP, TonePitchEnglish.C_SHARP, TonePitchEnglish.D,
                TonePitchEnglish.D_SHARP, TonePitchEnglish.E, TonePitchEnglish.E_SHARP, TonePitchEnglish.F_SHARP,
                TonePitchEnglish.G, TonePitchEnglish.G_SHARP, TonePitchEnglish.A);
        assertThat(MusicalKey.A_SHARP_MINOR.getChromaticSignTones()).containsExactly(TonePitchEnglish.F_SHARP,
                TonePitchEnglish.C_SHARP, TonePitchEnglish.G_SHARP, TonePitchEnglish.D_SHARP,
                TonePitchEnglish.A_SHARP, TonePitchEnglish.E_SHARP, TonePitchEnglish.B_SHARP);
    }

    /**
     * Test of {@link MusicalKey#values() all} {@link MusicalKey}s with some generic asserts.
     */
    @Test
    public void testAll() {

        for (MusicalKey key : MusicalKey.values()) {
            String name = key.getName();
            assertThat(key.toString()).isEqualTo(name + "-" + key.getTonalSystem());
            assertThat(MusicalKey.fromName(name)).isSameAs(key);
        }
    }

    /**
     * Test of {@link MusicalKey#getTone(Interval)}.
     */
    @Test
    public void testGetTone() {

        assertThat(MusicalKey.C_MAJOR.getTone(Solmization.DO)).isSameAs(TonePitchEnglish.C);
        assertThat(MusicalKey.C_MAJOR.getTone(Solmization.RE)).isSameAs(TonePitchEnglish.D);
        assertThat(MusicalKey.C_MAJOR.getTone(Solmization.MI)).isSameAs(TonePitchEnglish.E);
        assertThat(MusicalKey.C_MAJOR.getTone(Solmization.FA)).isSameAs(TonePitchEnglish.F);
        assertThat(MusicalKey.C_MAJOR.getTone(Solmization.SOL)).isSameAs(TonePitchEnglish.G);
        assertThat(MusicalKey.C_MAJOR.getTone(Solmization.LA)).isSameAs(TonePitchEnglish.A);
        assertThat(MusicalKey.C_MAJOR.getTone(Solmization.TI)).isSameAs(TonePitchEnglish.B);

        assertThat(MusicalKey.A_SHARP_MINOR.getTone(Solmization.LA)).isSameAs(TonePitchEnglish.A_SHARP);
        assertThat(MusicalKey.A_SHARP_MINOR.getTone(Solmization.TI)).isSameAs(TonePitchEnglish.B_SHARP);
        assertThat(MusicalKey.A_SHARP_MINOR.getTone(Solmization.DO)).isSameAs(TonePitchEnglish.C_SHARP);
        assertThat(MusicalKey.A_SHARP_MINOR.getTone(Solmization.RE)).isSameAs(TonePitchEnglish.D_SHARP);
        assertThat(MusicalKey.A_SHARP_MINOR.getTone(Solmization.MI)).isSameAs(TonePitchEnglish.E_SHARP);
        assertThat(MusicalKey.A_SHARP_MINOR.getTone(Solmization.FA)).isSameAs(TonePitchEnglish.F_SHARP);
        assertThat(MusicalKey.A_SHARP_MINOR.getTone(Solmization.SOL)).isSameAs(TonePitchEnglish.G_SHARP);
    }

}
