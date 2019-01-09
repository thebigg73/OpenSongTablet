package com.garethevans.church.opensongtablet.core.music.tab;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class TabEffect {

    public static final TabEffect HAMMER_ON = new TabStartEffect("hammerOn", "H");

    public static final TabEffect PULL_OFF = new TabEndEffect("pullOff", "P");

    public static final TabEffect SLIDE = new TabMoveEffect("slide", "S");

    public static final TabEffect BEND = new TabMoveEffect("bend", "B");

    public static final Set<TabEffect> FX_NONE = Collections.emptySet();

    public static final Set<TabEffect> FX_HAMMER = effects(HAMMER_ON);

    public static final Set<TabEffect> FX_PULL = effects(PULL_OFF);

    public static final Set<TabEffect> FX_SLIDE = effects(SLIDE);

    public static final Set<TabEffect> FX_BEND = effects(BEND);

    public static final Set<TabEffect> FX_HAMMER_AND_PULL = effects(HAMMER_ON, PULL_OFF);

    public static final Set<TabEffect> FX_HAMMER_AND_SLIDE = effects(HAMMER_ON, SLIDE);

    public static final Set<TabEffect> FX_HAMMER_AND_BEND = effects(HAMMER_ON, BEND);

    public static final Set<TabEffect> FX_SLIDE_AND_PULL = effects(SLIDE, PULL_OFF);

    public static final Set<TabEffect> FX_BEND_AND_PULL = effects(BEND, PULL_OFF);

    public static final Set<TabEffect> FX_HAMMER_AND_SLIDE_AND_PULL = effects(HAMMER_ON, SLIDE, PULL_OFF);

    public static final Set<TabEffect> FX_HAMMER_AND_BEND_AND_PULL = effects(HAMMER_ON, BEND, PULL_OFF);

    private final String name;

    private final String symbol;

    protected TabEffect(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    private static final Set<TabEffect> effects(TabEffect... effects) {
        Set<TabEffect> set = new HashSet<>(effects.length);
        for (TabEffect effect : effects) {
            set.add(effect);
        }
        return Collections.unmodifiableSet(set);
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getName() {
        return this.name;
    }

    public boolean isStart() {
        return false;
    }

    public boolean isEnd() {
        return false;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static final class TabStartEffect extends TabEffect {

        private TabStartEffect(String name, String symbol) {
            super(name, symbol);
        }

        @Override
        public boolean isStart() {
            return true;
        }
    }

    public static final class TabEndEffect extends TabEffect {

        public TabEndEffect(String name, String symbol) {
            super(name, symbol);
        }

        @Override
        public boolean isEnd() {
            return true;
        }
    }

    public static final class TabMoveEffect extends TabEffect {

        public TabMoveEffect(String name, String symbol) {
            super(name, symbol);
        }
    }
}
