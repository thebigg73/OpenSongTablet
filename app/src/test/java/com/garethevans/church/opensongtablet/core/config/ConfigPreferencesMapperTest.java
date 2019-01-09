package com.garethevans.church.opensongtablet.core.config;

import com.garethevans.church.opensongtablet.core.bean.BeanPreferencesMapper;
import com.garethevans.church.opensongtablet.core.property.Property;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConfigPreferencesMapperTest extends Assertions {

    @Test
    public void testSavePreferences() {

        // given
        AppConfig config = new AppConfig();
        ChordConfig chordConfig = config.getChord();
        chordConfig.showCapoAsNumerals.set(true);
        Map<String, Object> map = new HashMap<>();
        DummyPreferences dummyPreferences = new DummyPreferences(map);
        Collection<Property<?>> props = config.getProperties(true, false);
        BeanPreferencesMapper preferences = new BeanPreferencesMapper(config, dummyPreferences);

        // when
        preferences.save();

        // then
        assertThat(map).hasSize(props.size());
        for (Property<?> p : props) {
            assertThat(dummyPreferences.get(p.getName(), null)).as(p.getName()).isEqualTo(p.getValueOrDefault());
        }
    }

    @Test
    public void testLoadPreferences() {

        // given
        AppConfig originalConfig = new AppConfig();
        ChordConfig chordConfig = originalConfig.getChord();
        chordConfig.showChords.set(false);
        chordConfig.showCapoChords.set(true);
        chordConfig.showNativeAndCapoChords.set(true);
        chordConfig.showCapoAsNumerals.set(true);
        Map<String, Object> map = new HashMap<>();
        Collection<Property<?>> originalProperties = originalConfig.getProperties(true, false);
        for (Property<?> p : originalProperties) {
            map.put(p.getName(), p.getValue());
        }
        DummyPreferences dummyPreferences = new DummyPreferences(map);
        AppConfig config = new AppConfig();
        BeanPreferencesMapper preferences = new BeanPreferencesMapper(config, dummyPreferences);

        // when
        preferences.load();

        // then
        for (Property<?> originalProperty : originalProperties) {
            Property<?> property = config.getProperty(originalProperty.getName(), true);
            assertThat(property.getValue()).isEqualTo(originalProperty.getValue());
        }
    }
}
