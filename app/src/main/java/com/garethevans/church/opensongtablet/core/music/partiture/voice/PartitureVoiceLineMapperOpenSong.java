package com.garethevans.church.opensongtablet.core.music.partiture.voice;

import com.garethevans.church.opensongtablet.core.StringHelper;
import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.music.harmony.ChordContainer;
import com.garethevans.church.opensongtablet.core.music.harmony.ChordContainerMapper;
import com.garethevans.church.opensongtablet.core.music.partiture.comment.PartitureCommentLineMapper;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;
import java.util.List;

/**
 * {@link PartitureVoiceLineMapper} for {@link com.garethevans.church.opensongtablet.core.format.SongFormatOpenSong OpenSong format}.
 */
public class PartitureVoiceLineMapperOpenSong extends PartitureVoiceLineMapper {

    public static final PartitureVoiceLineMapperOpenSong INSTANCE = new PartitureVoiceLineMapperOpenSong();

    public static final char BEGIN_CHORDS = '.';

    public static final char BEGIN_ITEMS = '=';

    public static final char BEGIN_LYRICS = ' ';

    @Override
    public PartitureVoiceLine parse(CharStream chars) {
        char c = chars.peek();
        if (c == PartitureCommentLineMapper.BEGIN_COMMENT) {
            return null;
        }
        if (ListCharFilter.NEWLINE.accept(c)) {
            return null;
        }
        PartitureVoiceLine line = new PartitureVoiceLine();
        if (c == BEGIN_CHORDS) {
            chars.next();
            PartitureVoiceLineContinuation continuation = PartitureVoiceLineContinuation.of(c);
            if (continuation != null) {
                chars.next();
                line.setContinuation(continuation);
            }
            while (chars.hasNext() && !chars.skipNewline()) {
                ChordContainer chordContainer = ChordContainerMapper.INSTANCE.parse(chars);
                if (chordContainer == null) {
                    break;
                } else {
                    line.addCell(new PartitureVoiceCell(chordContainer));
                }
            }
            c = chars.peek();
        }
        if (c == BEGIN_LYRICS) {
            chars.next();
        }
        int cellCount = line.getCellCount();
        if (cellCount == 0) {
            String lyric = chars.readUntil(ListCharFilter.NEWLINE, true);
            line.addCell(new PartitureVoiceCell(lyric));
            chars.skipNewline();
        } else {
            int cellMax = cellCount - 1;
            for (int i = 0; i < cellMax; i++) {
                PartitureVoiceCell cell = line.getCell(i);
                ChordContainer chordContainer = cell.getChordContainer();
                int chordLength = chordContainer.toString().length();
                String lyric = chars.readUntil(ListCharFilter.NEWLINE, chordLength);
                cell.setLyric(lyric);
            }
            String lyric = chars.readUntil(ListCharFilter.NEWLINE, true);
            if (lyric.length() > 0) {
                PartitureVoiceCell cell = line.getCell(cellMax);
                cell.setLyric(lyric);
            }
            chars.skipNewline();
        }
        return line;
    }

    @Override
    public void format(PartitureVoiceLine line, Appendable buffer, SongFormatOptions options) throws IOException {
        if (line == null) {
            return;
        }
        buffer.append(BEGIN_CHORDS);
        PartitureVoiceLineContinuation continuation = line.getContinuation();
        if (continuation != null) {
            buffer.append(continuation.getSymbol());
        }
        StringBuilder lyrics = new StringBuilder();
        lyrics.append(BEGIN_LYRICS);
        List<PartitureVoiceCell> cells = line.getCells();
        int cellMax = cells.size() - 1;
        for (int i = 0; i <= cellMax; i++) {
            PartitureVoiceCell cell = cells.get(i);
            int chordLength = 0;
            ChordContainer chordContainer = cell.getChordContainer();
            if (chordContainer != null) {
                String chord = chordContainer.toString();
                buffer.append(chord); // ChordContainerMapper.INSTANCE.format(chordContainer, buffer, options);
                chordLength = chord.length();
                if (i < cellMax) {
                    buffer.append(' ');
                    chordLength++;
                }
            }
            String lyric = cell.getLyric();
            lyrics.append(lyric);
            if (i < cellMax) {
                int spaces = lyric.length() - chordLength;
                if (spaces > 0) {
                    StringHelper.appendSpaces(buffer, spaces);
                } else if (spaces < 0) {
                    StringHelper.appendSpaces(lyrics, -spaces);
                }
            }
        }
        buffer.append(NEWLINE);
        buffer.append(lyrics);
        buffer.append(NEWLINE);
    }
}
