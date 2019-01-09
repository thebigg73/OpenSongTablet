/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package com.garethevans.church.opensongtablet.core.music;

import com.garethevans.church.opensongtablet.core.music.stave.StaveBracket;

/**
 * Collection of musical constants (e.g. Unicode characters and strings for musical symbols).
 *
 * @author hohwille
 */
public interface MusicalUnicodeConstants {

  /** The single flat symbol: {@value}. */
  char SINGLE_FLAT_CHAR = '\u266D';

  /** The single flat symbol: {@value}. */
  String SINGLE_FLAT = Character.toString(SINGLE_FLAT_CHAR);

  /** The neutral symbol: {@value}. */
  char NEUTRAL_CHAR = '\u266E';

  /** The neutral symbol: {@value}. */
  String NEUTRAL = Character.toString(NEUTRAL_CHAR);

  /** The single sharp symbol: {@value} . */
  char SINGLE_SHARP_CHAR = '\u266F';

  /** The single sharp symbol: {@value}. */
  String SINGLE_SHARP = Character.toString(SINGLE_SHARP_CHAR);

  /** The single barline symbol: {@value}. */
  String SINGLE_BARLINE = "\uD834\uDD00";

  /** The double barline symbol: {@value}. */
  String DOUBLE_BARLINE = "\uD834\uDD01";

  /** The finale barline symbol: {@value}. */
  String FINAL_BARLINE = "\uD834\uDD02";

  /** The reverse finale barline symbol: {@value}. */
  String REVERSE_FINAL_BARLINE = "\uD834\uDD03";

  /** The left repeat symbol: {@value}. */
  String LEFT_REPEAT = "\uD834\uDD06";

  /** The right repeat symbol: {@value}. */
  String RIGHT_REPEAT = "\uD834\uDD07";

  /** The dal segno symbol: {@value}. */
  String DAL_SEGNO = "\uD834\uDD09";

  /** The da capo symbol: {@value}. */
  String DA_CAPO = "\uD834\uDD0A";

  /** The segno symbol: {@value}. */
  String SEGNO = "\uD834\uDD0B";

  /** The coda symbol: {@value}. */
  String CODA = "\uD834\uDD0C";

  /** The fermata symbol: {@value}. */
  String FERMATA = "\uD834\uDD10";

  /** The fermata below symbol: {@value}. */
  String FERMATA_BELOW = "\uD834\uDD11";

  /** The breath symbol: {@value}. */
  String BREATH_MARK = "\uD834\uDD12";

  /** The caesura symbol: {@value}. */
  String CAESURA = "\uD834\uDD13";

  /**
   * The brace symbol: {@value}.
   *
   * @see StaveBracket#CURLY
   */
  String BRACE = "\uD834\uDD14";

  /**
   * The bracket symbol: {@value}.
   *
   * @see StaveBracket#SQUARE
   */
  String BRACKET = "\uD834\uDD15";

  /**
   * The 5-line stave symbol: {@value}.
   *
   * @see com.garethevans.church.opensongtablet.core.music.stave.Stave
   */
  String STAVE_5 = "\uD834\uDD1A";

  /**
   * The 6-string fretboard symbol: {@value}.
   *
   * @see com.garethevans.church.opensongtablet.core.music.harmony.Chord
   * @see com.garethevans.church.opensongtablet.core.music.tab.Tab
   */
  String SIX_STRING_FRETBOARD = "\uD834\uDD1C";

  /**
   * The 4-string fretboard symbol: {@value}.
   *
   * @see com.garethevans.church.opensongtablet.core.music.harmony.Chord
   * @see com.garethevans.church.opensongtablet.core.music.tab.Tab
   */
  String FOUR_STRING_FRETBOARD = "\uD834\uDD1D";

  /**
   * The G clef symbol: {@value}.
   *
   * @see com.garethevans.church.opensongtablet.core.music.stave.Clef#G
   */
  String G_CLEV = "\uD834\uDD1E";

