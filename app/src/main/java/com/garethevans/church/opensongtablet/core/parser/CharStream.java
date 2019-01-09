package com.garethevans.church.opensongtablet.core.parser;

import com.garethevans.church.opensongtablet.core.filter.CharFilter;

/**
 * A stream of characters with method to consume and of the underlying text. Data may come from any
 * source like a {@link String} or {@link java.io.Reader}.
 */
public interface CharStream {

    /**
     * @return the current index in the stream of characters or in other words the number of characters
     * that have been consumed from this stream.
     */
    long getIndex();

    /**
     * @return {@code true} if a {@link #next() next character} is available, {@code false} otherwise
     * (end-of-text has been reached).
     */
    boolean hasNext();

    /**
     * @return the current character in this stream (incrementing the {@link #getIndex() index} by 1). If
     * no {@link #hasNext() next character} is available, the character NUL ({@code '\0'}) is returned and
     * the {@link #getIndex() index} remains unchanged.
     */
    char next();

    /**
     * @return the current character in this stream (without modifying the state of this stream). If
     * no {@link #hasNext() next character} is available, the character NUL ({@code '\0'}) is returned.
     */
    char peek();

    /**
     * @param maxLen the maximum number of characters to peek (get as lookahead without modifying this stream).
     * @return a {@link String} with the {@link #peek() peeked} characters of the given {@code length}
     * or less if the end-of-text has been reached before. The state of this stream remains unchanged.
     * @see #skip(int)
     */
    String peek(int maxLen);

    /**
     * @param filter the {@link CharFilter} {@link CharFilter#accept(char) accepting} only the characters
     *               to peek.
     * @param maxLen the maximum number of characters to peek (get as lookahead without modifying this stream).
     * @return a {@link String} with the {@link #peek() peeked} characters of the given {@code maxLen}
     * or less if a character was hit that is <em>not</em> {@link CharFilter#accept(char) accepted} by
     * the given {@code filter} or the end-of-text has been reached before. The state of this stream remains unchanged.
     * @see #readWhile(CharFilter)
     * @see #skip(int)
     */
    String peekWhile(CharFilter filter, int maxLen);

    /**
     * @param stopFilter the {@link CharFilter} that decides which characters to {@link CharFilter#accept(char) accept}
     *                   as stop characters.
     * @param maxLen     the maximum number of characters to peek (get as lookahead without modifying this stream).
     * @return a {@link String} with the {@link #peek() peeked} characters of the given {@code maxLen}
     * or less if a stop character was hit or the end-of-text has been reached before. The state of this
     * stream remains unchanged.
     * @see #readWhile(CharFilter)
     * @see #skip(int)
     */
    String peekUntil(CharFilter stopFilter, int maxLen);

    /**
     * @param stop      the character where to stop consuming.
     * @param acceptEot {@code true} to accept end-of-text like {@code stop}, {@code false} otherwise.
     * @return the {@link String} with all consumed characters excluding the {@code stop} character
     * (the {@link #next() next} character will then be the stop character or end-of-text).
     * If no {@code stop} character was found and acceptEot was {@code false}, then {@code null}
     * is returned and the state of this stream remains unchanged.
     */
    String readUntil(char stop, boolean acceptEot);

    /**
     * @param stopFilter the {@link CharFilter} that decides which characters to {@link CharFilter#accept(char) accept}
     *                   as stop characters.
     * @param acceptEot  {@code true} to accept end-of-text like stop characters, {@code false} otherwise.
     * @return the {@link String} with all consumed characters excluding the stop character
     * (the {@link #next() next} character will then be the stop character or end-of-text).
     * If no {@code stop} character was found and acceptEot was {@code false}, then {@code null}
     * is returned and the state of this stream remains unchanged.
     * @see #peekUntil(CharFilter, int)
     */
    String readUntil(CharFilter stopFilter, boolean acceptEot);

