package com.garethevans.church.opensongtablet.core.parser;

import com.garethevans.church.opensongtablet.core.filter.CharFilter;
import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link CharStream} for plain {@link String}.
 */
public class StringCharStream implements CharStream {

    private final String string;

    private final int end;

    private int index;

    private List<Warning> warnings;

    public StringCharStream(String string) {
        this(string, 0);
    }

    public StringCharStream(String string, int index) {
        this(string, index, string.length() - 1);
    }

    public StringCharStream(String string, int index, int end) {
        super();
        assert (end < string.length());
        this.string = string;
        this.end = end;
        this.index = index;
    }

    @Override
    public long getIndex() {
        return this.index;
    }

    @Override
    public boolean hasNext() {
        return (this.index <= this.end);
    }

    @Override
    public char next() {
        if (this.index <= this.end) {
            return this.string.charAt(this.index++);
        }
        return 0;
    }

    @Override
    public char peek() {
        if (this.index <= this.end) {
            return this.string.charAt(this.index);
        }
        return 0;
    }

    @Override
    public String peek(int maxLen) {
        int max = this.index + maxLen;
        if (max > this.end) {
            max = this.end + 1;
        }
        if (this.index >= max) {
            return "";
        }
        return this.string.substring(this.index, max);
    }

    @Override
    public String peekUntil(CharFilter stopFilter, int maxLen) {
        if (this.index > this.end) {
            return "";
        }
        int i = this.index;
        int maxEnd = this.index + maxLen;
        if (maxEnd > this.end) {
            maxEnd = this.end;
        }
        while ((i <= maxEnd) && !stopFilter.accept(this.string.charAt(i))) {
            i++;
        }
        if (i > this.index) {
            return this.string.substring(this.index, i);
        } else {
            return "";
        }
    }

    @Override
    public String peekWhile(CharFilter filter, int maxLen) {
        if (this.index > this.end) {
            return "";
        }
        int i = this.index;
        int maxEnd = this.index + maxLen;
        if (maxEnd > this.end) {
            maxEnd = this.end;
        }
        while ((i <= maxEnd) && filter.accept(this.string.charAt(i))) {
            i++;
        }
        if (i > this.index) {
            return this.string.substring(this.index, i);
        } else {
            return "";
        }
    }

    @Override
    public String read(int length) {
        int max = this.index + length;
        if (max > this.end) {
            max = this.end + 1;
        }
        String result = "";
        if (this.index < max) {
            result = this.string.substring(this.index, max);
        }
        this.index = max;
        return result;
    }

    @Override
    public String readUntil(char stop, boolean acceptEot) {
        if (this.index > this.end) {
            return "";
        }
        int newIndex = this.index;
        while (newIndex <= this.end) {
            if (this.string.charAt(newIndex) == stop) {
                break;
            }
            newIndex++;
        }
        if (acceptEot) {
            String result = this.string.substring(this.index, newIndex);
            this.index = newIndex;
            return result;
        } else {
            return null;
        }
    }

    @Override
    public String readUntil(CharFilter stopFilter, boolean acceptEot) {
        if (this.index > this.end) {
            return "";
        }
        int newIndex = this.index;
        boolean found = false;
        while (newIndex <= this.end) {
            if (stopFilter.accept(this.string.charAt(newIndex))) {
                found = true;
                break;
            }
            newIndex++;
        }
        if (found || acceptEot) {
            String result = this.string.substring(this.index, newIndex);
            this.index = newIndex;
            return result;
        } else {
            return null;
        }
    }

    @Override
    public String readUntil(CharFilter stopFilter, int maxLength) {
        if ((this.index > this.end) || (maxLength <= 0)) {
            return "";
        }
        int newIndex = this.index;
        int maxIndex = this.index + maxLength;
        if ((maxIndex >= this.end) || (maxIndex < this.index)) {
            maxIndex = this.end - 1;
        }
        while (newIndex < maxIndex) {
            if (stopFilter.accept(this.string.charAt(newIndex))) {
                break;
            }
            newIndex++;
        }
        String result = this.string.substring(this.index, newIndex);
        this.index = newIndex;
        return result;
    }

    @Override
    public String readWhile(CharFilter filter) {
        if (this.index > this.end) {
            return "";
        }
        int start = this.index;
        while (this.index <= this.end) {
            if (!filter.accept(this.string.charAt(this.index))) {
                break;
            }
            this.index++;
        }
        return this.string.substring(start, this.index);
    }

