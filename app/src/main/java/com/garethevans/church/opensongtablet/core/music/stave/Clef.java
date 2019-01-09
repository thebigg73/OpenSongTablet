/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music.stave;

import com.garethevans.church.opensongtablet.core.music.tone.TonePitch;

/**
 * The clef is the initial symbol of a {@link Stave} that
 * indicates which line is identifying which {@link TonePitch tone}.
 *
 * @author hohwille
 */
public enum Clef {

  /**
   * The G-clef which is also called treble or violin clef. This is the most common clef used in modern music. If
   * you have proper unicode support you can see it here: &#119070;
   */
  G,

  /**
   * The F-clef which is also called bass clef. Besides the G-clef this is also commonly used in modern music. If you
   * have proper unicode support you can see it here: &#119074;
   */
  F,

  /**
   * The C-clef which is also called alto clef (or tenor clef according to placement). If you have proper unicode
   * support you can see it here: &#119073;
   */
  C,

  /**
   * The neutral clef is also called percussion clef. It is actually not a clef in the same sense as the others. If you
   * have proper unicode support you can see it here: &#119077;
   */
  N,

}
