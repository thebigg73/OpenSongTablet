package com.garethevans.church.opensongtablet.core.music.partiture.section;

public enum PartitureSectionType {

    CHORUS("Chorus", "C"),
    VERSE("Verse", "V"),
    ;

    private final String name;

    private final String id;

    private PartitureSectionType(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
