package com.garethevans.church.opensongtablet.core.music.tone;

import org.junit.Test;

/**
 * Test of {@link TonePitchNeoLatin}.
 */
public class TonePitchNeoLatinTest extends AbstractTonePitchTest {

    protected ToneNameStyle<?> getNameStyle() {
        return TonePitchNeoLatin.STYLE;
    }

    @Test
    public void testReferences() {
        verifyReference(TonePitchNeoLatin.DO, "Do", ChromaticStep.S0, EnharmonicType.NORMAL, TonePitchNeoLatin.DO_BEMOLLE, TonePitchNeoLatin.DO_DIESIS);
        verifyReference(TonePitchNeoLatin.DO_DIESIS, "Do diesis", ChromaticStep.S1, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatin.DO, TonePitchNeoLatin.DO_DOPPIO_DIESIS);
        verifyReference(TonePitchNeoLatin.RE, "Re", ChromaticStep.S2, EnharmonicType.NORMAL, TonePitchNeoLatin.RE_BEMOLLE, TonePitchNeoLatin.RE_DIESIS);
        verifyReference(TonePitchNeoLatin.RE_DIESIS, "Re diesis", ChromaticStep.S3, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatin.RE, TonePitchNeoLatin.RE_DOPPIO_DIESIS);
        verifyReference(TonePitchNeoLatin.MI, "Mi", ChromaticStep.S4, EnharmonicType.NORMAL, TonePitchNeoLatin.MI_BEMOLLE, TonePitchNeoLatin.MI_DIESIS);
        verifyReference(TonePitchNeoLatin.FA, "Fa", ChromaticStep.S5, EnharmonicType.NORMAL, TonePitchNeoLatin.FA_BEMOLLE, TonePitchNeoLatin.FA_DIESIS);
        verifyReference(TonePitchNeoLatin.FA_DIESIS, "Fa diesis", ChromaticStep.S6, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatin.FA, TonePitchNeoLatin.FA_DOPPIO_DIESIS);
        verifyReference(TonePitchNeoLatin.SOL, "Sol", ChromaticStep.S7, EnharmonicType.NORMAL, TonePitchNeoLatin.SOL_BEMOLLE, TonePitchNeoLatin.SOL_DIESIS);
        verifyReference(TonePitchNeoLatin.SOL_DIESIS, "Sol diesis", ChromaticStep.S8, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatin.SOL, TonePitchNeoLatin.SOL_DOPPIO_DIESIS);
        verifyReference(TonePitchNeoLatin.LA, "La", ChromaticStep.S9, EnharmonicType.NORMAL, TonePitchNeoLatin.LA_BEMOLLE, TonePitchNeoLatin.LA_DIESIS);
        verifyReference(TonePitchNeoLatin.LA_DIESIS, "La diesis", ChromaticStep.S10, EnharmonicType.SINGLE_SHARP, TonePitchNeoLatin.LA, TonePitchNeoLatin.LA_DOPPIO_DIESIS);
        verifyReference(TonePitchNeoLatin.SI, "Si", ChromaticStep.S11, EnharmonicType.NORMAL, TonePitchNeoLatin.SI_BEMOLLE, TonePitchNeoLatin.SI_DIESIS);
    }

