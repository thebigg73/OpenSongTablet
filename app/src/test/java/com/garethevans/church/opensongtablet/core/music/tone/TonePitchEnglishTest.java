package com.garethevans.church.opensongtablet.core.music.tone;

import org.junit.Test;

/**
 * Test of {@link TonePitchEnglish}.
 */
public class TonePitchEnglishTest extends AbstractTonePitchTest {

    protected ToneNameStyle<?> getNameStyle() {
        return TonePitchEnglish.STYLE;
    }

    @Test
    public void testReferences() {
        verifyReference(TonePitchEnglish.C, "C", ChromaticStep.S0, EnharmonicType.NORMAL, TonePitchEnglish.C_FLAT, TonePitchEnglish.C_SHARP);
        verifyReference(TonePitchEnglish.C_SHARP, "C#", ChromaticStep.S1, EnharmonicType.SINGLE_SHARP, TonePitchEnglish.C, TonePitchEnglish.C_DOUBLE_SHARP);
        verifyReference(TonePitchEnglish.D, "D", ChromaticStep.S2, EnharmonicType.NORMAL, TonePitchEnglish.D_FLAT, TonePitchEnglish.D_SHARP);
        verifyReference(TonePitchEnglish.D_SHARP, "D#", ChromaticStep.S3, EnharmonicType.SINGLE_SHARP, TonePitchEnglish.D, TonePitchEnglish.D_DOUBLE_SHARP);
        verifyReference(TonePitchEnglish.E, "E", ChromaticStep.S4, EnharmonicType.NORMAL, TonePitchEnglish.E_FLAT, TonePitchEnglish.E_SHARP);
        verifyReference(TonePitchEnglish.F, "F", ChromaticStep.S5, EnharmonicType.NORMAL, TonePitchEnglish.F_FLAT, TonePitchEnglish.F_SHARP);
        verifyReference(TonePitchEnglish.F_SHARP, "F#", ChromaticStep.S6, EnharmonicType.SINGLE_SHARP, TonePitchEnglish.F, TonePitchEnglish.F_DOUBLE_SHARP);
        verifyReference(TonePitchEnglish.G, "G", ChromaticStep.S7, EnharmonicType.NORMAL, TonePitchEnglish.G_FLAT, TonePitchEnglish.G_SHARP);
        verifyReference(TonePitchEnglish.G_SHARP, "G#", ChromaticStep.S8, EnharmonicType.SINGLE_SHARP, TonePitchEnglish.G, TonePitchEnglish.G_DOUBLE_SHARP);
        verifyReference(TonePitchEnglish.A, "A", ChromaticStep.S9, EnharmonicType.NORMAL, TonePitchEnglish.A_FLAT, TonePitchEnglish.A_SHARP);
        verifyReference(TonePitchEnglish.B_FLAT, "Bb", ChromaticStep.S10, EnharmonicType.SINGLE_FLAT, TonePitchEnglish.B_DOUBLE_FLAT, TonePitchEnglish.B);
        verifyReference(TonePitchEnglish.B, "B", ChromaticStep.S11, EnharmonicType.NORMAL, TonePitchEnglish.B_FLAT, TonePitchEnglish.B_SHARP);
    }

