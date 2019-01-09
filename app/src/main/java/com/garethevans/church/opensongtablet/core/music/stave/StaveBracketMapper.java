package com.garethevans.church.opensongtablet.core.music.stave;

import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

public class StaveBracketMapper extends AbstractMapper<StaveBracket> {

    public static final StaveBracketMapper INSTANCE = new StaveBracketMapper();

    @Override
    public StaveBracket parse(CharStream chars) {
        String syntax = chars.peek(2);
        StaveBracket bracket = StaveBracket.ofSyntax(syntax);
        if (bracket != null) {
            chars.skip(2);
        }
        return bracket;
    }

    @Override
    public void format(StaveBracket bracket, Appendable buffer, SongFormatOptions options) throws IOException {
        if (bracket == null) {
            return;
        }
        buffer.append(bracket.getSyntax());
    }
}
