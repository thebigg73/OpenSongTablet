package com.garethevans.church.opensongtablet.core.music.tone;

import org.junit.Test;

/**
 * Test of {@link TonePitchDutch}.
 */
public class TonePitchDutchTest extends AbstractTonePitchTest {

    protected ToneNameStyle<?> getNameStyle() {
        return TonePitchDutch.STYLE;
    }

    @Test
    public void testReferences() {
        verifyReference(TonePitchDutch.C, "C", ChromaticStep.S0, EnharmonicType.NORMAL, TonePitchDutch.CES, TonePitchDutch.CIS);
        verifyReference(TonePitchDutch.CIS, "Cis", ChromaticStep.S1, EnharmonicType.SINGLE_SHARP, TonePitchDutch.C, TonePitchDutch.CISIS);
        verifyReference(TonePitchDutch.D, "D", ChromaticStep.S2, EnharmonicType.NORMAL, TonePitchDutch.DES, TonePitchDutch.DIS);
        verifyReference(TonePitchDutch.DIS, "Dis", ChromaticStep.S3, EnharmonicType.SINGLE_SHARP, TonePitchDutch.D, TonePitchDutch.DISIS);
        verifyReference(TonePitchDutch.E, "E", ChromaticStep.S4, EnharmonicType.NORMAL, TonePitchDutch.ES, TonePitchDutch.EIS);
        verifyReference(TonePitchDutch.F, "F", ChromaticStep.S5, EnharmonicType.NORMAL, TonePitchDutch.FES, TonePitchDutch.FIS);
        verifyReference(TonePitchDutch.FIS, "Fis", ChromaticStep.S6, EnharmonicType.SINGLE_SHARP, TonePitchDutch.F, TonePitchDutch.FISIS);
        verifyReference(TonePitchDutch.G, "G", ChromaticStep.S7, EnharmonicType.NORMAL, TonePitchDutch.GES, TonePitchDutch.GIS);
        verifyReference(TonePitchDutch.GIS, "Gis", ChromaticStep.S8, EnharmonicType.SINGLE_SHARP, TonePitchDutch.G, TonePitchDutch.GISIS);
        verifyReference(TonePitchDutch.A, "A", ChromaticStep.S9, EnharmonicType.NORMAL, TonePitchDutch.AS, TonePitchDutch.AIS);
        verifyReference(TonePitchDutch.BES, "Bes", ChromaticStep.S10, EnharmonicType.SINGLE_FLAT, TonePitchDutch.BESES, TonePitchDutch.B);
        verifyReference(TonePitchDutch.B, "B", ChromaticStep.S11, EnharmonicType.NORMAL, TonePitchDutch.BES, TonePitchDutch.BIS);
    }

    @Test
    public void testSingleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchDutch.EIS, "Eis", EnharmonicType.SINGLE_SHARP, TonePitchDutch.F, TonePitchDutch.E, TonePitchDutch.EISIS);
        verifyEnharmonicChange(TonePitchDutch.AIS, "Ais", EnharmonicType.SINGLE_SHARP, TonePitchDutch.BES, TonePitchDutch.A, TonePitchDutch.AISIS);
        verifyEnharmonicChange(TonePitchDutch.BIS, "Bis", EnharmonicType.SINGLE_SHARP, TonePitchDutch.C, TonePitchDutch.B, TonePitchDutch.BISIS);
    }

    @Test
    public void testSingleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchDutch.CES, "Ces", EnharmonicType.SINGLE_FLAT, TonePitchDutch.B, TonePitchDutch.CESES, TonePitchDutch.C);
        verifyEnharmonicChange(TonePitchDutch.DES, "Des", EnharmonicType.SINGLE_FLAT, TonePitchDutch.CIS, TonePitchDutch.DESES, TonePitchDutch.D);
        verifyEnharmonicChange(TonePitchDutch.ES, "Es", EnharmonicType.SINGLE_FLAT, TonePitchDutch.DIS, TonePitchDutch.ESES, TonePitchDutch.E);
        verifyEnharmonicChange(TonePitchDutch.FES, "Fes", EnharmonicType.SINGLE_FLAT, TonePitchDutch.E, TonePitchDutch.FESES, TonePitchDutch.F);
        verifyEnharmonicChange(TonePitchDutch.GES, "Ges", EnharmonicType.SINGLE_FLAT, TonePitchDutch.FIS, TonePitchDutch.GESES, TonePitchDutch.G);
        verifyEnharmonicChange(TonePitchDutch.AS, "As", EnharmonicType.SINGLE_FLAT, TonePitchDutch.GIS, TonePitchDutch.ASES, TonePitchDutch.A);
    }

    @Test
    public void testDoubleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchDutch.CISIS, "Cisis", EnharmonicType.DOUBLE_SHARP, TonePitchDutch.D, TonePitchDutch.CIS, null);
        verifyEnharmonicChange(TonePitchDutch.DISIS, "Disis", EnharmonicType.DOUBLE_SHARP, TonePitchDutch.E, TonePitchDutch.DIS, null);
        verifyEnharmonicChange(TonePitchDutch.EISIS, "Eisis", EnharmonicType.DOUBLE_SHARP, TonePitchDutch.FIS, TonePitchDutch.EIS, null);
        verifyEnharmonicChange(TonePitchDutch.FISIS, "Fisis", EnharmonicType.DOUBLE_SHARP, TonePitchDutch.G, TonePitchDutch.FIS, null);
        verifyEnharmonicChange(TonePitchDutch.GISIS, "Gisis", EnharmonicType.DOUBLE_SHARP, TonePitchDutch.A, TonePitchDutch.GIS, null);
        verifyEnharmonicChange(TonePitchDutch.AISIS, "Aisis", EnharmonicType.DOUBLE_SHARP, TonePitchDutch.B, TonePitchDutch.AIS, null);
        verifyEnharmonicChange(TonePitchDutch.BISIS, "Bisis", EnharmonicType.DOUBLE_SHARP, TonePitchDutch.CIS, TonePitchDutch.BIS, null);
    }

    @Test
    public void testDoubleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchDutch.CESES, "Ceses", EnharmonicType.DOUBLE_FLAT, TonePitchDutch.BES, null, TonePitchDutch.CES);
        verifyEnharmonicChange(TonePitchDutch.DESES, "Deses", EnharmonicType.DOUBLE_FLAT, TonePitchDutch.C, null, TonePitchDutch.DES);
        verifyEnharmonicChange(TonePitchDutch.ESES, "Eses", EnharmonicType.DOUBLE_FLAT, TonePitchDutch.D, null, TonePitchDutch.ES);
        verifyEnharmonicChange(TonePitchDutch.FESES, "Feses", EnharmonicType.DOUBLE_FLAT, TonePitchDutch.DIS, null, TonePitchDutch.FES);
        verifyEnharmonicChange(TonePitchDutch.GESES, "Geses", EnharmonicType.DOUBLE_FLAT, TonePitchDutch.F, null, TonePitchDutch.GES);
        verifyEnharmonicChange(TonePitchDutch.ASES, "Ases", EnharmonicType.DOUBLE_FLAT, TonePitchDutch.G, null, TonePitchDutch.AS);
        verifyEnharmonicChange(TonePitchDutch.BESES, "Beses", EnharmonicType.DOUBLE_FLAT, TonePitchDutch.A, null, TonePitchDutch.BES);
    }

}
