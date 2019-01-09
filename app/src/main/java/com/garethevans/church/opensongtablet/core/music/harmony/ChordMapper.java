package com.garethevans.church.opensongtablet.core.music.harmony;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.music.tone.ToneNameCase;
import com.garethevans.church.opensongtablet.core.music.tone.ToneNameStyle;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchMapper;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link AbstractMapper Mapper} for {@link Chord}.
 */
public class ChordMapper extends AbstractMapper<Chord> {

    public static final char BASE_TONE_SEPARATOR = '/';

    public static final ChordMapper INSTANCE = new ChordMapper();

    @Override
    public Chord parse(CharStream chars) {
        chars.skipWhile(' ');
        // detect fundamental tone
        TonePitch fundamentalTone = TonePitchMapper.INSTANCE.parse(chars);
        if (fundamentalTone == null) {
            return null;
        }
        // Amaj may conflict with Amaj7
        List<ChordExtension> extensions = new ArrayList<>();
        ChordExtension extension = ChordExtensionMapper.INSTANCE.parse(chars);
        // detect tonal tonalSystem (maj/min)...
        TonalSystem tonalSystem = null;
        if (extension == null) {
            tonalSystem = TonalSystemMapper.INSTANCE.parse(chars);
        }
        if (tonalSystem == null) {
            if (fundamentalTone.isLowercase()) {
                tonalSystem = TonalSystem.MINOR_EMPTY;
            } else {
                tonalSystem = TonalSystem.MAJOR_EMPTY;
            }
        }
        // detect (further) chord extensions
        boolean startExt = (extension != null);
        do {
            if (!startExt) {
                extension = ChordExtensionMapper.INSTANCE.parse(chars);
            }
            startExt = false;
            if (extension != null) {
                if (extension.isRemoveThird()) {
                    tonalSystem = null;
                }
                extensions.add(extension);
            }
        } while (extension != null);
        // detect base tone
        TonePitch baseTone = fundamentalTone;
        if (chars.expect(BASE_TONE_SEPARATOR)) {
            baseTone = TonePitchMapper.INSTANCE.parse(chars);
            if (baseTone == null) {
                // actually a parse error...
                baseTone = fundamentalTone;
            } else if (baseTone.getNameStyle() != fundamentalTone.getNameStyle()){
                if (fundamentalTone.getName().length() == 1) {
                    fundamentalTone = fundamentalTone.with(baseTone.getNameStyle());
                } else if (baseTone.getName().length() == 1) {
                    baseTone = baseTone.with(fundamentalTone.getNameStyle());
                }
            }
        }
        return new Chord(fundamentalTone, tonalSystem, baseTone, extensions);
    }

    @Override
    public void format(Chord chord, Appendable buffer, SongFormatOptions options) throws IOException {
        if (chord == null) {
            return;
        }
        if (options.isNormalizeChords()) {
            TonePitch fundamental = chord.getFundamentalTone().getNormalForm();
            TonalSystem tonalSystem = chord.getTonalSystem();
            ToneNameStyle toneNameStyle = options.getToneNameStyle();
            if (tonalSystem.isMinor()) {
                fundamental = fundamental.with(toneNameStyle, ToneNameCase.LOWER_CASE);
            } else if (tonalSystem.isMajor()) {
                fundamental = fundamental.with(toneNameStyle, ToneNameCase.CAPITAL_CASE);
            } else {
                fundamental = fundamental.with(toneNameStyle);
            }
            TonePitchMapper.INSTANCE.format(fundamental, buffer, options);
            if (tonalSystem.isMinor()) {
                buffer.append('m');
            }
            for (ChordExtension ext : chord.getExtensions()) {
                ChordExtensionMapper.INSTANCE.format(ext, buffer, options);
            }
            TonePitch base = chord.getBaseTone();
            if (base.getStep() != fundamental.getStep()) {
                buffer.append(BASE_TONE_SEPARATOR);
                base = base.with(toneNameStyle);
                TonePitchMapper.INSTANCE.format(base, buffer, options);
            }
        } else {
            buffer.append(chord.toString());
        }
    }
}
