package com.garethevans.church.opensongtablet.core.music.partiture.voice;

import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.music.partiture.comment.PartitureCommentLineMapper;
import com.garethevans.church.opensongtablet.core.music.stave.StaveBracket;
import com.garethevans.church.opensongtablet.core.music.stave.StaveBracketMapper;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link PartitureVoiceLineMapper} for {@link com.garethevans.church.opensongtablet.core.format.SongFormatChordPro ChordPro format}.
 */
public class PartitureVoiceLineMapperChordPro extends PartitureVoiceLineMapper {

    public static final PartitureVoiceLineMapperChordPro INSTANCE = new PartitureVoiceLineMapperChordPro();

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
        PartitureVoiceLineContinuation continuation = PartitureVoiceLineContinuation.of(c);
        if (continuation != null) {
            chars.next();
            line.setContinuation(continuation);
        }
        while (chars.hasNext() && !chars.skipNewline()) {
            long index = chars.getIndex();
            PartitureVoiceCell cell = PartitureVoiceCellMapperChordPro.INSTANCE.parse(chars);
            if (chars.getIndex() > index) {
                line.addCell(cell);
            } else {
                // ups, parser error - prevent infinity loop
                // todo: log error
                chars.next();
            }
        }
        return line;
    }

    @Override
    public void format(PartitureVoiceLine line, Appendable buffer, SongFormatOptions options) throws IOException {
        if (line == null) {
            return;
        }
        PartitureVoiceLineContinuation continuation = line.getContinuation();
        if (continuation != null) {
            buffer.append(continuation.getSymbol());
        }
        for (PartitureVoiceCell cell : line.getCells()) {
            PartitureVoiceCellMapperChordPro.INSTANCE.format(cell, buffer, options);
        }
        buffer.append(NEWLINE);
    }
}
