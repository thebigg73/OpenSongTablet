package com.garethevans.church.opensongtablet.core.music.tone;

import com.garethevans.church.opensongtablet.core.music.harmony.EnharmonicStyle;

import org.assertj.core.api.Assertions;

import java.util.Locale;

public abstract class AbstractTonePitchTest extends Assertions {

    protected abstract ToneNameStyle<?> getNameStyle();

    protected void verify(TonePitch pitch, String name, ChromaticStep step, EnharmonicType type, boolean normal, TonePitch reference, TonePitch flattened, TonePitch sharpened, ToneNameStyle style, ToneNameCase nameCase) {
        assertThat(pitch).isNotNull();
        assertThat(pitch.getName()).isEqualTo(name);
        assertThat(pitch.getStep()).isSameAs(step).isSameAs(reference.getStep());
        assertThat(pitch.getNameStyle()).isSameAs(style);
        assertThat(pitch.getCase()).isEqualTo(nameCase);
        assertThat(nameCase.convert(pitch.getName())).isEqualTo(pitch.getName());
        assertThat(pitch.getEnharmonicType()).isSameAs(type);
        EnharmonicStyle enharmonicStyle = type.getStyle();
        assertThat(pitch.getEnharmonicStyle()).isSameAs(enharmonicStyle);
        assertThat(pitch.isFlat()).isEqualTo(enharmonicStyle.isFlat());
        assertThat(pitch.isSharp()).isEqualTo(enharmonicStyle.isSharp());
        assertThat(pitch.isNormal()).isEqualTo(normal);
        if (normal) {
            assertThat(pitch.getNormalForm()).isSameAs(pitch);
        } else {
            assertThat(pitch.getNormalForm()).isNotSameAs(pitch);
        }
        assertThat(pitch.getReference()).isSameAs(reference);
        assertThat(pitch.flatten()).isSameAs(flattened);
        assertThat(pitch.sharpen()).isSameAs(sharpened);
    }

    protected void verify(TonePitch pitch, String name, ChromaticStep step, EnharmonicType type, boolean normal, TonePitch reference, TonePitch flattened, TonePitch sharpened, ToneNameStyle style) {
        verify(pitch, name, step, type, normal, reference, flattened, sharpened, style, ToneNameCase.CAPITAL_CASE);
        ToneNameCase lowerCase = ToneNameCase.LOWER_CASE;
        TonePitch flattenedLower = null;
        if (flattened != null) {
            flattenedLower = flattened.with(lowerCase);
        }
        TonePitch sharpenedLower = null;
        if (sharpened != null) {
            sharpenedLower = sharpened.with(lowerCase);
        }
        verify(pitch.with(lowerCase), name.toLowerCase(Locale.US), step, type, normal, reference, flattenedLower, sharpenedLower, style, lowerCase);
    }

    protected void verify(TonePitch pitch, String name, ChromaticStep step, EnharmonicType type, boolean normal, TonePitch reference, TonePitch flattened, TonePitch sharpened) {
        verify(pitch, name, step, type, normal, reference, flattened, sharpened, getNameStyle());
    }

    protected void verifyEnharmonicChange(TonePitch pitch, String name, EnharmonicType type, TonePitch reference, TonePitch flattened, TonePitch sharpened) {
        verify(pitch, name, reference.getStep(), type, false, reference, flattened, sharpened);
    }

    protected void verifyNormal(TonePitch pitch, String name, ChromaticStep step, EnharmonicType type, TonePitch reference, TonePitch flattened, TonePitch sharpened) {
        verify(pitch, name, step, type, true, reference, flattened, sharpened);
    }

    protected void verifyReference(TonePitch pitch, String name, ChromaticStep step, EnharmonicType type, TonePitch flattened, TonePitch sharpened) {
        verifyNormal(pitch, name, step, type, pitch, flattened, sharpened);
    }

}
