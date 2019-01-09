package com.garethevans.church.opensongtablet.core.music.tone;

import org.junit.Test;

/**
 * Test of {@link TonePitchNeoLatinAsciiSigns}.
 */
public class TonePitchNeoLatinAsciiSignsTest extends AbstractTonePitchTest {

    protected ToneNameStyle<?> getNameStyle() {
        return TonePitchNeoLatinAsciiSigns.STYLE;
    }

    @Test
    public void testReferences() {
        verifyReference(TonePitchNeoLatinAsciiSigns.DO, "Do", ChromaticStep.S0, EnharmonicType.NORMAL, TonePitchNeoLatinAsciiSigns.DO_FLAT, TonePitchNeoLatinAsciiSigns.DO_SHARP);
        verifyReference(TonePitchNeoLatinAsciiSigns.DO_SHARP, "Do#", ChromaticStep.S1, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinAsciiSigns.DO, TonePitchNeoLatinAsciiSigns.DO_DOUBLE_SHARP);
        verifyReference(TonePitchNeoLatinAsciiSigns.RE, "Re", ChromaticStep.S2, EnharmonicType.NORMAL, TonePitchNeoLatinAsciiSigns.RE_FLAT, TonePitchNeoLatinAsciiSigns.RE_SHARP);
        verifyReference(TonePitchNeoLatinAsciiSigns.RE_SHARP, "Re#", ChromaticStep.S3, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinAsciiSigns.RE, TonePitchNeoLatinAsciiSigns.RE_DOUBLE_SHARP);
        verifyReference(TonePitchNeoLatinAsciiSigns.MI, "Mi", ChromaticStep.S4, EnharmonicType.NORMAL, TonePitchNeoLatinAsciiSigns.MI_FLAT, TonePitchNeoLatinAsciiSigns.MI_SHARP);
        verifyReference(TonePitchNeoLatinAsciiSigns.FA, "Fa", ChromaticStep.S5, EnharmonicType.NORMAL, TonePitchNeoLatinAsciiSigns.FA_FLAT, TonePitchNeoLatinAsciiSigns.FA_SHARP);
        verifyReference(TonePitchNeoLatinAsciiSigns.FA_SHARP, "Fa#", ChromaticStep.S6, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinAsciiSigns.FA, TonePitchNeoLatinAsciiSigns.FA_DOUBLE_SHARP);
        verifyReference(TonePitchNeoLatinAsciiSigns.SOL, "Sol", ChromaticStep.S7, EnharmonicType.NORMAL, TonePitchNeoLatinAsciiSigns.SOL_FLAT, TonePitchNeoLatinAsciiSigns.SOL_SHARP);
        verifyReference(TonePitchNeoLatinAsciiSigns.SOL_SHARP, "Sol#", ChromaticStep.S8, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinAsciiSigns.SOL, TonePitchNeoLatinAsciiSigns.SOL_DOUBLE_SHARP);
        verifyReference(TonePitchNeoLatinAsciiSigns.LA, "La", ChromaticStep.S9, EnharmonicType.NORMAL, TonePitchNeoLatinAsciiSigns.LA_FLAT, TonePitchNeoLatinAsciiSigns.LA_SHARP);
        verifyReference(TonePitchNeoLatinAsciiSigns.LA_SHARP, "La#", ChromaticStep.S10, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinAsciiSigns.LA, TonePitchNeoLatinAsciiSigns.LA_DOUBLE_SHARP);
        verifyReference(TonePitchNeoLatinAsciiSigns.SI, "Si", ChromaticStep.S11, EnharmonicType.NORMAL, TonePitchNeoLatinAsciiSigns.SI_FLAT, TonePitchNeoLatinAsciiSigns.SI_SHARP);
    }

    @Test
    public void testSingleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.MI_SHARP, "Mi#", EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinAsciiSigns.FA, TonePitchNeoLatinAsciiSigns.MI, TonePitchNeoLatinAsciiSigns.MI_DOUBLE_SHARP);
        // verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.LA_SHARP, "La#", EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinAsciiSigns.LA_SHARP, TonePitchNeoLatinAsciiSigns.A, TonePitchNeoLatinAsciiSigns.AISIS);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.SI_SHARP, "Si#", EnharmonicType.SINGLE_SHARP, TonePitchNeoLatinAsciiSigns.DO, TonePitchNeoLatinAsciiSigns.SI, TonePitchNeoLatinAsciiSigns.SI_DOUBLE_SHARP);
    }

    @Test
    public void testSingleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.DO_FLAT, "Dob", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinAsciiSigns.SI, TonePitchNeoLatinAsciiSigns.DO_DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.DO);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.RE_FLAT, "Reb", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinAsciiSigns.DO_SHARP, TonePitchNeoLatinAsciiSigns.RE_DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.RE);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.MI_FLAT, "Mib", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinAsciiSigns.RE_SHARP, TonePitchNeoLatinAsciiSigns.MI_DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.MI);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.FA_FLAT, "Fab", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinAsciiSigns.MI, TonePitchNeoLatinAsciiSigns.FA_DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.FA);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.SOL_FLAT, "Solb", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinAsciiSigns.FA_SHARP, TonePitchNeoLatinAsciiSigns.SOL_DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.SOL);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.LA_FLAT, "Lab", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatinAsciiSigns.SOL_SHARP, TonePitchNeoLatinAsciiSigns.LA_DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.LA);
    }

    @Test
    public void testDoubleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.DO_DOUBLE_SHARP, "Do##", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinAsciiSigns.RE, TonePitchNeoLatinAsciiSigns.DO_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.RE_DOUBLE_SHARP, "Re##", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinAsciiSigns.MI, TonePitchNeoLatinAsciiSigns.RE_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.MI_DOUBLE_SHARP, "Mi##", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinAsciiSigns.FA_SHARP, TonePitchNeoLatinAsciiSigns.MI_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.FA_DOUBLE_SHARP, "Fa##", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinAsciiSigns.SOL, TonePitchNeoLatinAsciiSigns.FA_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.SOL_DOUBLE_SHARP, "Sol##", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinAsciiSigns.LA, TonePitchNeoLatinAsciiSigns.SOL_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.LA_DOUBLE_SHARP, "La##", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinAsciiSigns.SI, TonePitchNeoLatinAsciiSigns.LA_SHARP, null);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.SI_DOUBLE_SHARP, "Si##", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatinAsciiSigns.DO_SHARP, TonePitchNeoLatinAsciiSigns.SI_SHARP, null);
    }

    @Test
    public void testDoubleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.DO_DOUBLE_FLAT, "Dobb", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.LA_SHARP, null, TonePitchNeoLatinAsciiSigns.DO_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.RE_DOUBLE_FLAT, "Rebb", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.DO, null, TonePitchNeoLatinAsciiSigns.RE_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.MI_DOUBLE_FLAT, "Mibb", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.RE, null, TonePitchNeoLatinAsciiSigns.MI_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.FA_DOUBLE_FLAT, "Fabb", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.RE_SHARP, null, TonePitchNeoLatinAsciiSigns.FA_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.SOL_DOUBLE_FLAT, "Solbb", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.FA, null, TonePitchNeoLatinAsciiSigns.SOL_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.LA_DOUBLE_FLAT, "Labb", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.SOL, null, TonePitchNeoLatinAsciiSigns.LA_FLAT);
        verifyEnharmonicChange(TonePitchNeoLatinAsciiSigns.SI_DOUBLE_FLAT, "Sibb", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatinAsciiSigns.LA, null, TonePitchNeoLatinAsciiSigns.SI_FLAT);
    }

}
