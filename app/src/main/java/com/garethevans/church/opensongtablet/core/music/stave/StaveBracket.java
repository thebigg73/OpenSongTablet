package com.garethevans.church.opensongtablet.core.music.stave;

import com.garethevans.church.opensongtablet.core.music.MusicalUnicodeConstants;

/**
 * The bracket (type) of one or multiple {@link Stave}s.
 */
public enum StaveBracket {

    SQUARE('[', ']', MusicalUnicodeConstants.BRACKET),

    CURLY('{', '}', MusicalUnicodeConstants.BRACE);

    private final char start;

    private final char end;

    private final String syntax;

    private final String unicode;

    private StaveBracket(char start, char end, String unicode) {
        this.start = start;
        this.end = end;
        this.syntax = "" + start + end;
        this.unicode = unicode;
    }

    public char getStart() {
        return this.start;
    }

    public char getEnd() {
        return this.end;
    }

    public String getSyntax() {
        return this.syntax;
    }

    public String getUnicode() {
        return this.unicode;
    }

    @Override
    public String toString() {
        return "" + this.start + this.end;
    }

    public static StaveBracket ofStart(char c) {
        for (StaveBracket bracket : values()) {
            if (bracket.start == c) {
                return bracket;
            }
        }
        return null;
    }

    public static StaveBracket ofEnd(char c) {
        for (StaveBracket bracket : values()) {
            if (bracket.end == c) {
                return bracket;
            }
        }
        return null;
    }

    public static StaveBracket ofSyntax(String syntax) {
        for (StaveBracket bracket : values()) {
            if (bracket.syntax.equals(syntax)) {
                return bracket;
            }
        }
        return null;
    }
}
