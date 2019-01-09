package com.garethevans.church.opensongtablet.core.config;

import com.garethevans.church.opensongtablet.core.bean.BeanXmlStaxMapper;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;

public class ConfigXmlStaxMapperTest extends Assertions {

    public static final String PROFILE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" //
            + "<myprofile>\n" //
            + "  <showChords>false</showChords>\n" //
            + "  <showCapoChords>true</showCapoChords>\n" //
            + "  <showCapoAsNumerals>true</showCapoAsNumerals>\n" //
            + "  <showNativeAndCapoChords>true</showNativeAndCapoChords>\n" //
            + "</myprofile>\n";

    @Test
    public void testLoadXmlStax() throws Exception {

        // given
        AppConfig config = new AppConfig();
        ChordConfig chordConfig = config.getChord();
        assertThat(chordConfig.showCapoAsNumerals.get()).isFalse();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        Reader reader = new StringReader(PROFILE_XML);

        // when
        new BeanXmlStaxMapper(config).loadXml(reader);

        // then
        assertThat(chordConfig.showChords.get()).isFalse();
        assertThat(chordConfig.showCapoChords.get()).isTrue();
        assertThat(chordConfig.showCapoAsNumerals.get()).isTrue();
        assertThat(chordConfig.showNativeAndCapoChords.get()).isTrue();
    }

}
