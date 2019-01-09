package com.garethevans.church.opensongtablet.core.music.transpose;

import com.garethevans.church.opensongtablet.core.music.harmony.EnharmonicStyle;
import com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey;
import com.garethevans.church.opensongtablet.core.music.harmony.TonalSystem;
import com.garethevans.church.opensongtablet.core.music.tab.Tab;
import com.garethevans.church.opensongtablet.core.music.tone.Tone;

public class TransposeContext {

    private boolean keepKey;

    private MusicalKey key;

    private Tab tab;

    private EnharmonicStyle enharmonicStyle;

    private boolean changeTab;

    private boolean normalizeChords;

    public TransposeContext() {
        super();
        this.keepKey = true;
    }

    public TransposeContext(MusicalKey key) {
        this();
        this.key = key;
    }

    public TransposeContext(EnharmonicStyle enharmonicStyle) {
        this();
        this.enharmonicStyle = enharmonicStyle;
    }

    /**
     * @return {@code true} if the {@link MusicalKey} shall remain unchanged, {@code false} otherwise
     *         (to transpose also the key).
     */
    public boolean isKeepKey() {
        return this.keepKey;
    }

    public void setKeepKey(boolean keepKey) {
        this.keepKey = keepKey;
    }

    /**
     * @return the current {@link MusicalKey} used to transpose atomic musical objects such as e.g.
     *         a {@link Tone}. Will be {@code null}
     *         until set externally (by caller of initial transpose method) or internally (whilst
     *         transposing composed objects between recursive calls of transpose).
     */
    public MusicalKey getKey() {
        return this.key;
    }

    public void setKey(MusicalKey key) {
        this.key = key;
    }

    public TonalSystem getTonalSystem() {
        if (this.key != null) {
            return this.key.getTonalSystem();
        }
        return null;
    }

    /**
     * @return the {@link EnharmonicStyle}. May be used if {@link #getKey() key} is not available.
     */
    public EnharmonicStyle getEnharmonicStyle() {
        if (this.enharmonicStyle == null) {
            if (this.key == null) {
                return EnharmonicStyle.NORMAL;
            } else {
                return this.key.getEnharmonicStyle();
            }
        }
        return this.enharmonicStyle;
    }

    public void setEnharmonicStyle(EnharmonicStyle enharmonicStyle) {
        this.enharmonicStyle = enharmonicStyle;
    }

    /**
     * @return the current {@link Tab} used to transpose atomic musical instruments.
     */
    public Tab getTab() {
        return this.tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    /**
     * @return {@code true} if a potential {@link Tab}s should be transposed so that its
     * {@link Tab#getStrings() tuning} actually changes.
     * @see #setTab(Tab)
     */
    public boolean isChangeTab() {
        return this.changeTab;
    }

    public void setChangeTab(boolean changeTab) {
        this.changeTab = changeTab;
    }

    public boolean isNormalizeChords() {
        return this.normalizeChords;
    }

    public void setNormalizeChords(boolean normalizeChords) {
        this.normalizeChords = normalizeChords;
    }
}