    @Override
    public Integer readInteger() {
        return readInteger(11, true);
    }

    @Override
    public Integer readInteger(int maxLen, boolean acceptSign) {
        if ((maxLen > 11) || (maxLen <= 0)) {
            throw new IllegalArgumentException("" + maxLen);
        }
        String integer = readIntegerString(maxLen, acceptSign);
        if (integer == null) {
            return null;
        }
        return Integer.valueOf(integer);
    }

    private String readIntegerString(int maxLength, boolean acceptSign) {
        int newIndex = this.index;
        int max = this.index + maxLength;
        if (max > this.end) {
            max = this.end;
        }
        boolean hasDigit = false;
        while (newIndex <= max) {
            char c = this.string.charAt(newIndex);
            if (ListCharFilter.DIGITS.accept(c)) {
                hasDigit = true;
            } else if (!acceptSign || hasDigit || ((c != '-') && (c != '+'))) {
                break;
            }
            newIndex++;
        }
        if (!hasDigit) {
            return null;
        }
        String number = this.string.substring(this.index, newIndex);
        this.index = newIndex;
        return number;
    }

    @Override
    public int skip(int length) {
        int newIndex = this.index + length;
        int skipped = length;
        if ((newIndex > this.end) || (newIndex < 0)) {
            newIndex = this.end + 1;
            skipped = newIndex - this.index;
        }
        this.index = newIndex;
        return skipped;
    }

    @Override
    public int skipWhile(char skip) {
        int start = this.index;
        while ((this.index <= this.end) && (this.string.charAt(this.index) == skip)) {
            this.index++;
        }
        return this.index - start;
    }

    @Override
    public int skipWhile(CharFilter filter) {
        int start = this.index;
        while ((this.index <= this.end) && filter.accept(this.string.charAt(this.index))) {
            this.index++;
        }
        return this.index - start;
    }

    @Override
    public int skipUntil(CharFilter stopFilter) {
        int start = this.index;
        while ((this.index <= this.end) && !stopFilter.accept(this.string.charAt(this.index))) {
            this.index++;
        }
        return this.index - start;
    }

    @Override
    public boolean skipNewline() {
        if (this.index > this.end) {
            return false;
        }
        char c = this.string.charAt(this.index);
        if (c == '\n') {
            this.index++;
            if ((this.index <= this.end) && (this.string.charAt(this.index) == '\r')) {
                this.index++;
            }
            return true;
        } else if (c == '\r') {
            this.index++;
            if ((this.index <= this.end) && (this.string.charAt(this.index) == '\n')) {
                this.index++;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean expect(char expected) {
        return expect(expected, false);
    }

    @Override
    public boolean expect(char expected, boolean warning) {
        boolean found = false;
        if (this.index <= this.end) {
            char c = this.string.charAt(this.index);
            if (c == expected) {
                found = true;
                this.index++;
            }
        }
        if (!found) {
            getWarnings().add(new Warning(this.index, Character.toString(expected)));
        }
        return found;
    }

    @Override
    public boolean expect(String expected, boolean ignoreCase) {
        int expectedLength = expected.length();
        if ((this.end - this.index) < expectedLength) {
            return false;
        }
        int newIndex = this.index;
        for (int expectedIndex = 0; expectedIndex < expectedLength; expectedIndex++) {
            if (newIndex > this.end) {
                return false;
            }
            char c = this.string.charAt(newIndex);
            char exp = expected.charAt(expectedIndex);
            if (c != exp) {
                if ((!ignoreCase) || (Character.toLowerCase(c) != Character.toLowerCase(exp))) {
                    return false;
                }
            }
            newIndex++;
        }
        this.index = newIndex;
        return true;
    }

    public List<Warning> getWarnings() {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        return this.warnings;
    }

    public String getString() {
        return this.string;
    }

    @Override
    public String toString() {
        if (this.index >= this.end) {
            return "";
        }
        return this.string.substring(this.index);
    }

    public static class Warning {

        private final int startIndex;

        private final int endIndex;

        private final String expected;

        public Warning(int index, String expected) {
            super();
            this.startIndex = index;
            this.endIndex = index;
            this.expected = expected;
        }

        public int getStartIndex() {
            return this.startIndex;
        }

        public int getEndIndex() {
            return this.endIndex;
        }

        public String getExpected() {
            return this.expected;
        }
    }
}
