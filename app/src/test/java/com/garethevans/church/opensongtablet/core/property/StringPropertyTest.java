package com.garethevans.church.opensongtablet.core.property;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class StringPropertyTest extends Assertions {

    @Test
    public void testBasic() {

        // given + when
        StringProperty p = new StringProperty("test", "its magic");

        // then
        assertThat(p.getName()).isEqualTo("test");
        assertThat(p.getType()).isEqualTo(String.class);
        assertThat(p.getValue()).isEqualTo("its magic");
        assertThat(p.toString()).isEqualTo("test:its magic");
    }

    @Test
    public void testGetSet() {

        // given
        StringProperty p = new StringProperty("test");

        // when + then
        assertThat(p.getValue()).isEmpty();
        p.setValue("its magic");
        assertThat(p.getValue()).isEqualTo("its magic");
        p.setValue("hello world");
        assertThat(p.getValue()).isEqualTo("hello world");
    }
}
