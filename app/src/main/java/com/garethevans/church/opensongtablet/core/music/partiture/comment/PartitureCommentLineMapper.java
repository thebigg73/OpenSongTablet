package com.garethevans.church.opensongtablet.core.music.partiture.comment;

import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;
import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link PartitureCommentLine}.
 */
public class PartitureCommentLineMapper extends AbstractMapper<PartitureCommentLine> {

    public static final char BEGIN_COMMENT = ';';

    public static final PartitureCommentLineMapper INSTANCE = new PartitureCommentLineMapper();

    @Override
    public PartitureCommentLine parse(CharStream chars) {
        char c = chars.peek();
        if (c == BEGIN_COMMENT) {
            chars.next();
            String comment = chars.readUntil(ListCharFilter.NEWLINE, true);
            chars.skipNewline();
            return new PartitureCommentLine(comment);
        }
        return null;
    }

    @Override
    public void format(PartitureCommentLine line, Appendable buffer, SongFormatOptions options) throws IOException {
        buffer.append(BEGIN_COMMENT);
        buffer.append(line.getComment());
        buffer.append(NEWLINE);
    }
}
