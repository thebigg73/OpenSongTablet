package com.garethevans.church.opensongtablet.core.music.tone;

import org.junit.Test;

/**
 * Test of {@link TonePitchNeoLatinUnicodeSigns}.
 */
public class TonePitchNeoLatinUnicodeSignsTest extends AbstractTonePitchTest {

    protected ToneNameStyle<?> getNameStyle() {
        return TonePitchNeoLatinUnicodeSigns.STYLE;
    }

    @Test
    public void testReferences() {
        verifyReference(TonePitchNeoLatinUnicodeSigns.DO, "Do", ChromaticStep.S0, EnharmonicType.NORMAL, TonePitchNeoLatinUnicodeSigns.DO_FLAT, TonePitchNeoLatinUnicodeSigns.DO_SHARP);
        verifyReference(TonePitchNeoLatinUnicodeSigns.DO_SHARP, "Do\u266F", ChromaticStep.S1, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinUnicodeSigns.DO, TonePitchNeoLatinUnicodeSigns.DO_DOUBLE_SHARP);
        verifyReference(TonePitchNeoLatinUnicodeSigns.RE, "Re", ChromaticStep.S2, EnharmonicType.NORMAL, TonePitchNeoLatinUnicodeSigns.RE_FLAT, TonePitchNeoLatinUnicodeSigns.RE_SHARP);
        verifyReference(TonePitchNeoLatinUnicodeSigns.RE_SHARP, "Re\u266F", ChromaticStep.S3, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinUnicodeSigns.RE, TonePitchNeoLatinUnicodeSigns.RE_DOUBLE_SHARP);
        verifyReference(TonePitchNeoLatinUnicodeSigns.MI, "Mi", ChromaticStep.S4, EnharmonicType.NORMAL, TonePitchNeoLatinUnicodeSigns.MI_FLAT, TonePitchNeoLatinUnicodeSigns.MI_SHARP);
        verifyReference(TonePitchNeoLatinUnicodeSigns.FA, "Fa", ChromaticStep.S5, EnharmonicType.NORMAL, TonePitchNeoLatinUnicodeSigns.FA_FLAT, TonePitchNeoLatinUnicodeSigns.FA_SHARP);
        verifyReference(TonePitchNeoLatinUnicodeSigns.FA_SHARP, "Fa\u266F", ChromaticStep.S6, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinUnicodeSigns.FA, TonePitchNeoLatinUnicodeSigns.FA_DOUBLE_SHARP);
        verifyReference(TonePitchNeoLatinUnicodeSigns.SOL, "Sol", ChromaticStep.S7, EnharmonicType.NORMAL, TonePitchNeoLatinUnicodeSigns.SOL_FLAT, TonePitchNeoLatinUnicodeSigns.SOL_SHARP);
        verifyReference(TonePitchNeoLatinUnicodeSigns.SOL_SHARP, "Sol\u266F", ChromaticStep.S8, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinUnicodeSigns.SOL, TonePitchNeoLatinUnicodeSigns.SOL_DOUBLE_SHARP);
        verifyReference(TonePitchNeoLatinUnicodeSigns.LA, "La", ChromaticStep.S9, EnharmonicType.NORMAL, TonePitchNeoLatinUnicodeSigns.LA_FLAT, TonePitchNeoLatinUnicodeSigns.LA_SHARP);
        verifyReference(TonePitchNeoLatinUnicodeSigns.LA_SHARP, "La\u266F", ChromaticStep.S10, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinUnicodeSigns.LA, TonePitchNeoLatinUnicodeSigns.LA_DOUBLE_SHARP);
        verifyReference(TonePitchNeoLatinUnicodeSigns.SI, "Si", ChromaticStep.S11, EnharmonicType.NORMAL, TonePitchNeoLatinUnicodeSigns.SI_FLAT, TonePitchNeoLatinUnicodeSigns.SI_SHARP);
    }