  /**
   * The C clef symbol: {@value}.
   *
   * @see com.garethevans.church.opensongtablet.core.music.stave.Clef#C
   */
  String C_CLEV = "\uD834\uDD21";

  /**
   * The F clef symbol: {@value}.
   *
   * @see com.garethevans.church.opensongtablet.core.music.stave.Clef#F
   */
  String F_CLEV = "\uD834\uDD22";

  /**
   * The drum clef 1 symbol: {@value}.
   *
   * @see com.garethevans.church.opensongtablet.core.music.stave.Clef
   */
  String DRUM_CLEV_1 = "\uD834\uDD25";

  /**
   * The drum clef 2 symbol: {@value}.
   *
   * @see com.garethevans.church.opensongtablet.core.music.stave.Clef
   */
  String DRUM_CLEV_2 = "\uD834\uDD26";

  /** The multiple measure rest symbol: {@value}. */
  String MULTI_MEASURE_REST = "\uD834\uDD29";

  char DOUBLE_SIGN_CHAR1 = '\uD834';

  char DOUBLE_SHARP_CHAR2 = '\uDD2A';

  char DOUBLE_FLAT_CHAR2 = '\uDD2B';

  /** The double sharp symbol: {@value}. */
  String DOUBLE_SHARP = "" + DOUBLE_SIGN_CHAR1 + DOUBLE_SHARP_CHAR2;

  /** The double flat symbol: {@value}. */
  String DOUBLE_FLAT = "" + DOUBLE_SIGN_CHAR1 + DOUBLE_FLAT_CHAR2;

  /** The common time symbol: {@value}. */
  String COMMON_TIME = "\uD834\uDD34";

  /** The cut time symbol: {@value}. */
  String CUT_TIME = "\uD834\uDD35";

  /** The ottava alta (8va) symbol: {@value}. */
  String OTTAVA_ALTA = "\uD834\uDD36";

  /** The ottava bassa (8vb) symbol: {@value}. */
  String OTTAVA_BASSA = "\uD834\uDD37";

  /** The multi rest symbol: {@value}. */
  String MULTI_REST = "\uD834\uDD3A";

  /** The whole (1/1) rest symbol: {@value}. */
  String WHOLE_REST = "\uD834\uDD3B";

  /** The half (1/2) rest symbol: {@value}. */
  String HALF_REST = "\uD834\uDD3C";

  /** The quarter (1/4) rest symbol: {@value}. */
  String QUARTER_REST = "\uD834\uDD3D";

  /** The eighth (1/8) rest symbol: {@value}. */
  String EIGHTH_REST = "\uD834\uDD3E";

  /** The sixteenth (1/16) rest symbol: {@value}. */
  String SIXTEENTH_REST = "\uD834\uDD3F";

  /** The thirty-second (1/32) rest symbol: {@value}. */
  String THIRTY_SECOND_REST = "\uD834\uDD40";

  /** The sixty-fourth (1/64) rest symbol: {@value}. */
  String SIXTY_FOURTH_REST = "\uD834\uDD41";

  /** The one-hundred-twenty-eighth (1/128) rest symbol: {@value}. */
  String ONE_HUNDRED_TWENTY_EIGHTH_REST = "\uD834\uDD42";

  /** The breve (note) symbol: {@value}. */
  String BREVE = "\uD834\uDD5C";

  /** The whole (4/4) note symbol: {@value}. */
  String WHOLE_NOTE = "\uD834\uDD5D";

  /** The half (1/2) note symbol: {@value}. */
  String HALF_NOTE = "\uD834\uDD5E";

  /** The quarter (1/4) note symbol: {@value}. */
  String QUARTER_NOTE = "\uD834\uDD5F";

  /** The eighth (1/8) note symbol: {@value}. */
  String EIGHTH_NOTE = "\uD834\uDD60";

  /** The sixteenth (1/16) note symbol: {@value}. */
  String SIXTEENTH_NOTE = "\uD834\uDD61";

