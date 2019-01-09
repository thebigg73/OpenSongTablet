/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.AbstractTest;
import com.garethevans.church.opensongtablet.core.music.tone.ToneNameCase;
import com.garethevans.church.opensongtablet.core.music.tone.ToneNameStyle;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchEnglish;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchGerman;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchInternational;
import com.garethevans.church.opensongtablet.core.music.transpose.TransposeContext;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import org.junit.Test;

/**
 * Test of {@link Chord}.
 *
 * @author hohwille
 */
public class ChordTest extends AbstractTest {

  /** Test of {@link ChordMapper#parse(CharStream)} (parsing). */
  @Test
  public void testParse() {

    checkEqualsAndHashCode(ChordMapper.INSTANCE.parse("C"), new Chord(TonePitchEnglish.C, TonalSystem.MAJOR_EMPTY), true);
    checkEqualsAndHashCode(ChordMapper.INSTANCE.parse("C"), ChordMapper.INSTANCE.parse("Cm"), false);
    checkEqualsAndHashCode(ChordMapper.INSTANCE.parse("C"), ChordMapper.INSTANCE.parse("C/B"), false);
    checkEqualsAndHashCode(ChordMapper.INSTANCE.parse("C"), ChordMapper.INSTANCE.parse("C7"), false);
    checkEqualsAndHashCode(ChordMapper.INSTANCE.parse("C"), ChordMapper.INSTANCE.parse("Cis"), false);
    assertThat(ChordMapper.INSTANCE.parse("C").getTonalSystem()).isSameAs(TonalSystem.MAJOR_EMPTY);
    assertThat(ChordMapper.INSTANCE.parse("Cm")).isEqualTo(new Chord(TonePitchEnglish.C, TonalSystem.MINOR_M));
    assertThat(ChordMapper.INSTANCE.parse("CMi")).isEqualTo(ChordMapper.INSTANCE.parse("CmI"));
    assertThat(ChordMapper.INSTANCE.parse("C#maj7/E")).isEqualTo(
        new Chord(TonePitchEnglish.C_SHARP, TonalSystem.MAJOR_EMPTY, TonePitchEnglish.E, ChordExtension.MAJ_7));
    Chord AsSus4Add9OverFeses = ChordMapper.INSTANCE.parse("a\u266Dsus4add9/F\uD834\uDD2B");
    assertThat(AsSus4Add9OverFeses).isEqualTo(
        new Chord(TonePitchInternational.A_FLAT.with(ToneNameCase.LOWER_CASE), null, TonePitchInternational.F_DOUBLE_FLAT, ChordExtension.SUS_4, ChordExtension.ADD_9));
    assertThat(AsSus4Add9OverFeses.getFundamentalTone()).isSameAs(TonePitchInternational.A_FLAT.with(ToneNameCase.LOWER_CASE));
    assertThat(AsSus4Add9OverFeses.getTonalSystem()).isNull();
    assertThat(AsSus4Add9OverFeses.getBaseTone()).isSameAs(TonePitchInternational.F_DOUBLE_FLAT);
    assertThat(AsSus4Add9OverFeses.getExtensions()).containsExactly(ChordExtension.SUS_4, ChordExtension.ADD_9);
    assertThat(ChordMapper.INSTANCE.parse("Bb")).isEqualTo(new Chord(TonePitchEnglish.B_FLAT, TonalSystem.MAJOR_EMPTY));
    assertThat(ChordMapper.INSTANCE.parse("B")).isEqualTo(new Chord(TonePitchEnglish.B, TonalSystem.MAJOR_EMPTY));
    // accords with additional clutter
    assertThat(ChordMapper.INSTANCE.parse("F/")).isEqualTo(new Chord(TonePitchEnglish.F, TonalSystem.MAJOR_EMPTY));
    Chord d6sus4SupertrampEvenInTheQuitesMoments = ChordMapper.INSTANCE.parse("D6sus4(xx0787)");
    assertThat(d6sus4SupertrampEvenInTheQuitesMoments).isEqualTo(new Chord(TonePitchEnglish.D, TonalSystem.MAJOR_EMPTY, ChordExtension._6, ChordExtension.SUS_4));
    assertThat(d6sus4SupertrampEvenInTheQuitesMoments.getName()).isEqualTo("D6sus4");
    // negative tests (invalid chords)
    assertThat(ChordMapper.INSTANCE.parse("X")).isNull();
    assertThat(ChordMapper.INSTANCE.parse("W")).isNull();
    assertThat(ChordMapper.INSTANCE.parse("N.C.")).isNull();
  }

