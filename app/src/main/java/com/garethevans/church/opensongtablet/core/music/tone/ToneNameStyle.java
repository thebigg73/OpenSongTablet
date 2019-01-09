package com.garethevans.church.opensongtablet.core.music.tone;

import com.garethevans.church.opensongtablet.core.music.harmony.EnharmonicStyle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Style of the {@link TonePitch#getName() name} of a {@link TonePitch} and its related objects
 * such as {@link Tone}, {@link com.garethevans.church.opensongtablet.core.music.harmony.Chord},
 * {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey}, etc.
 */
public abstract class ToneNameStyle<P extends TonePitch> {

    private final Map<String, EnharmonicType> signsMap;

    private final String[] signs;

    protected ToneNameStyle() {
        super();
        this.signsMap = createSignsMap();
        this.signs = new String[]{getDoubleFlatSign(), getSingleFlatSign(), "", getSingleSharpSign(), getDoubleSharpSign()};
    }

    protected Map<String, EnharmonicType> createSignsMap() {
        Map<String, EnharmonicType> map = new HashMap<>();
        map.put(getSingleFlatSign(), EnharmonicType.SINGLE_FLAT);
        map.put(getDoubleFlatSign(), EnharmonicType.DOUBLE_FLAT);
        map.put(getSingleSharpSign(), EnharmonicType.SINGLE_SHARP);
        map.put(getDoubleSharpSign(), EnharmonicType.DOUBLE_SHARP);
        map.put(getNeutralSign(), EnharmonicType.NORMAL);
        return map;
    }

    /**
     * @return the name of this style (e.g. "English" or "German").
     */
    public abstract String getName();

    /**
     * @return the single sharp sign of this style.
     */
    public abstract String getSingleSharpSign();

    /**
     * @return the (regular) single flat sign of this style. Please note that the returned {@link String}
     * may not be consistently the single flat sign suffix. E.g. textual name styles (German or Dutch)
     * use "es" as single sharp sign but the flattened pitch of "A" is "As" rather than "Aes" and
     * the flattened pitch of "E" is "Es" rather than "Ees".
     */
    public abstract String getSingleFlatSign();

    /**
     * @return the double sharp sign of this style.
     */
    public abstract String getDoubleSharpSign();

    /**
     * @return the (regular) double flat sign of this style. Please note that the returned {@link String}
     * may not be consistently the double flat sign suffix. E.g. textual name styles (German or Dutch)
     * use "eses" as double sharp sign but the double flattened pitch of "A" is "Ases" rather than "Aeses" and
     * the double flattened pitch of "E" is "Eses" rather than "Eeses".
     */
    public abstract String getDoubleFlatSign();

    /**
     * @return the (optional) neutral sign used to dissolve enharmonic signs. Used for {@link TonePitchInternational#B_NEUTRAL}.
     * Will be the empty {@link String} if not used by this style.
     */
    public abstract String getNeutralSign();

    /**
     * @param enharmonicType the {@link EnharmonicType}.
     * @return the suffix to append to the base pitch (e.g. A, B, C, D, E, F, G) as enharmonic signs
     * of the given {@link EnharmonicType}.
     */
    public String getSigns(EnharmonicType enharmonicType) {
        return this.signs[enharmonicType.getSignOffset() + 2];
    }

    /**
     * @param name the {@link TonePitch#getName() pitch name}.
     * @return the {@link EnharmonicType} for the given {@code name}.
     */
    public EnharmonicType getType(String name) {
        int len = name.length();
        if (len == 1) {
            return EnharmonicType.NORMAL;
        } else if (len == 0) {
            return null;
        }
        String signs = name.substring(1);
        return this.signsMap.get(signs);
    }

    public boolean checkName(String name, EnharmonicType enharmonicType) {
        return enharmonicType.equals(getType(name));
    }

    /**
     * @param name the {@link TonePitch#getName() name} of the requested {@link TonePitch}.
     * @return the {@link TonePitch} of this {@link TonePitch#getNameStyle() name style} with the given
     * {@link TonePitch#getName() name} or {@code null} if no such pitch exists.
     */
    public abstract P pitch(String name);

    /**
     * @param step the {@link TonePitch#getStep() step} of the requested {@link TonePitch}. Should
     *             be in the range from {@code 0} to {@code 11}.
     * @return the {@link TonePitch} of this {@link TonePitch#getNameStyle() name style} with the given
     * {@link TonePitch#getStep() step} or {@code null} if no such pitch exists (if step is not in
     * the range from {@code 0} to {@code 11}).
     */
    public final P pitch(ChromaticStep step) {
        return pitch(step, null);
    }

    /**
     * @param step the {@link TonePitch#getStep() step} of the requested {@link TonePitch}. Should
     *             be in the range from {@code 0} to {@code 11}.
     * @param type the {@link TonePitch#getEnharmonicType() enharmonic type} of the requested {@link TonePitch}.
     * @return the {@link TonePitch} of this {@link TonePitch#getNameStyle() name style} with the given
     * {@link TonePitch#getStep() step} and {@link TonePitch#getEnharmonicType() enharmonic type} or
     * {@code null} if no such pitch exists (e.g. for step {@code 8} no pitch exists for {@link EnharmonicType#NORMAL},
     * {@link EnharmonicType#DOUBLE_FLAT}, or {@link EnharmonicType#DOUBLE_SHARP}).
     */
    public final P pitch(ChromaticStep step, EnharmonicType type) {
        return pitch(step, type, ToneNameCase.CAPITAL_CASE);
    }

    /**
     * @param step     the {@link TonePitch#getStep() step} of the requested {@link TonePitch}. Should
     *                 be in the range from {@code 0} to {@code 11}.
     * @param style    the preferred {@link EnharmonicStyle} of the requested {@link TonePitch}. However,
     *                 to ensure that a valid result can always be returned even if no {@link TonePitch}
     *                 exists for the given {@code step} that has the given {@link TonePitch#getEnharmonicStyle() enharmonic style},
     *                 this argument is only used as a preference. If a the given {@link EnharmonicStyle}
     *                 can be satisfied with a {@link EnharmonicType#getSignCount() single enharmonic sign},
     *                 such pitch will be returned, otherwise the {@link TonePitch#isNormal() normal} pitch
     *                 will be returned.
     * @param nameCase the {@link TonePitch#getCase() case} of the requested {@link TonePitch}.
     * @return the {@link TonePitch} of this {@link TonePitch#getNameStyle() name style} with the given
     * {@link TonePitch#getStep() step} and a single enharmonic sign of the given {@link EnharmonicStyle}
     * or if not possible the {@link TonePitch#isNormal() normal} one.
     */
    public final P pitch(ChromaticStep step, ToneNameCase nameCase, EnharmonicStyle style) {
        if ((style != null) && !style.isNormal()) {
            P pitch;
            if (style.isFlat()) {
                pitch = pitch(step, EnharmonicType.SINGLE_FLAT, nameCase);
            } else {
                pitch = pitch(step, EnharmonicType.SINGLE_SHARP, nameCase);
            }
            if (pitch != null) {
                return pitch;
            }
        }
        return pitch(step, null, nameCase);
    }

    /**
     * @param step     the {@link TonePitch#getStep() step} of the requested {@link TonePitch}. Should
     *                 be in the range from {@code 0} to {@code 11}.
     * @param type     the {@link TonePitch#getEnharmonicType() enharmonic type} of the requested {@link TonePitch}.
     * @param nameCase the {@link TonePitch#getCase() case} of the requested {@link TonePitch}.
     * @return the {@link TonePitch} of this {@link TonePitch#getNameStyle() name style} with the given
     * {@link TonePitch#getStep() step} and {@link TonePitch#getEnharmonicType() enharmonic type} or
     * {@code null} if no such pitch exists (e.g. for step {@code 8} no pitch exists for {@link EnharmonicType#NORMAL},
     * {@link EnharmonicType#DOUBLE_FLAT}, or {@link EnharmonicType#DOUBLE_SHARP}).
     */
    public abstract P pitch(ChromaticStep step, EnharmonicType type, ToneNameCase nameCase);

    /**
     * @param sign the number of {@link EnharmonicStyle#isSharp() sharp} signs or the negative number of
     *             {@link EnharmonicStyle#isFlat() flat} signs of a {@link com.garethevans.church.opensongtablet.core.music.harmony.MusicalKey}.
     *             Should be in the range from {@code -7} to {@code 7}.
     * @return the {@link TonePitch} added by the enharmonic sign represented by {@code sign} according
     * to the <em>circle of fifths</em>.
     */
    public P pitchByKeySign(int sign) {
        switch (sign) {
            case -7:
                return pitch(ChromaticStep.S4, EnharmonicType.SINGLE_FLAT); // F-flat
            case -6:
                return pitch(ChromaticStep.S11, EnharmonicType.SINGLE_FLAT); // C-flat
            case -5:
                return pitch(ChromaticStep.S6, EnharmonicType.SINGLE_FLAT); // G-flat
            case -4:
                return pitch(ChromaticStep.S1, EnharmonicType.SINGLE_FLAT); // D-flat
            case -3:
                return pitch(ChromaticStep.S8, EnharmonicType.SINGLE_FLAT); // A-flat
            case -2:
                return pitch(ChromaticStep.S3, EnharmonicType.SINGLE_FLAT); // E-flat
            case -1:
                return pitch(ChromaticStep.S10, EnharmonicType.SINGLE_FLAT); // B-flat
            case 0:
                return pitch(ChromaticStep.S0);
            case 1:
                return pitch(ChromaticStep.S6, EnharmonicType.SINGLE_SHARP); // F-sharp
            case 2:
                return pitch(ChromaticStep.S1, EnharmonicType.SINGLE_SHARP); // C-sharp
            case 3:
                return pitch(ChromaticStep.S8, EnharmonicType.SINGLE_SHARP); // G-sharp
            case 4:
                return pitch(ChromaticStep.S3, EnharmonicType.SINGLE_SHARP); // D-sharp
            case 5:
                return pitch(ChromaticStep.S10, EnharmonicType.SINGLE_SHARP); // A-sharp
            case 6:
                return pitch(ChromaticStep.S5, EnharmonicType.SINGLE_SHARP); // E-sharp
            case 7:
                return pitch(ChromaticStep.S0, EnharmonicType.SINGLE_SHARP); // B-sharp
        }
        return null;
    }

    /**
     * @return the {@link Collection} with all available {@link TonePitch}es of this style.
     */
    public abstract Collection<P> values();

    @Override
    public String toString() {
        return getName();
    }
}