  /** The thirty-second (1/32) note symbol: {@value}. */
  String THIRTY_SECOND_NOTE = "\uD834\uDD62";

  /** The sixty-fourth (1/64) note symbol: {@value}. */
  String SIXTY_FOURTH_NOTE = "\uD834\uDD63";

  /** The one-hundred-twenty-eighth (1/128) note symbol: {@value}. */
  String ONE_HUNDRED_TWENTY_EIGHTH_NOTE = "\uD834\uDD64";

  /** The combining accent symbol: {@value}. */
  String COMBINING_ACCENT = "\uD834\uDD7B";

  /** The combining staccato symbol: {@value}. */
  String COMBINING_STACCATO = "\uD834\uDD7C";

  /** The combining tenuto symbol: {@value}. */
  String COMBINING_TENUTO = "\uD834\uDD7D";

  /** The combining staccatissimo symbol: {@value}. */
  String COMBINING_STACCATISSIMO = "\uD834\uDD7E";

  /** The combining marcato symbol: {@value}. */
  String COMBINING_MARCATO = "\uD834\uDD7F";

  /** The arpeggiato up symbol: {@value}. */
  String ARPEGGIATO_UP = "\uD834\uDD83";

  /** The arpeggiato down symbol: {@value}. */
  String ARPEGGIATO_DOWN = "\uD834\uDD84";

  /** The rinforzando symbol: {@value}. */
  String RINFORZANDO = "\uD834\uDD8C";

  /** The subito symbol: {@value}. */
  String SUBITO = "\uD834\uDD8D";

  /** The Z symbol: {@value}. */
  String Z = "\uD834\uDD8E";

  /** The piano symbol: {@value}. */
  String PIANO = "\uD834\uDD8F";

  /** The piano symbol: {@value}. */
  String MEZZO = "\uD834\uDD90";

  /** The forte symbol: {@value}. */
  String FORTE = "\uD834\uDD91";

  /** The crescendo symbol: {@value}. */
  String CRESCENDO = "\uD834\uDD92";

  /** The decrescendo symbol: {@value}. */
  String DECRESCENDO = "\uD834\uDD93";

  /** The TR (trello) symbol: {@value}. */
  String TR = "\uD834\uDD96";

  /** The turn symbol: {@value}. */
  String TURN = "\uD834\uDD97";

  /** The inverted turn symbol: {@value}. */
  String INVERTED_TURN = "\uD834\uDD98";

  /** The turn slash symbol: {@value}. */
  String TURN_SLASH = "\uD834\uDD99";

  /** The turn up symbol: {@value}. */
  String TURN_UP = "\uD834\uDD9A";

  /** The ornament stroke-5 symbol: {@value}. */
  String ORNAMENT_STROKE_5 = "\uD834\uDD9F";

  /** The combining down bow symbol: {@value}. */
  String COMBINING_DOWN_BOW = "\uD834\uDDAA";

  /** The combining up bow symbol: {@value}. */
  String COMBINING_UP_BOW = "\uD834\uDDAB";

  /** The combining harmonic symbol: {@value}. */
  String COMBINING_HARMONIC = "\uD834\uDDAC";

  /** The combining snap pizzicato symbol: {@value}. */
  String COMBINING_SNAP_PIZZICATO = "\uD834\uDDAD";

  /** The pedal mark symbol: {@value}. */
  String PEDAL_MARK = "\uD834\uDDAE";

  /** The pedal up mark symbol: {@value}. */
  String PEDAL_UP_MARK = "\uD834\uDDAF";

  /** The half pedal up mark symbol: {@value}. */
  String HALF_PEDAL_MARK = "\uD834\uDDB0";

  /** The glissando up symbol: {@value}. */
  String GLISSANDO_UP = "\uD834\uDDB1";

  /** The glissando down symbol: {@value}. */
  String GLISSANDO_DOWN = "\uD834\uDDB2";

}
