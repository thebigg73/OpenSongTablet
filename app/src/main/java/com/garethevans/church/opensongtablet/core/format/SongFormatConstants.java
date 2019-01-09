package com.garethevans.church.opensongtablet.core.format;

public interface SongFormatConstants {

    char BEGIN_COMMENT = ';';

    char BEGIN_SECTION = '#';

    String START_SECTION_CHORD_PRO = "#[";

    String START_SECTION_OPEN_SONG = "[";

    char END_SECTION = ']';

    char BEGIN_CHORDS = '.';

    char CONTINUE_ROW = '+';

    char START_CHORD = '[';

    char END_CHORD = ']';

    char START_TONE = '{';

    char SEPARATE_TONE = ';';

    String SEPARATE_TONE_STRING = "" + SEPARATE_TONE;

    char SEPARATE_STAVE = '=';

    String SEPARATE_STAVE_STRING = "" + SEPARATE_STAVE;

    char END_TONE = '}';

    char START_BLOCK = '{';

    char END_BLOCK = '}';

    char PROPERTY_KEY_VALUE_SEPARATOR = ':';

    char PROPERTY_SEPARATOR = ';';

    char VOICE_SEPARATOR = ',';

    char STAVE_SEPARATOR = '|';

    char PROPERTY_KEY = 'K';

    String PROPERTY_KEY_PREFIX = "" + PROPERTY_KEY + PROPERTY_KEY_VALUE_SEPARATOR;;

    char PROPERTY_BEAT = 'M';

    String PROPERTY_BEAT_PREFIX = "" + PROPERTY_BEAT + PROPERTY_KEY_VALUE_SEPARATOR;

    char PROPERTY_CLEV = '§';

    String PROPERTY_CLEV_PREFIX = "" + PROPERTY_CLEV + PROPERTY_KEY_VALUE_SEPARATOR;

    char PROPERTY_VOICE = 'V';

    String PROPERTY_VOICE_PREFIX = "" + PROPERTY_VOICE + PROPERTY_KEY_VALUE_SEPARATOR;

    char NEWLINE = '\n';

}