    /**
     * @param stopFilter the {@link CharFilter} that decides which characters to {@link CharFilter#accept(char) accept}
     *                   as stop characters.
     * @param maxLength  the (maximum) length of the characters to consume.
     * @return the {@link String} with all consumed characters excluding the stop character.
     * If no {@code stop} character was found until {@code maxLength} characters have been consumed,
     * this method behaves like {@link #read(int) read(maxLength)}.
     * @see #read(int)
     * @see #peekUntil(CharFilter, int)
     */
    String readUntil(CharFilter stopFilter, int maxLength);

    /**
     * @param filter the {@link CharFilter} {@link CharFilter#accept(char) accepting} all characters
     *               that shall be consumed.
     * @return the {@link String} with all consumed characters. If already the first {@link #next() next}
     * character was {@link #hasNext() unavailable} or not {@link CharFilter#accept(char) accepted},
     * the empty {@link String} is returned and this stream remains unchanged.
     * @see #peekWhile(CharFilter, int)
     */
    String readWhile(CharFilter filter);

    /**
     * @param length the number of characters to consume.
     * @return a {@link String} with the {@link #next() next} consumed characters of the given {@code length}
     * or less if the end-of-text has been reached before.
     */
    String read(int length);

    /**
     * @return an {@link Integer} from all {@link #next() next} consumed characters that belong to an {@link Integer} number
     * or {@code null} if no integer was found and this stream remains unchanged.
     */
    Integer readInteger();

    /**
     * @param maxLen     the maximum number of characters to consume. Has to be in the range from 1 to 11 ({@code -2147483648}).
     * @param acceptSign {@code true} to accept a leading signum ('+' or '-'), {@code false} otherwise
     *                   (will prevent negative numbers).
     * @return an {@link Integer} from all {@link #next() next} consumed characters that belong to an {@link Integer} number
     * or {@code null} if no integer was found and this stream remains unchanged.
     */
    Integer readInteger(int maxLen, boolean acceptSign);

    /**
     * @param length the number of characters to skip.
     * @return the number of characters that actually have been skipped. May be less than {@code length}
     * if the end-of-text has been reached.
     */
    int skip(int length);

    /**
     * @param skip the character to skip.
     * @return the number of {@code skip} characters that have been consumed (skipped). The
     * {@link #getIndex() index} has increased by this number.
     */
    int skipWhile(char skip);

    /**
     * @return {@code true} if a single newline ('\r\n', '\n\r', '\n', or '\r') has been consumed (skipped).
     * Otherwise {@code false} is returned and this stream remains unchanged.
     */
    boolean skipNewline();

    /**
     * @param filter the {@link CharFilter} {@link CharFilter#accept(char) accepting} all characters
     *               that shall be skipped.
     * @return the number of {@code skip} characters that have been consumed (skipped). The
     * {@link #getIndex() index} has increased by this number.
     */
    int skipWhile(CharFilter filter);

    /**
     * @param stopFilter the {@link CharFilter} that decides which characters to {@link CharFilter#accept(char) accept}
     *                   as stop characters. All other characters will be consumed (skipped) until the first
     *                   stop character was found or the end-of-text was reached.
     * @return the number of characters that have been consumed (skipped). The {@link #getIndex() index}
     * has increased by this number.
     */
    int skipUntil(CharFilter stopFilter);

    /**
     * @param expected   the {@link String} to expect as next characters in this stream.
     * @param ignoreCase {@code true} if the {@link String#equalsIgnoreCase(String)} case shall be ignored},
     *                   {@code false} otherwise.
     * @return {@code true} if the expected {@link String} was found and consumed, {@code false} otherwise
     * (and this stream remains unchanged).
     */
    boolean expect(String expected, boolean ignoreCase);

    /**
     * @param expected the character to expect as {@link #next() next} in this stream.
     * @return {@code true} if the expected character was found and consumer, {@code false} otherwise
     * (and this stream remains unchanged).
     */
    boolean expect(char expected);

    /**
     * @param expected the character to expect as {@link #next() next} in this stream.
     * @param warning {@code true} if a warning is collected in case the expected character was not present.
     * @return {@code true} if the expected character was found and consumer, {@code false} otherwise
     * (and this stream remains unchanged).
     */
    boolean expect(char expected, boolean warning);

}
