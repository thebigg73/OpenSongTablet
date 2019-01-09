package com.garethevans.church.opensongtablet.core.config;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class AppConfigTest extends Assertions {

    @Test
    public void testDefaults() {

        // given + when
        AppConfig config = new AppConfig();
        ChordConfig chordConfig = config.getChord();

        // then
        assertThat(chordConfig.showChords.get()).isTrue();
        assertThat(chordConfig.showCapoChords.get()).isFalse();
        assertThat(chordConfig.showNativeAndCapoChords.get()).isFalse();
        assertThat(chordConfig.showCapoAsNumerals.get()).isFalse();
    }

}