    @Test
    public void testSingleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatin.MI_DIESIS, "Mi diesis", EnharmonicType.SINGLE_SHARP, TonePitchNeoLatin.FA, TonePitchNeoLatin.MI, TonePitchNeoLatin.MI_DOPPIO_DIESIS);
        // verifyEnharmonicChange(TonePitchNeoLatin.LA_DIESIS, "La diesis", EnharmonicType.SINGLE_SHARP, TonePitchNeoLatin.LA_DIESIS, TonePitchNeoLatin.A, TonePitchNeoLatin.AISIS);
        verifyEnharmonicChange(TonePitchNeoLatin.SI_DIESIS, "Si diesis", EnharmonicType.SINGLE_SHARP, TonePitchNeoLatin.DO, TonePitchNeoLatin.SI, TonePitchNeoLatin.SI_DOPPIO_DIESIS);
    }

    @Test
    public void testSingleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatin.DO_BEMOLLE, "Do bemolle", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatin.SI, TonePitchNeoLatin.DO_DOPPIO_BEMOLLE, TonePitchNeoLatin.DO);
        verifyEnharmonicChange(TonePitchNeoLatin.RE_BEMOLLE, "Re bemolle", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatin.DO_DIESIS, TonePitchNeoLatin.RE_DOPPIO_BEMOLLE, TonePitchNeoLatin.RE);
        verifyEnharmonicChange(TonePitchNeoLatin.MI_BEMOLLE, "Mi bemolle", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatin.RE_DIESIS, TonePitchNeoLatin.MI_DOPPIO_BEMOLLE, TonePitchNeoLatin.MI);
        verifyEnharmonicChange(TonePitchNeoLatin.FA_BEMOLLE, "Fa bemolle", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatin.MI, TonePitchNeoLatin.FA_DOPPIO_BEMOLLE, TonePitchNeoLatin.FA);
        verifyEnharmonicChange(TonePitchNeoLatin.SOL_BEMOLLE, "Sol bemolle", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatin.FA_DIESIS, TonePitchNeoLatin.SOL_DOPPIO_BEMOLLE, TonePitchNeoLatin.SOL);
        verifyEnharmonicChange(TonePitchNeoLatin.LA_BEMOLLE, "La bemolle", EnharmonicType.SINGLE_FLAT, TonePitchNeoLatin.SOL_DIESIS, TonePitchNeoLatin.LA_DOPPIO_BEMOLLE, TonePitchNeoLatin.LA);
    }

    @Test
    public void testDoubleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatin.DO_DOPPIO_DIESIS, "Do doppio diesis", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatin.RE, TonePitchNeoLatin.DO_DIESIS, null);
        verifyEnharmonicChange(TonePitchNeoLatin.RE_DOPPIO_DIESIS, "Re doppio diesis", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatin.MI, TonePitchNeoLatin.RE_DIESIS, null);
        verifyEnharmonicChange(TonePitchNeoLatin.MI_DOPPIO_DIESIS, "Mi doppio diesis", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatin.FA_DIESIS, TonePitchNeoLatin.MI_DIESIS, null);
        verifyEnharmonicChange(TonePitchNeoLatin.FA_DOPPIO_DIESIS, "Fa doppio diesis", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatin.SOL, TonePitchNeoLatin.FA_DIESIS, null);
        verifyEnharmonicChange(TonePitchNeoLatin.SOL_DOPPIO_DIESIS, "Sol doppio diesis", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatin.LA, TonePitchNeoLatin.SOL_DIESIS, null);
        verifyEnharmonicChange(TonePitchNeoLatin.LA_DOPPIO_DIESIS, "La doppio diesis", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatin.SI, TonePitchNeoLatin.LA_DIESIS, null);
        verifyEnharmonicChange(TonePitchNeoLatin.SI_DOPPIO_DIESIS, "Si doppio diesis", EnharmonicType.DOUBLE_SHARP, TonePitchNeoLatin.DO_DIESIS, TonePitchNeoLatin.SI_DIESIS, null);
    }

    @Test
    public void testDoubleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchNeoLatin.DO_DOPPIO_BEMOLLE, "Do doppio bemolle", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatin.LA_DIESIS, null, TonePitchNeoLatin.DO_BEMOLLE);
        verifyEnharmonicChange(TonePitchNeoLatin.RE_DOPPIO_BEMOLLE, "Re doppio bemolle", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatin.DO, null, TonePitchNeoLatin.RE_BEMOLLE);
        verifyEnharmonicChange(TonePitchNeoLatin.MI_DOPPIO_BEMOLLE, "Mi doppio bemolle", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatin.RE, null, TonePitchNeoLatin.MI_BEMOLLE);
        verifyEnharmonicChange(TonePitchNeoLatin.FA_DOPPIO_BEMOLLE, "Fa doppio bemolle", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatin.RE_DIESIS, null, TonePitchNeoLatin.FA_BEMOLLE);
        verifyEnharmonicChange(TonePitchNeoLatin.SOL_DOPPIO_BEMOLLE, "Sol doppio bemolle", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatin.FA, null, TonePitchNeoLatin.SOL_BEMOLLE);
        verifyEnharmonicChange(TonePitchNeoLatin.LA_DOPPIO_BEMOLLE, "La doppio bemolle", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatin.SOL, null, TonePitchNeoLatin.LA_BEMOLLE);
        verifyEnharmonicChange(TonePitchNeoLatin.SI_DOPPIO_BEMOLLE, "Si doppio bemolle", EnharmonicType.DOUBLE_FLAT, TonePitchNeoLatin.LA, null, TonePitchNeoLatin.SI_BEMOLLE);
    }

}
