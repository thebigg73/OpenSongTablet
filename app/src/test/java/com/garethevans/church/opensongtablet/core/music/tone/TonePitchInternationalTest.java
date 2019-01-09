package com.garethevans.church.opensongtablet.core.music.tone;

import org.junit.Test;

/**
 * Test of {@link TonePitchInternational}.
 */
public class TonePitchInternationalTest extends AbstractTonePitchTest {

    protected ToneNameStyle<?> getNameStyle() {
        return TonePitchInternational.STYLE;
    }

    @Test
    public void testReferences() {
        verifyReference(TonePitchInternational.C, "C", ChromaticStep.S0, EnharmonicType.NORMAL, TonePitchInternational.C_FLAT, TonePitchInternational.C_SHARP);
        verifyReference(TonePitchInternational.C_SHARP, "C\u266F", ChromaticStep.S1, EnharmonicType.SINGLE_SHARP, TonePitchInternational.C, TonePitchInternational.C_DOUBLE_SHARP);
        verifyReference(TonePitchInternational.D, "D", ChromaticStep.S2, EnharmonicType.NORMAL, TonePitchInternational.D_FLAT, TonePitchInternational.D_SHARP);
        verifyReference(TonePitchInternational.D_SHARP, "D\u266F", ChromaticStep.S3, EnharmonicType.SINGLE_SHARP, TonePitchInternational.D, TonePitchInternational.D_DOUBLE_SHARP);
        verifyReference(TonePitchInternational.E, "E", ChromaticStep.S4, EnharmonicType.NORMAL, TonePitchInternational.E_FLAT, TonePitchInternational.E_SHARP);
        verifyReference(TonePitchInternational.F, "F", ChromaticStep.S5, EnharmonicType.NORMAL, TonePitchInternational.F_FLAT, TonePitchInternational.F_SHARP);
        verifyReference(TonePitchInternational.F_SHARP, "F\u266F", ChromaticStep.S6, EnharmonicType.SINGLE_SHARP, TonePitchInternational.F, TonePitchInternational.F_DOUBLE_SHARP);
        verifyReference(TonePitchInternational.G, "G", ChromaticStep.S7, EnharmonicType.NORMAL, TonePitchInternational.G_FLAT, TonePitchInternational.G_SHARP);
        verifyReference(TonePitchInternational.G_SHARP, "G\u266F", ChromaticStep.S8, EnharmonicType.SINGLE_SHARP, TonePitchInternational.G, TonePitchInternational.G_DOUBLE_SHARP);
        verifyReference(TonePitchInternational.A, "A", ChromaticStep.S9, EnharmonicType.NORMAL, TonePitchInternational.A_FLAT, TonePitchInternational.A_SHARP);
        verifyReference(TonePitchInternational.B_FLAT, "B\u266D", ChromaticStep.S10, EnharmonicType.SINGLE_FLAT, TonePitchInternational.B_DOUBLE_FLAT, TonePitchInternational.B_NEUTRAL);
        verifyReference(TonePitchInternational.B_NEUTRAL, "B\u266E", ChromaticStep.S11, EnharmonicType.NORMAL, TonePitchInternational.B_FLAT, TonePitchInternational.B_SHARP);
    }