    @Test
    public void testSingleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.MI_SHARP, "Mi\u266F", EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinUnicodeSigns.FA, TonePitchNeoLatinUnicodeSigns.MI, TonePitchNeoLatinUnicodeSigns.MI_DOUBLE_SHARP);
        // verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.LA_SHARP, "La\u266F", EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinUnicodeSigns.LA_SHARP, TonePitchNeoLatinUnicodeSigns.A, TonePitchNeoLatinUnicodeSigns.AISIS);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.SI_SHARP, "Si\u266F", EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinUnicodeSigns.DO, TonePitchNeoLatinUnicodeSigns.SI, TonePitchNeoLatinUnicodeSigns.SI_DOUBLE_SHARP);
    }

    @Test
    public void testSingleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.DO_FLAT, "Do\u266D", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinUnicodeSigns.SI, TonePitchNeoLatinUnicodeSigns.DO_DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.DO);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.RE_FLAT, "Re\u266D", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinUnicodeSigns.DO_SHARP, TonePitchNeoLatinUnicodeSigns.RE_DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.RE);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.MI_FLAT, "Mi\u266D", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinUnicodeSigns.RE_SHARP, TonePitchNeoLatinUnicodeSigns.MI_DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.MI);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.FA_FLAT, "Fa\u266D", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinUnicodeSigns.MI, TonePitchNeoLatinUnicodeSigns.FA_DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.FA);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.SOL_FLAT, "Sol\u266D", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinUnicodeSigns.FA_SHARP, TonePitchNeoLatinUnicodeSigns.SOL_DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.SOL);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.LA_FLAT, "La\u266D", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinUnicodeSigns.SOL_SHARP, TonePitchNeoLatinUnicodeSigns.LA_DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.LA);
    }

    @Test
    public void testDoubleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.DO_DOUBLE_SHARP, "Do\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinUnicodeSigns.RE, TonePitchNeoLatinUnicodeSigns.DO_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.RE_DOUBLE_SHARP, "Re\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinUnicodeSigns.MI, TonePitchNeoLatinUnicodeSigns.RE_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.MI_DOUBLE_SHARP, "Mi\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinUnicodeSigns.FA_SHARP, TonePitchNeoLatinUnicodeSigns.MI_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.FA_DOUBLE_SHARP, "Fa\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinUnicodeSigns.SOL, TonePitchNeoLatinUnicodeSigns.FA_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.SOL_DOUBLE_SHARP, "Sol\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinUnicodeSigns.LA, TonePitchNeoLatinUnicodeSigns.SOL_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.LA_DOUBLE_SHARP, "La\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinUnicodeSigns.SI, TonePitchNeoLatinUnicodeSigns.LA_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.SI_DOUBLE_SHARP, "Si\uD834\uDD2A", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinUnicodeSigns.DO_SHARP, TonePitchNeoLatinUnicodeSigns.SI_SHARP, null);
    }

    @Test
    public void testDoubleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.DO_DOUBLE_FLAT, "Do\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.LA_SHARP, null, TonePitchNeoLatinUnicodeSigns.DO_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.RE_DOUBLE_FLAT, "Re\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.DO, null, TonePitchNeoLatinUnicodeSigns.RE_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.MI_DOUBLE_FLAT, "Mi\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.RE, null, TonePitchNeoLatinUnicodeSigns.MI_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.FA_DOUBLE_FLAT, "Fa\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.RE_SHARP, null, TonePitchNeoLatinUnicodeSigns.FA_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.SOL_DOUBLE_FLAT, "Sol\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.FA, null, TonePitchNeoLatinUnicodeSigns.SOL_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.LA_DOUBLE_FLAT, "La\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.SOL, null, TonePitchNeoLatinUnicodeSigns.LA_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinUnicodeSigns.SI_DOUBLE_FLAT, "Si\uD834\uDD2B", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinUnicodeSigns.LA, null, TonePitchNeoLatinUnicodeSigns.SI_FLAT);
    }

}
