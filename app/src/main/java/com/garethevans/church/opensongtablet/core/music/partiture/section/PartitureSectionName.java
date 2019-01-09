package com.garethevans.church.opensongtablet.core.music.partiture.section;

/**
 * Represents the name of a {@link PartitureSection} in a structured way with {@link #getType() type} and {@link #getSuffix() suffix}.
 * @see PartitureSection#getName()
 */
public class PartitureSectionName {

    private final String name;

    private final PartitureSectionType type;

    private final boolean typeId;

    private final String suffix;

    public PartitureSectionName(String name) {
        super();
        this.name = name;
        String suffix = null;
        boolean typeId = false;
        PartitureSectionType type = null;
        for (PartitureSectionType t : PartitureSectionType.values()) {
            suffix = getSuffix(name, t.getId());
            if (suffix == null) {
                suffix = getSuffix(name, t.getName());
            } else {
                typeId = true;
            }
            if (suffix != null) {
                type = t;
                break;
            }
        }
        this.type = type;
        if (suffix == null) {
            this.suffix = "";
        } else {
            this.suffix = suffix;
        }
        this.typeId = typeId;
    }

    private static String getSuffix(String name, String type) {

        if (name.startsWith(type)) {
            String suffix = name.substring(type.length());
            if (suffix.isEmpty()) {
                return suffix;
            }
            char c = suffix.charAt(0);
            if ((c == ' ') || ((c >= '0') && (c <= '9'))) {
                return suffix;
            }
        }
        return null;
    }

    public String getName() {

        return this.name;
    }

    /**
     * @return the standard {@link PartitureSectionType} or {@code null} if no type was detected.
     */
    public PartitureSectionType getType() {

        return this.type;
    }

    /**
     * @return {@code true} if the {@link #getType() type} was detected via its {@link PartitureSectionType#getId() ID}, {@code false} otherwise.
     */
    public boolean isTypeId() {
        return this.typeId;
    }

    /**
     * @return the optional suffix (e.g. "1" if name is "Verse1").
     */
    public String getSuffix() {

        return this.suffix;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
