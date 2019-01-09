package com.garethevans.church.opensongtablet.core.music.tone;

import org.junit.Test;

/**
 * Test of {@link TonePitchGerman}.
 */
public class TonePitchGermanTest extends AbstractTonePitchTest {

    protected ToneNameStyle<?> getNameStyle() {
        return TonePitchGerman.STYLE;
    }

    @Test
    public void testReferences() {
        verifyReference(TonePitchGerman.C, "C", ChromaticStep.S0, EnharmonicType.NORMAL, TonePitchGerman.CES, TonePitchGerman.CIS);
        verifyReference(TonePitchGerman.CIS, "Cis", ChromaticStep.S1, EnharmonicType.SINGLE_SHARP, TonePitchGerman.C, TonePitchGerman.CISIS);
        verifyReference(TonePitchGerman.D, "D", ChromaticStep.S2, EnharmonicType.NORMAL, TonePitchGerman.DES, TonePitchGerman.DIS);
        verifyReference(TonePitchGerman.DIS, "Dis", ChromaticStep.S3, EnharmonicType.SINGLE_SHARP, TonePitchGerman.D, TonePitchGerman.DISIS);
        verifyReference(TonePitchGerman.E, "E", ChromaticStep.S4, EnharmonicType.NORMAL, TonePitchGerman.ES, TonePitchGerman.EIS);
        verifyReference(TonePitchGerman.F, "F", ChromaticStep.S5, EnharmonicType.NORMAL, TonePitchGerman.FES, TonePitchGerman.FIS);
        verifyReference(TonePitchGerman.FIS, "Fis", ChromaticStep.S6, EnharmonicType.SINGLE_SHARP, TonePitchGerman.F, TonePitchGerman.FISIS);
        verifyReference(TonePitchGerman.G, "G", ChromaticStep.S7, EnharmonicType.NORMAL, TonePitchGerman.GES, TonePitchGerman.GIS);
        verifyReference(TonePitchGerman.GIS, "Gis", ChromaticStep.S8, EnharmonicType.SINGLE_SHARP, TonePitchGerman.G, TonePitchGerman.GISIS);
        verifyReference(TonePitchGerman.A, "A", ChromaticStep.S9, EnharmonicType.NORMAL, TonePitchGerman.AS, TonePitchGerman.AIS);
        verifyReference(TonePitchGerman.B, "B", ChromaticStep.S10, EnharmonicType.SINGLE_FLAT, TonePitchGerman.HESES, TonePitchGerman.H);
        verifyReference(TonePitchGerman.H, "H", ChromaticStep.S11, EnharmonicType.NORMAL, TonePitchGerman.B, TonePitchGerman.HIS);
    }

    @Test
    public void testSingleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchGerman.EIS, "Eis", EnharmonicType.SINGLE_SHARP, TonePitchGerman.F, TonePitchGerman.E, TonePitchGerman.EISIS);
        verifyEnharmonicChange(TonePitchGerman.AIS, "Ais", EnharmonicType.SINGLE_SHARP, TonePitchGerman.B, TonePitchGerman.A, TonePitchGerman.AISIS);
        verifyEnharmonicChange(TonePitchGerman.HIS, "His", EnharmonicType.SINGLE_SHARP, TonePitchGerman.C, TonePitchGerman.H, TonePitchGerman.HISIS);
    }

    @Test
    public void testSingleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchGerman.CES, "Ces", EnharmonicType.SINGLE_FLAT, TonePitchGerman.H, TonePitchGerman.CESES, TonePitchGerman.C);
        verifyEnharmonicChange(TonePitchGerman.DES, "Des", EnharmonicType.SINGLE_FLAT, TonePitchGerman.CIS, TonePitchGerman.DESES, TonePitchGerman.D);
        verifyEnharmonicChange(TonePitchGerman.ES, "Es", EnharmonicType.SINGLE_FLAT, TonePitchGerman.DIS, TonePitchGerman.ESES, TonePitchGerman.E);
        verifyEnharmonicChange(TonePitchGerman.FES, "Fes", EnharmonicType.SINGLE_FLAT, TonePitchGerman.E, TonePitchGerman.FESES, TonePitchGerman.F);
        verifyEnharmonicChange(TonePitchGerman.GES, "Ges", EnharmonicType.SINGLE_FLAT, TonePitchGerman.FIS, TonePitchGerman.GESES, TonePitchGerman.G);
        verifyEnharmonicChange(TonePitchGerman.AS, "As", EnharmonicType.SINGLE_FLAT, TonePitchGerman.GIS, TonePitchGerman.ASES, TonePitchGerman.A);
    }

    @Test
    public void testDoubleSharpEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchGerman.CISIS, "Cisis", EnharmonicType.DOUBLE_SHARP, TonePitchGerman.D, TonePitchGerman.CIS, null);
        verifyEnharmonicChange(TonePitchGerman.DISIS, "Disis", EnharmonicType.DOUBLE_SHARP, TonePitchGerman.E, TonePitchGerman.DIS, null);
        verifyEnharmonicChange(TonePitchGerman.EISIS, "Eisis", EnharmonicType.DOUBLE_SHARP, TonePitchGerman.FIS, TonePitchGerman.EIS, null);
        verifyEnharmonicChange(TonePitchGerman.FISIS, "Fisis", EnharmonicType.DOUBLE_SHARP, TonePitchGerman.G, TonePitchGerman.FIS, null);
        verifyEnharmonicChange(TonePitchGerman.GISIS, "Gisis", EnharmonicType.DOUBLE_SHARP, TonePitchGerman.A, TonePitchGerman.GIS, null);
        verifyEnharmonicChange(TonePitchGerman.AISIS, "Aisis", EnharmonicType.DOUBLE_SHARP, TonePitchGerman.H, TonePitchGerman.AIS, null);
        verifyEnharmonicChange(TonePitchGerman.HISIS, "Hisis", EnharmonicType.DOUBLE_SHARP, TonePitchGerman.CIS, TonePitchGerman.HIS, null);
    }

    @Test
    public void testDoubleFlatEnharmonicChanges() {
        verifyEnharmonicChange(TonePitchGerman.CESES, "Ceses", EnharmonicType.DOUBLE_FLAT, TonePitchGerman.B, null, TonePitchGerman.CES);
        verifyEnharmonicChange(TonePitchGerman.DESES, "Deses", EnharmonicType.DOUBLE_FLAT, TonePitchGerman.C, null, TonePitchGerman.DES);
        verifyEnharmonicChange(TonePitchGerman.ESES, "Eses", EnharmonicType.DOUBLE_FLAT, TonePitchGerman.D, null, TonePitchGerman.ES);
        verifyEnharmonicChange(TonePitchGerman.FESES, "Feses", EnharmonicType.DOUBLE_FLAT, TonePitchGerman.DIS, null, TonePitchGerman.FES);
        verifyEnharmonicChange(TonePitchGerman.GESES, "Geses", EnharmonicType.DOUBLE_FLAT, TonePitchGerman.F, null, TonePitchGerman.GES);
        verifyEnharmonicChange(TonePitchGerman.ASES, "Ases", EnharmonicType.DOUBLE_FLAT, TonePitchGerman.G, null, TonePitchGerman.AS);
        verifyEnharmonicChange(TonePitchGerman.HESES, "Heses", EnharmonicType.DOUBLE_FLAT, TonePitchGerman.A, null, TonePitchGerman.B);
    }

}