  /** Test of {@link Chord#Chord(TonePitch, TonalSystem, TonePitch)}. */
  @Test
  public void testNew() {

    assertThat(new Chord(TonePitchGerman.FIS, TonalSystem.MAJOR_EMPTY, TonePitchEnglish.E)).isEqualTo(ChordMapper.INSTANCE.parse("Fis/E"));
    assertThat(new Chord(TonePitchEnglish.F_SHARP, TonalSystem.MAJOR_EMPTY, TonePitchEnglish.E)).isEqualTo(ChordMapper.INSTANCE.parse("F#/E"));
    assertThat(new Chord(TonePitchInternational.F_SHARP, TonalSystem.MAJOR_EMPTY, TonePitchEnglish.E)).isEqualTo(ChordMapper.INSTANCE.parse("F\u266F/E"));
    assertThat(new Chord(TonePitchGerman.ES, TonalSystem.MINOR_M, ChordExtension._7)).isEqualTo(ChordMapper.INSTANCE.parse("Esm7"));
    assertThat(new Chord(TonePitchEnglish.E_FLAT, TonalSystem.MINOR_M, ChordExtension._7)).isEqualTo(ChordMapper.INSTANCE.parse("Ebm7"));
    assertThat(new Chord(TonePitchInternational.E_FLAT.with(ToneNameCase.LOWER_CASE), TonalSystem.MINOR_M, ChordExtension._7)).isEqualTo(ChordMapper.INSTANCE.parse("e\u266Dm7"));
  }

  /** Test of {@link Chord#transposeChromatic(int)}. */
  @Test
  public void testTransposeChromatic() {

    assertThat(ChordMapper.INSTANCE.parse("C").transposeChromatic(1).getName()).isEqualTo("C#");
    assertThat(ChordMapper.INSTANCE.parse("ebmadd9/A").transposeChromatic(-1).getName()).isEqualTo("dmadd9/G#");
    assertThat(ChordMapper.INSTANCE.parse("E\u266Dmadd9/A").transposeChromatic(-1).getName()).isEqualTo("Dmadd9/G\u266F");
    assertThat(ChordMapper.INSTANCE.parse("Cis4add9no5/A").transposeChromatic(-1).toString()).isEqualTo("C4add9no5/Gis");
  }

  /** Test of {@link Chord#transpose(Interval, TransposeContext)}. */
  @Test
  public void testTransposeInterval() {

    // chromatic
    assertThat(
        ChordMapper.INSTANCE.parse("C\u266F7/B\u266D").transpose(ChromaticInterval.PERFECT_FOURTH,
            new TransposeContext(MusicalKey.C_SHARP_MAJOR.transposeChromatic(ChromaticInterval.PERFECT_FOURTH.getChromaticSteps()))))
        .isEqualTo(ChordMapper.INSTANCE.parse("F\u266F7/D\u266F"));

    assertThat(ChordMapper.INSTANCE.parse("ebmadd9/A").transposeChromatic(-1).getName()).isEqualTo("dmadd9/G#");
    assertThat(ChordMapper.INSTANCE.parse("E\u266Dmadd9/A").transposeChromatic(-1).getName()).isEqualTo("Dmadd9/G\u266F");
    assertThat(ChordMapper.INSTANCE.parse("Cis4add9no5/A").transposeChromatic(-1).getName()).isEqualTo("C4add9no5/Gis");

    // diatonic
    TransposeContext context = new TransposeContext(MusicalKey.C_SHARP_MAJOR);
    assertThat(ChordMapper.INSTANCE.parse("C\u266F7/B\u266D").transpose(DiatonicInterval.THIRD, context)).isEqualTo(
        ChordMapper.INSTANCE.parse("E\u266F7/B\u266F"));
    context.setNormalizeChords(true);
    assertThat(ChordMapper.INSTANCE.parse("C\u266F7/Bb").transpose(DiatonicInterval.THIRD, context)).isEqualTo(
            ChordMapper.INSTANCE.parse("E\u266F7/C"));
  }

}
