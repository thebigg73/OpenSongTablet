/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core;

import org.assertj.core.api.Assertions;

/**
 * Abstract base class for tests.
 *
 * @author hohwille
 */
public class AbstractTest extends Assertions {

  public static void checkNegative(Runnable lambda, Class<? extends Throwable> error) {

    checkNegative(lambda, error, false, null, true);
  }

  public static void checkNegative(Runnable lambda, Class<? extends Throwable> error, boolean rootCause,
      String message, boolean substring) {

    try {
      lambda.run();
    } catch (Throwable t) {
      if (rootCause) {
        assertThat(t).isInstanceOf(error);
      } else {
        assertThat(t).hasCauseInstanceOf(error);
      }
      if (message != null) {
        if (substring) {
          assertThat(t).hasMessageContaining(message);
        } else {
          assertThat(t).hasMessage(message);
        }
      }
    }
  }

  public static <T> void checkEqualsAndHashCode(T x, T y, boolean equal) {

    checkEquals(x, y, equal);
    checkHashCode(x, y);
  }

  public static <T> void checkEquals(T x, T y, boolean equal) {

    // equals has to be reflexive...
    assertThat(x).isNotNull().isNotEqualTo(null).isEqualTo(x);
    assertThat(y).isNotNull().isNotEqualTo(null).isEqualTo(y);
    // equals has to be reflexive...
    assertThat(x).isNotSameAs(y);
    assertThat(x.equals(y)).isEqualTo(y.equals(x)).isEqualTo(equal);
    // equals has to consider type
    assertThat(x).isNotEqualTo(new Object());
  }

  public static <T> void checkHashCode(T x, T y) {

    assertThat(x).isNotNull();
    assertThat(y).isNotNull();
    // hashCode() has to be self consistent
    assertThat(x.hashCode()).isEqualTo(x.hashCode());
    assertThat(y.hashCode()).isEqualTo(y.hashCode());
    // hashCode() has to be consistent with equals
    assertThat(x).isNotSameAs(y);
    if (x.equals(y)) {
      assertThat(x.hashCode()).isEqualTo(y.hashCode());
    } else {
      // this one is maybe a little dangerous as there can be a hash collision
      // However, users are requested to provide reasonable arguments to avoid this...
      assertThat(x.hashCode()).isNotEqualTo(y.hashCode());
    }
  }

}
