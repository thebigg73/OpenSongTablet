package com.garethevans.church.opensongtablet.core.music.partiture.section;

import com.garethevans.church.opensongtablet.core.filter.CharFilter;
import com.garethevans.church.opensongtablet.core.filter.ListCharFilter;
import com.garethevans.church.opensongtablet.core.format.AbstractMapper;
import com.garethevans.church.opensongtablet.core.format.SongFormatConstants;
import com.garethevans.church.opensongtablet.core.format.SongFormatOptions;
import com.garethevans.church.opensongtablet.core.parser.CharStream;

import java.io.IOException;

/**
 * {@link AbstractMapper Mapper} for {@link PartitureSectionName}.
 */
public class PartitureSectionNameMapper extends AbstractMapper<PartitureSectionName> {

    private static final CharFilter STOP_FILTER = ListCharFilter.allOf(SongFormatConstants.END_SECTION, '\n', '\r');

    public static final PartitureSectionNameMapper CHORD_PRO = new PartitureSectionNameMapper("#" + SECTION_START);

    public static final PartitureSectionNameMapper OPEN_SONG = new PartitureSectionNameMapper("" + SECTION_START);

    private final String sectionStart;

    public PartitureSectionNameMapper(String sectionStart) {
        this.sectionStart = sectionStart;
    }

    @Override
    public PartitureSectionName parse(CharStream chars) {
        if (chars.expect(this.sectionStart, false)) {
            String section = chars.readUntil(STOP_FILTER, true);
            PartitureSectionName name = new PartitureSectionName(section);
            chars.expect(SECTION_END, true);
            return name;
        }
        return null;
    }

    @Override
    public void format(PartitureSectionName object, Appendable buffer, SongFormatOptions options) throws IOException {
        buffer.append(this.sectionStart);
        buffer.append(object.getName());
        buffer.append(SECTION_END);
    }

}