    @Test
    public void testSingleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchInternational.E_SHARP, "E\u266F", EnharmonicType.SINGLE_SHARP, TonePitchInternational.F, TonePitchInternational.E, TonePitchInternational.E_DOUBLE_SHARP);
        verifyEnharmonicChange(TonePitchInternational.A_SHARP, "A\u266F", EnharmonicType.SINGLE_SHARP, TonePitchInternational.B_FLAT, TonePitchInternational.A, TonePitchInternational.A_DOUBLE_SHARP);
        verifyEnharmonicChange(TonePitchInternational.B_SHARP, "B\u266F", EnharmonicType.SINGLE_SHARP, TonePitchInternational.C, TonePitchInternational.B_NEUTRAL, TonePitchInternational.B_DOUBLE_SHARP);
    }

    @Test
    public void testSingleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchInternational.C_FLAT, "C\u266D", EnharmonicType.SINGLE_FLAT, TonePitchInternational.B_NEUTRAL, TonePitchInternational.C_DOUBLE_FLAT, TonePitchInternational.C);
        verifyEnharmonicChange(TonePitchInternational.D_FLAT, "D\u266D", EnharmonicType.SINGLE_FLAT, TonePitchInternational.C_SHARP, TonePitchInternational.D_DOUBLE_FLAT, TonePitchInternational.D);
        verifyEnharmonicChange(TonePitchInternational.E_FLAT, "E\u266D", EnharmonicType.SINGLE_FLAT, TonePitchInternational.D_SHARP, TonePitchInternational.E_DOUBLE_FLAT, TonePitchInternational.E);
        verifyEnharmonicChange(TonePitchInternational.F_FLAT, "F\u266D", EnharmonicType.SINGLE_FLAT, TonePitchInternational.E, TonePitchInternational.F_DOUBLE_FLAT, TonePitchInternational.F);
        verifyEnharmonicChange(TonePitchInternational.G_FLAT, "G\u266D", EnharmonicType.SINGLE_FLAT, TonePitchInternational.F_SHARP, TonePitchInternational.G_DOUBLE_FLAT, TonePitchInternational.G);
        verifyEnharmonicChange(TonePitchInternational.A_FLAT, "A\u266D", EnharmonicType.SINGLE_FLAT, TonePitchInternational.G_SHARP, TonePitchInternational.A_DOUBLE_FLAT, TonePitchInternational.A);
    }

    @Test
    public void testDoubleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchInternational.C_DOUBLE_SHARP, "C\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchInternational.D, TonePitchInternational.C_SHARP, null);
        verifyEnharmonicChange(TonePitchInternational.D_DOUBLE_SHARP, "D\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchInternational.E, TonePitchInternational.D_SHARP, null);
        verifyEnharmonicChange(TonePitchInternational.E_DOUBLE_SHARP, "E\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchInternational.F_SHARP, TonePitchInternational.E_SHARP, null);
        verifyEnharmonicChange(TonePitchInternational.F_DOUBLE_SHARP, "F\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchInternational.G, TonePitchInternational.F_SHARP, null);
        verifyEnharmonicChange(TonePitchInternational.G_DOUBLE_SHARP, "G\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchInternational.A, TonePitchInternational.G_SHARP, null);
        verifyEnharmonicChange(TonePitchInternational.A_DOUBLE_SHARP, "A\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchInternational.B_NEUTRAL, TonePitchInternational.A_SHARP, null);
        verifyEnharmonicChange(TonePitchInternational.B_DOUBLE_SHARP, "B\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchInternational.C_SHARP, TonePitchInternational.B_SHARP, null);
    }

    @Test
    public void testDoubleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchInternational.C_DOUBLE_FLAT, "C\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchInternational.B_FLAT, null, TonePitchInternational.C_FLAT);
        verifyEnharmonicChange(TonePitchInternational.D_DOUBLE_FLAT, "D\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchInternational.C, null, TonePitchInternational.D_FLAT);
        verifyEnharmonicChange(TonePitchInternational.E_DOUBLE_FLAT, "E\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchInternational.D, null, TonePitchInternational.E_FLAT);
        verifyEnharmonicChange(TonePitchInternational.F_DOUBLE_FLAT, "F\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchInternational.D_SHARP, null, TonePitchInternational.F_FLAT);
        verifyEnharmonicChange(TonePitchInternational.G_DOUBLE_FLAT, "G\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchInternational.F, null, TonePitchInternational.G_FLAT);
        verifyEnharmonicChange(TonePitchInternational.A_DOUBLE_FLAT, "A\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchInternational.G, null, TonePitchInternational.A_FLAT);
        verifyEnharmonicChange(TonePitchInternational.B_DOUBLE_FLAT, "B\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchInternational.A, null, TonePitchInternational.B_FLAT);
    }

}