    @Test
    public void testSingleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchEnglish.E_SHARP, "E#", EnharmonicType.SINGLE_SHARP, TonePitchEnglish.F, TonePitchEnglish.E, TonePitchEnglish.E_DOUBLE_SHARP);
        verifyEnharmonicChange(TonePitchEnglish.A_SHARP, "A#", EnharmonicType.SINGLE_SHARP, TonePitchEnglish.B_FLAT, TonePitchEnglish.A, TonePitchEnglish.A_DOUBLE_SHARP);
        verifyEnharmonicChange(TonePitchEnglish.B_SHARP, "B#", EnharmonicType.SINGLE_SHARP, TonePitchEnglish.C, TonePitchEnglish.B, TonePitchEnglish.B_DOUBLE_SHARP);
    }

    @Test
    public void testSingleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchEnglish.C_FLAT, "Cb", EnharmonicType.SINGLE_FLAT, TonePitchEnglish.B, TonePitchEnglish.C_DOUBLE_FLAT, TonePitchEnglish.C);
        verifyEnharmonicChange(TonePitchEnglish.D_FLAT, "Db", EnharmonicType.SINGLE_FLAT, TonePitchEnglish.C_SHARP, TonePitchEnglish.D_DOUBLE_FLAT, TonePitchEnglish.D);
        verifyEnharmonicChange(TonePitchEnglish.E_FLAT, "Eb", EnharmonicType.SINGLE_FLAT, TonePitchEnglish.D_SHARP, TonePitchEnglish.E_DOUBLE_FLAT, TonePitchEnglish.E);
        verifyEnharmonicChange(TonePitchEnglish.F_FLAT, "Fb", EnharmonicType.SINGLE_FLAT, TonePitchEnglish.E, TonePitchEnglish.F_DOUBLE_FLAT, TonePitchEnglish.F);
        verifyEnharmonicChange(TonePitchEnglish.G_FLAT, "Gb", EnharmonicType.SINGLE_FLAT, TonePitchEnglish.F_SHARP, TonePitchEnglish.G_DOUBLE_FLAT, TonePitchEnglish.G);
        verifyEnharmonicChange(TonePitchEnglish.A_FLAT, "Ab", EnharmonicType.SINGLE_FLAT, TonePitchEnglish.G_SHARP, TonePitchEnglish.A_DOUBLE_FLAT, TonePitchEnglish.A);
    }

    @Test
    public void testDoubleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchEnglish.C_DOUBLE_SHARP, "C##", EnharmonicType.DOUBLE_SHARP, TonePitchEnglish.D, TonePitchEnglish.C_SHARP, null);
        verifyEnharmonicChange(TonePitchEnglish.D_DOUBLE_SHARP, "D##", EnharmonicType.DOUBLE_SHARP, TonePitchEnglish.E, TonePitchEnglish.D_SHARP, null);
        verifyEnharmonicChange(TonePitchEnglish.E_DOUBLE_SHARP, "E##", EnharmonicType.DOUBLE_SHARP, TonePitchEnglish.F_SHARP, TonePitchEnglish.E_SHARP, null);
        verifyEnharmonicChange(TonePitchEnglish.F_DOUBLE_SHARP, "F##", EnharmonicType.DOUBLE_SHARP, TonePitchEnglish.G, TonePitchEnglish.F_SHARP, null);
        verifyEnharmonicChange(TonePitchEnglish.G_DOUBLE_SHARP, "G##", EnharmonicType.DOUBLE_SHARP, TonePitchEnglish.A, TonePitchEnglish.G_SHARP, null);
        verifyEnharmonicChange(TonePitchEnglish.A_DOUBLE_SHARP, "A##", EnharmonicType.DOUBLE_SHARP, TonePitchEnglish.B, TonePitchEnglish.A_SHARP, null);
        verifyEnharmonicChange(TonePitchEnglish.B_DOUBLE_SHARP, "B##", EnharmonicType.DOUBLE_SHARP, TonePitchEnglish.C_SHARP, TonePitchEnglish.B_SHARP, null);
    }

    @Test
    public void testDoubleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchEnglish.C_DOUBLE_FLAT, "Cbb", EnharmonicType.DOUBLE_FLAT, TonePitchEnglish.B_FLAT, null, TonePitchEnglish.C_FLAT);
        verifyEnharmonicChange(TonePitchEnglish.D_DOUBLE_FLAT, "Dbb", EnharmonicType.DOUBLE_FLAT, TonePitchEnglish.C, null, TonePitchEnglish.D_FLAT);
        verifyEnharmonicChange(TonePitchEnglish.E_DOUBLE_FLAT, "Ebb", EnharmonicType.DOUBLE_FLAT, TonePitchEnglish.D, null, TonePitchEnglish.E_FLAT);
        verifyEnharmonicChange(TonePitchEnglish.F_DOUBLE_FLAT, "Fbb", EnharmonicType.DOUBLE_FLAT, TonePitchEnglish.D_SHARP, null, TonePitchEnglish.F_FLAT);
        verifyEnharmonicChange(TonePitchEnglish.G_DOUBLE_FLAT, "Gbb", EnharmonicType.DOUBLE_FLAT, TonePitchEnglish.F, null, TonePitchEnglish.G_FLAT);
        verifyEnharmonicChange(TonePitchEnglish.A_DOUBLE_FLAT, "Abb", EnharmonicType.DOUBLE_FLAT, TonePitchEnglish.G, null, TonePitchEnglish.A_FLAT);
        verifyEnharmonicChange(TonePitchEnglish.B_DOUBLE_FLAT, "Bbb", EnharmonicType.DOUBLE_FLAT, TonePitchEnglish.A, null, TonePitchEnglish.B_FLAT);
    }

}
