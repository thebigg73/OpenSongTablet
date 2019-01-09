package com.garethevans.church.opensongtablet.core.property;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class BooleanPropertyTest extends Assertions {

    @Test
    public void testBasic() {

        // given + when
        BooleanProperty p = new BooleanProperty("test", true);

        // then
        assertThat(p.getName()).isEqualTo("test");
        assertThat(p.getType()).isEqualTo(Boolean.class);
        assertThat(p.get()).isTrue().isEqualTo(p.getValue());
        assertThat(p.toString()).isEqualTo("test:true");
    }

    @Test
    public void testGetSet() {

        // given
        BooleanProperty p = new BooleanProperty("test");

        // when + then
        assertThat(p.getValue()).isNull();
        assertThat(p.get()).isFalse();
        p.set(true);
        assertThat(p.get()).isTrue().isEqualTo(p.getValue());
        p.setValue(false);
        assertThat(p.get()).isFalse().isEqualTo(p.getValue());
    }

    @Test
    public void testToggle() {

        // given
        BooleanProperty p = new BooleanProperty("test");

        // when + then
        assertThat(p.get()).isFalse();
        p.toggle();
        assertThat(p.get()).isTrue();
        p.toggle();
        assertThat(p.get()).isFalse();
    }
}
