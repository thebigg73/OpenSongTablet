package com.garethevans.church.opensongtablet.core.format;

import com.garethevans.church.opensongtablet.core.music.tone.ToneNameStyle;
import com.garethevans.church.opensongtablet.core.music.tone.TonePitchEnglish;

public class SongFormatOptions {

    private boolean normalizeChords;

    private boolean normalizeChordExtensions;

    private boolean normalizeItemDecorations;

    private boolean normalizeMusicalKeys;

    private boolean normalizeSections;

    private ToneNameStyle toneNameStyle;

    public SongFormatOptions() {
        super();
        this.toneNameStyle = TonePitchEnglish.STYLE;
    }

    public ToneNameStyle getToneNameStyle() {
        return this.toneNameStyle;
    }

    public void setToneNameStyle(ToneNameStyle toneNameStyle) {
        this.toneNameStyle = toneNameStyle;
    }

    public boolean isNormalizeChords() {
        return this.normalizeChords;
    }

    public void setNormalizeChords(boolean normalizeChords) {
        this.normalizeChords = normalizeChords;
    }

    public boolean isNormalizeChordExtensions() {
        return this.normalizeChordExtensions;
    }

    public void setNormalizeChordExtensions(boolean normalizeChordExtensions) {
        this.normalizeChordExtensions = normalizeChordExtensions;
    }

    public boolean isNormalizeItemDecorations() {
        return this.normalizeItemDecorations;
    }

    public void setNormalizeItemDecorations(boolean normalizeItemDecorations) {
        this.normalizeItemDecorations = normalizeItemDecorations;
    }

    public boolean isNormalizeSections() {
        return this.normalizeSections;
    }

    public void setNormalizeSections(boolean normalizeSections) {
        this.normalizeSections = normalizeSections;
    }

    public boolean isNormalizeMusicalKeys() {
        return this.normalizeMusicalKeys;
    }

    public void setNormalizeMusicalKeys(boolean normalizeMusicalKeys) {
        this.normalizeMusicalKeys = normalizeMusicalKeys;
    }
}
