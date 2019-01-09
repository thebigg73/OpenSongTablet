package com.garethevans.church.opensongtablet.core.property;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class FloatPropertyTest extends Assertions {

    @Test
    public void testBasic() {

        // given + when
        FloatProperty p = new FloatProperty("test", 42.42f);

        // then
        assertThat(p.getName()).isEqualTo("test");
        assertThat(p.getType()).isEqualTo(Float.class);
        assertThat(p.get()).isEqualTo(42.42f).isEqualTo(p.getValue());
        assertThat(p.toString()).isEqualTo("test:42.42");
    }

    @Test
    public void testGetSet() {

        // given
        FloatProperty p = new FloatProperty("test");

        // when + then
        assertThat(p.getValue()).isNull();
        assertThat(p.get()).isEqualTo(0);
        p.set(42.42f);
        assertThat(p.get()).isEqualTo(42.42f).isEqualTo(p.getValue());
        p.setValue(Float.valueOf(1234));
        assertThat(p.get()).isEqualTo(1234f).isEqualTo(p.getValue());
    }
}
