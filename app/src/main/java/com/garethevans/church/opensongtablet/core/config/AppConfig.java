package com.garethevans.church.opensongtablet.core.config;

import com.garethevans.church.opensongtablet.core.bean.Bean;
import com.garethevans.church.opensongtablet.core.property.BeanProperty;

/**
 * {@link Bean} of the entire app.
 */
public class AppConfig extends Config {

    public final BeanProperty<ChordConfig> chordProperty;

    public final BeanProperty<PadConfig> padProperty;

    public final BeanProperty<MetronomeConfig> metronomeProperty;

    public final BeanProperty<MenuConfig> menuProperty;

    public AppConfig() {

        super();
        this.chordProperty = register(new BeanProperty<>("chord", new ChordConfig()));
        this.padProperty = register(new BeanProperty<>("pad", new PadConfig()));
        this.metronomeProperty = register(new BeanProperty<>("metronome", new MetronomeConfig()));
        this.menuProperty = register(new BeanProperty<>("menu", new MenuConfig()));
    }

    public ChordConfig getChord() {

        return this.chordProperty.getValue();
    }

    public PadConfig getPad() {

        return this.padProperty.getValue();
    }

    public MetronomeConfig getMetronome() {

        return this.metronomeProperty.getValue();
    }

    public MenuConfig getMenu() {

        return this.menuProperty.getValue();
    }
}
