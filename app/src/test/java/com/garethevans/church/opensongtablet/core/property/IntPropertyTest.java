package com.garethevans.church.opensongtablet.core.property;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class IntPropertyTest extends Assertions {

    @Test
    public void testBasic() {

        // given + when
        IntProperty p = new IntProperty("test", 42);

        // then
        assertThat(p.getName()).isEqualTo("test");
        assertThat(p.getType()).isEqualTo(Integer.class);
        assertThat(p.get()).isEqualTo(42).isEqualTo(p.getValue());
        assertThat(p.toString()).isEqualTo("test:42");
    }

    @Test
    public void testGetSet() {

        // given
        IntProperty p = new IntProperty("test");

        // when + then
        assertThat(p.getValue()).isNull();
        assertThat(p.get()).isEqualTo(0);
        p.set(42);
        assertThat(p.get()).isEqualTo(42).isEqualTo(p.getValue());
        p.setValue(Integer.valueOf(1234));
        assertThat(p.get()).isEqualTo(1234).isEqualTo(p.getValue());
    }
}
